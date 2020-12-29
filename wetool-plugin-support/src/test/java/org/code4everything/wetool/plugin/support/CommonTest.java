package org.code4everything.wetool.plugin.support;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSON;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.junit.Test;

/**
 * @author pantao
 * @since 2020/12/29
 */
public class CommonTest {

    @Test
    public void test() {
        WeConfig config = JSON.parseObject("{}", WeConfig.class);
        Console.log(JSON.toJSONString(config, true));
        System.out.println(config.getAutoWrap());
    }
}
