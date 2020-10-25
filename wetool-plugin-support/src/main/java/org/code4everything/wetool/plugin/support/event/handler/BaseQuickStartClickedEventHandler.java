package org.code4everything.wetool.plugin.support.event.handler;

import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.code4everything.wetool.plugin.support.event.message.QuickStartEventMessage;

/**
 * @author pantao
 * @since 2020/10/25
 */
public abstract class BaseQuickStartClickedEventHandler extends BaseEventHandler<QuickStartEventMessage> {

    @Override
    public final boolean shouldHandle(EventMessage eventMessage) {
        return eventMessage instanceof QuickStartEventMessage;
    }
}
