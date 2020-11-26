package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventHandler;
import org.code4everything.wetool.plugin.support.event.EventMessage;

import java.util.Date;

/**
 * @author pantao
 * @since 2020/10/25
 */
public abstract class BaseNoMessageEventHandler implements EventHandler {

    @Override
    public final void handleEvent(String eventKey, Date eventTime, EventMessage eventMessage) {
        handleEvent0(eventKey, eventTime);
    }

    /**
     * 事件处理器
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生时间
     */
    public abstract void handleEvent0(String eventKey, Date eventTime);
}
