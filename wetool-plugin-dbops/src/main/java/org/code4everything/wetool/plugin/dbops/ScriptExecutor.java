package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class ScriptExecutor {

    public static void execute(List<List<String>> codeBlocks, JSONObject args) {
        Console.log(codeBlocks);
    }
}
