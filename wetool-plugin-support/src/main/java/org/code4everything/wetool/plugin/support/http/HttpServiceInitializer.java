package org.code4everything.wetool.plugin.support.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import lombok.RequiredArgsConstructor;

/**
 * @author pantao
 * @since 2020/12/5
 */
@RequiredArgsConstructor
public class HttpServiceInitializer extends ChannelInitializer<SocketChannel> {

    private final int port;

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        p.addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin().allowCredentials().allowNullOrigin().build()));
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast(new HttpServiceHandler(port));
    }
}
