package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.event.handler.BaseNoMessageEventHandler;
import org.code4everything.wetool.plugin.support.exception.HttpExportException;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pantao
 * @since 2020/12/5
 */
@Slf4j
@UtilityClass
public class HttpService {

    public static final String REQ_API_KEY = "wetool-http-request-api";

    static final Map<Integer, Map<String, HttpApiHandler>> HTTP_SERVICE = new ConcurrentHashMap<>(8);

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final Thread.UncaughtExceptionHandler exceptionHandler = (t, e) -> log.error(ExceptionUtil.stacktraceToString(e), Integer.MAX_VALUE);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "http-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        }
    };

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(4, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(32), THREAD_FACTORY);

    private static Integer defaultPort = null;

    /**
     * 获取默认端口
     *
     * @since 1.3.0
     */
    public static int getDefaultPort() {
        return ObjectUtil.defaultIfNull(defaultPort, 8189);
    }

    public static void setDefaultPort(int port) {
        if (Objects.isNull(defaultPort)) {
            defaultPort = port;
        }
    }

    /**
     * 暴露http文件上传接口
     *
     * @param port 端口
     * @param api 请求接口
     * @param dir 文件上传目录
     *
     * @since 1.6.0
     */
    public static void exportHttpFileUpload(int port, String api, String dir) {
        exportHttp(port, api, new FileUploadHttpApiHandler() {}.setDir(dir));
    }

    /**
     * 暴露http服务
     *
     * @param port 端口
     * @param method 请求方法
     * @param api 请求接口
     * @param handler 请求处理回调
     *
     * @since 1.3.0
     */
    public static void exportHttp(int port, HttpMethod method, String api, HttpApiHandler handler) {
        exportHttp(port, method.name().toLowerCase() + " " + api, handler);
    }

    /**
     * 暴露http服务
     *
     * @param api 请求接口，包含请求方法（小写），例如：get/api/hello, post/api/register
     * @param handler 请求处理回调
     *
     * @since 1.3.0
     */
    public static void exportHttp(String api, HttpApiHandler handler) {
        exportHttp(getDefaultPort(), api, handler);
    }

    /**
     * 暴露http服务，支持模式匹配，如：get/api/file/*
     *
     * @param port 端口
     * @param api 请求接口，包含请求方法（小写），例如：get/api/hello, post/api/register
     * @param handler 请求处理回调
     *
     * @since 1.3.0
     */
    public static void exportHttp(int port, String api, HttpApiHandler handler) {
        Preconditions.checkArgument(StrUtil.isNotBlank(api), "api must not be blank");
        if (!NetUtil.isValidPort(port)) {
            // 非法端口
            throw new HttpExportException(StrUtil.format("invalid port: {}", port));
        }

        if (!HTTP_SERVICE.containsKey(port)) {
            if (!NetUtil.isUsableLocalPort(port)) {
                // 端口已被其他程序占用
                throw new HttpExportException(StrUtil.format("port[{}] is already in used", port));
            }

            WeUtils.execute(() -> runHttpService(port));
        }

        Map<String, HttpApiHandler> apiMap = HTTP_SERVICE.computeIfAbsent(port, p -> new ConcurrentHashMap<>(32));

        if (apiMap.containsKey(api)) {
            throw new HttpExportException(StrUtil.format("api[{}] is already mapped", api));
        }

        apiMap.put(api, handler);
        log.info("export api[{}] on port[{}] success", api, port);
    }

    @SneakyThrows
    private static void runHttpService(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, THREAD_POOL_EXECUTOR);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1, THREAD_POOL_EXECUTOR);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new HttpServiceInitializer(port));

            Channel ch = b.bind(port).sync().channel();
            EventCenter.onWetoolExit(new BaseNoMessageEventHandler() {
                @Override
                public void handleEvent0(String eventKey, Date eventTime) {
                    log.info("shutdown http service, port: {}", port);
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            log.info("export http service: http://127.0.0.1:{}", port);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
