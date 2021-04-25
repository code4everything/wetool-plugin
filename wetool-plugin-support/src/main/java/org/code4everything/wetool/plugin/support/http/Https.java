package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
        FILE_TYPE_MAP.put("mp4", "video/mp4");
        FILE_TYPE_MAP.put("torrent", "application/x-bittorrent");
        FILE_TYPE_MAP.put("svg", "text/xml");
        FILE_TYPE_MAP.put("mp3", "audio/mp3");
        FILE_TYPE_MAP.put("mp2", "audio/mp2");
        FILE_TYPE_MAP.put("exe", "application/x-msdownload");
        FILE_TYPE_MAP.put("css", "text/css");
    }

    /**
     * 响应HTML内容
     */
    public static FullHttpResponse responseHtml(FullHttpResponse response, String html) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
        return setContent(response, str2buf(html));
    }

    /**
     * 响应普通文本
     */
    public static FullHttpResponse responseText(FullHttpResponse response, String text) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8");
        return setContent(response, str2buf(text));
    }

    /**
     * 响应流媒体文件
     */
    public static FullHttpResponse responseMedia(FullHttpResponse response, String fileAbsolutePath) {
        String type = FILE_TYPE_MAP.getOrDefault(FileUtil.getSuffix(fileAbsolutePath), "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, type + ";charset=utf-8");
        return setContent(response, Unpooled.copiedBuffer(FileUtil.readBytes(fileAbsolutePath)));
    }

    /**
     * 客户端下载文件
     */
    public static FullHttpResponse responseFile(FullHttpResponse response, String fileAbsolutePath) {
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, HttpHeaderValues.FILE);
        return setContent(response, Unpooled.copiedBuffer(FileUtil.readBytes(fileAbsolutePath)));
    }

    /**
     * 响应JSON
     */
    public static FullHttpResponse responseJson(FullHttpResponse response, Object object) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        return setContent(response, str2buf(objectToJsonString(object)));
    }

    /**
     * 对象转JSON
     */
    public static String objectToJsonString(Object object) {
        return JSON.toJSONString(object, SerializerFeature.QuoteFieldNames,
                SerializerFeature.WriteMapNullValue, SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.SkipTransientField, SerializerFeature.WriteNonStringKeyAsString);
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
