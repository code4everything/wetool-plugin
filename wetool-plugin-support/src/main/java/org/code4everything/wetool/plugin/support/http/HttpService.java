package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pantao
 * @since 2020/12/5
 */
@Slf4j
@UtilityClass
public class HttpService {

    public static final int DEFAULT_PORT = 8189;

    static final Map<Integer, Map<String, HttpApiHandler>> HTTP_SERVICE = new ConcurrentHashMap<>(8);

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
     * @param port 端口
     * @param api 请求接口，包含请求方法（小写），例如：get /api/hello, post /api/register
     * @param handler 请求处理回调
     *
     * @since 1.3.0
     */
    public static void exportHttp(int port, String api, HttpApiHandler handler) {
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
    }

    @SneakyThrows
    private static void runHttpService(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new HttpServiceInitializer(port));

            Channel ch = b.bind(port).sync().channel();
            EventCenter.subscribeEvent(EventCenter.EVENT_WETOOL_EXIT, new BaseNoMessageEventHandler() {
                @Override
                public void handleEvent0(String eventKey, Date eventTime) {
                    log.info("shut down http service, port: {}", port);
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
