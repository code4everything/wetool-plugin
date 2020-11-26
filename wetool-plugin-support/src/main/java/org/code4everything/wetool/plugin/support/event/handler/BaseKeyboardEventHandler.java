package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.code4everything.wetool.plugin.support.event.message.KeyboardListenerEventMessage;

/**
 * @author pantao
 * @since 2020/11/26
 */
public abstract class BaseKeyboardEventHandler extends BaseEventHandler<KeyboardListenerEventMessage> {

    @Override
    public final boolean shouldHandle(EventMessage eventMessage) {
        return eventMessage instanceof KeyboardListenerEventMessage;
    }
}
