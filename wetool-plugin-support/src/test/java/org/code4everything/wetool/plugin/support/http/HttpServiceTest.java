package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.thread.ThreadUtil;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.util.Map;

public class HttpServiceTest {

    public static void main(String[] args) {
        HttpService.setDefaultPort(58189);
        HttpService.exportHttp("post/api/hello", (req, resp, params, body) -> {
            Map<String, FileUpload> map = Https.getMultipartFiles(req);
            map.forEach((k, v) -> {
                Https.writeMultipartFile(v, "D:\\Projects\\Java\\wetool-plugin");
            });
            return null;
        });

        while (true) {
            ThreadUtil.sleep(1000);
        }
    }
}
