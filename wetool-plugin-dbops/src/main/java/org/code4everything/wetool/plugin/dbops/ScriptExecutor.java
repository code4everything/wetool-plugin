package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.lang.Console;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * @author pantao
 * @since 2020/11/11
 */
@UtilityClass
public class ScriptExecutor {

    public static void execute(String dbName, String codes, Map<String, Object> args) {
        Console.log(codes);
    }
}
