package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pantao
 * @since 2021/4/25
 */
@UtilityClass
public class Https {

    private static final Map<String, String> FILE_TYPE_MAP = new HashMap<>(8, 1);

    static {
        FILE_TYPE_MAP.put("html", "text/html");
        FILE_TYPE_MAP.put("xml", "text/xml");
        FILE_TYPE_MAP.put("gif", "image/gif");
        FILE_TYPE_MAP.put("jpg", "image/jpeg");
        FILE_TYPE_MAP.put("jpeg", "image/jpeg");
        FILE_TYPE_MAP.put("png", "image/png");
        FILE_TYPE_MAP.put("json", "application/json");
        FILE_TYPE_MAP.put("pdf", "application/pdf");
        FILE_TYPE_MAP.put("mp4", "video/mpeg4");
        FILE_TYPE_MAP.put("torrent", "application/x-bittorrent");
        FILE_TYPE_MAP.put("svg", "text/xml");
        FILE_TYPE_MAP.put("mp3", "audio/mp3");
        FILE_TYPE_MAP.put("mp2", "audio/mp2");
        FILE_TYPE_MAP.put("exe", "application/x-msdownload");
        FILE_TYPE_MAP.put("css", "text/css");
    }

    public static FullHttpResponse writeHtml(FullHttpResponse response, String html) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
        return setContent(response, str2buf(html));
    }

    public static FullHttpResponse writeText(FullHttpResponse response, String text) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8");
        return setContent(response, str2buf(text));
    }

    public static FullHttpResponse writeMedia(FullHttpResponse response, String fileAbsolutePath) {
        String type = FILE_TYPE_MAP.getOrDefault(FileUtil.getSuffix(fileAbsolutePath), "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, type + ";charset=utf-8");
        return setContent(response, Unpooled.copiedBuffer(FileUtil.readBytes(fileAbsolutePath)));
    }

    public static FullHttpResponse writeFile(FullHttpResponse response, String fileAbsolutePath) {
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, HttpHeaderValues.FILE);
        return response;
    }

    private static FullHttpResponse setContent(FullHttpResponse response, ByteBuf content) {
        if (response instanceof WeFullHttpResponse) {
            ((WeFullHttpResponse) response).setContent(content);
            return response;
        }
        return response.replace(content);
    }

    static ByteBuf str2buf(String str) {
        return Unpooled.copiedBuffer(str, CharsetUtil.CHARSET_UTF_8);
    }
}
