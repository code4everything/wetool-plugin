/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author pantao
 * @since 2020/12/5
 */
@Slf4j
@RequiredArgsConstructor
public class HttpHelloWorldServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final int port;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            String uri = req.uri();
            int idx = uri.indexOf("?");
            String api = req.method().name().toLowerCase() + " " + (idx > 0 ? uri.substring(0, idx) : uri);

            // TODO: 2020/12/6 解析参数和body

            FullHttpResponse response = getResponse(req, api);

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON).setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }
            } else {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }

            ChannelFuture f = ctx.write(response);

            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private FullHttpResponse getResponse(HttpRequest req, String api) {
        HttpVersion httpVersion = req.protocolVersion();
        HttpApiHandler apiHandler = HttpService.HTTP_SERVICE.get(port).get(api);

        FullHttpResponse response;

        if (Objects.isNull(apiHandler)) {
            response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.NOT_FOUND,
                    Unpooled.wrappedBuffer(new byte[0]));
        } else {
            response = new WeFullHttpResponse(httpVersion, HttpResponseStatus.OK);
            try {
                Object responseObject = apiHandler.handleApi(req, response, null, null);
                if (Objects.nonNull(responseObject)) {
                    String responseJson = JSON.toJSONString(responseObject);
                    ((WeFullHttpResponse) response).setContent(Unpooled.wrappedBuffer(responseJson.getBytes()));
                }
            } catch (Exception e) {
                String errMsg = ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE);
                log.error(errMsg);
                ByteBuf content = Unpooled.wrappedBuffer(errMsg.getBytes());
                HttpResponseStatus serverError = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                response = new DefaultFullHttpResponse(httpVersion, serverError, content);
            }
        }

        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ExceptionUtil.stacktraceToString(cause, Integer.MAX_VALUE));
        ctx.close();
    }
}
