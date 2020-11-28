package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.code4everything.wetool.plugin.support.event.message.MouseListenerEventMessage;

/**
 * @author pantao
 * @since 2020/11/28
 */
public abstract class BaseMouseEventHandler extends BaseEventHandler<MouseListenerEventMessage> {

    @Override
    public boolean shouldHandle(EventMessage eventMessage) {
        return eventMessage instanceof MouseListenerEventMessage;
    }
}
