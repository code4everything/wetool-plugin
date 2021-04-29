package org.code4everything.wetool.plugin.dbops.script;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author pantao
 * @since 2020/11/11
 */
@RequiredArgsConstructor
public enum ExecuteTypeEnum {


    /**
     * 手动触发
     */
    HANDY("手动触发"),

    /**
     * 事件触发
     */
    EVENT("事件触发"),

    /**
     * cron表达式
     */
    CRON("定时任务");

    @Getter
    private final String desc;

    public static ExecuteTypeEnum parseByDesc(String desc) {
        return Arrays.stream(values()).filter(e -> e.getDesc().equals(desc)).findFirst().orElse(HANDY);
    }
}
