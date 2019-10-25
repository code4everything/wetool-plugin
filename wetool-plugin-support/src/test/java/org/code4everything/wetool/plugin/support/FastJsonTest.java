package org.code4everything.wetool.plugin.support;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

/**
 * @author pantao
 * @since 2019/10/25
 */
public class FastJsonTest {

    @Test
    public void testDeserializeWithComment() {
        String file = "/json-with-comment.json";
        JSONObject object = JSON.parseObject(IoUtil.read(FastJsonTest.class.getResourceAsStream(file), "utf-8"));
        Console.log(object);
    }
}
