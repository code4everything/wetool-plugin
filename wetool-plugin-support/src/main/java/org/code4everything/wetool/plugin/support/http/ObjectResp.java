package org.code4everything.wetool.plugin.support.http;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author pantao
 * @since 2020/12/6
 */
public class ObjectResp extends JSONObject {

    public static ObjectResp of() {
        return new ObjectResp();
    }

    public static ObjectResp of(String key, Object value) {
        return of().put(key, value);
    }

    @Override
    public ObjectResp put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public ObjectResp put(Map<String, Object> map) {
        super.putAll(map);
        return this;
    }
}
