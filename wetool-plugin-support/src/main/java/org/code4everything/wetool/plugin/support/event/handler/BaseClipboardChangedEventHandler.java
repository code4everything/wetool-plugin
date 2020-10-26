package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.code4everything.wetool.plugin.support.event.message.ClipboardChangedEventMessage;

/**
 * @author pantao
 * @since 2020/10/26
 */
public abstract class BaseClipboardChangedEventHandler extends BaseEventHandler<ClipboardChangedEventMessage> {

    @Override
    public final boolean shouldHandle(EventMessage eventMessage) {
        return eventMessage instanceof ClipboardChangedEventMessage;
    }
}
