package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;

public class HttpServiceTest {

    public static void main(String[] args) {
        HttpService.exportHttp(HttpService.DEFAULT_PORT, "post/api/hello", (req, resp, params, body) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("a", params.getInteger("a"));
            jsonObject.put("b", params.getInteger("b"));
            return jsonObject;
        });

        while (true) {
            ThreadUtil.sleep(1000);
        }
    }
}
