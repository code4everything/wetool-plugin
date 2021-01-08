package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.code4everything.wetool.plugin.support.event.message.DynamicEventMessage;
import org.code4everything.wetool.plugin.support.event.message.MouseListenerEventMessage;

/**
 * @author pantao
 * @since 2021/1/8
 */
public abstract class BaseDynamicEventHandler extends BaseEventHandler<MouseListenerEventMessage> {

    @Override
    public boolean shouldHandle(EventMessage eventMessage) {
        return eventMessage instanceof DynamicEventMessage;
    }
}
