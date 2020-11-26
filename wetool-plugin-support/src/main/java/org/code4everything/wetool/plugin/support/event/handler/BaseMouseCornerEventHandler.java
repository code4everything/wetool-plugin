package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.code4everything.wetool.plugin.support.event.message.MouseCornerEventMessage;

/**
 * @author pantao
 * @since 2020/11/2
 */
public abstract class BaseMouseCornerEventHandler extends BaseEventHandler<MouseCornerEventMessage> {

    @Override
    public final boolean shouldHandle(EventMessage eventMessage) {
        return eventMessage instanceof MouseCornerEventMessage;
    }
}
