package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSON;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisVO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExplorerControllerTest {

    @Test
    public void compare() {
        List<JedisVO> list = new ArrayList<>();
        list.add(new JedisVO().setType("set").setKey("test:set:2"));
        list.add(new JedisVO().setType("set").setKey("test:set:1"));
        list.add(new JedisVO().setType("string").setKey("test:string:2"));
        list.add(new JedisVO().setType("string").setKey("test:string:2:1"));
        list.add(new JedisVO().setType("container").setKey("test"));
        list.add(new JedisVO().setType("container").setKey("test:container"));

        list.sort(new ExplorerController());

        Console.log(JSON.toJSONString(list, true));

        String s="{\"createTime\":1542447999993,\"email\":\"tao@zhazhapan.com\"," +
                "\"id\":\"a02aa148493f4334b0f032be589561f5\",\"loginTime\":1573823003750," +
                "\"password\":\"91991083660b272c45d841afb32b5aa5\",\"status\":\"7\",\"username\":\"pantao\"}";

        Console.log(s.length());
    }
}
