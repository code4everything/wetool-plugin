package org.code4everything.wetool.plugin.support.event;

import java.util.Date;

/**
 * @author pantao
 * @since 2020/10/25
 */
@FunctionalInterface
public interface EventHandler {

    /**
     * 事件处理器
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生时间
     * @param eventMessage 事件消息，可能为空，请根据不同的eventKey，进行不同的处理
     */
    void handleEvent(String eventKey, Date eventTime, EventMessage eventMessage);
}
