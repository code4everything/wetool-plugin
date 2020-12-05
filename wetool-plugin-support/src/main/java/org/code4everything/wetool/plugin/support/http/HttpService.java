package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.net.NetUtil;
import io.netty.handler.codec.http.HttpMethod;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pantao
 * @since 2020/12/5
 */
@Slf4j
@UtilityClass
public class HttpService {

    static final Map<Integer, Map<String, HttpApiHandler>> HTTP_SERVICE = new HashMap<>(8);

    /**
     * 暴露http服务
     *
     * @param port 端口
     * @param method 请求方法
     * @param api 请求接口
     * @param handler 请求处理回调
     *
     * @return 接口服务是否暴露成功
     *
     * @since 1.3.0
     */
    public static boolean exportHttp(int port, HttpMethod method, String api, HttpApiHandler handler) {
        return exportHttp(port, method.name().toLowerCase() + " " + api, handler);
    }

    /**
     * 暴露http服务
     *
     * @param port 端口
     * @param api 请求接口，包含请求方法（小写），例如：get /api/hello, post /api/register
     * @param handler 请求处理回调
     *
     * @return 接口服务是否暴露成功
     *
     * @since 1.3.0
     */
    public static boolean exportHttp(int port, String api, HttpApiHandler handler) {
        if (!NetUtil.isValidPort(port)) {
            // 非法端口
            log.error("invalid port: {}", port);
            return false;
        }

        if (!HTTP_SERVICE.containsKey(port)) {
            if (!NetUtil.isUsableLocalPort(port)) {
                // 端口已被其他程序占用
                log.error("port[{}] is already in used", port);
                return false;
            }
            // TODO: 2020/12/6 暴露http服务
        }

        Map<String, HttpApiHandler> apiMap = HTTP_SERVICE.computeIfAbsent(port, p -> new HashMap<>(32));

        if (apiMap.containsKey(api)) {
            log.error("api[{}] is already mapped", api);
            return false;
        }

        apiMap.put(api, handler);
        return true;
    }
}
