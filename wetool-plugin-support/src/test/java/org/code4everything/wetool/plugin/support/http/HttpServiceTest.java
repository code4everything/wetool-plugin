package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.junit.Test;

public class HttpServiceTest {

    @Test
    public void testExportHttp() {
        HttpService.exportHttp(8080, "post /api/hello", new HttpApiHandler() {
            @Override
            public Object handleApi(HttpRequest req, HttpResponse resp, JSONObject params, JSONObject body) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("a", params.getInteger("a"));
                jsonObject.put("b", params.getInteger("b"));
                return jsonObject;
            }
        });

        while (true) {
            ThreadUtil.sleep(1000);
        }
    }
}
