package org.code4everything.wetool.plugin.support.http;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * 响应JSON
 *
 * @author pantao
 * @since 2020/12/5
 */
@FunctionalInterface
public interface HttpApiHandler {

    /**
     * 处理api
     *
     * @param req http请求
     * @param resp http响应
     * @param params 请求参数json
     * @param body 请求body json
     *
     * @return 响应json
     */
    Object handleApi(HttpRequest req, FullHttpResponse resp, JSONObject params, JSONObject body);
}
