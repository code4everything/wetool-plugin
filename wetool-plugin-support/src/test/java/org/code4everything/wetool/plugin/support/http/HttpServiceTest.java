package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;

public class HttpServiceTest {

    public static void main(String[] args) {
        HttpService.setDefaultPort(58189);
        HttpService.exportHttp("get/api/hello", (req, resp, params, body) -> {
            ArgRequires.notEmpty(params, "a");
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
