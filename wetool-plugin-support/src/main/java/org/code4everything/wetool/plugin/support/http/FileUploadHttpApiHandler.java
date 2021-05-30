package org.code4everything.wetool.plugin.support.http;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import lombok.EqualsAndHashCode;

/**
 * @author pantao
 * @since 2021/5/30
 */
@EqualsAndHashCode
public abstract class FileUploadHttpApiHandler implements HttpApiHandler {

    private String dir;

    public FileUploadHttpApiHandler setDir(String dir) {
        this.dir = dir;
        return this;
    }

    @Override
    public Object handleApi(HttpRequest req, FullHttpResponse resp, JSONObject params, JSONObject body) {
        Https.writeMultipartFiles(req, dir);
        return null;
    }
}
