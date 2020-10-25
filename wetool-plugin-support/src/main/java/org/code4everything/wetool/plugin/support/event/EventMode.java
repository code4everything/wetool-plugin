package org.code4everything.wetool.plugin.support.event;

/**
 * @author pantao
 * @since 2020/10/25
 */
public enum EventMode {
    /**
     * 事件只能被一个订阅者消费
     */
    SINGLE_SUB,

    /**
     * 事件可以被多个订阅者消费
     */
    MULTI_SUB
}
