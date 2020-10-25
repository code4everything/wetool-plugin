package org.code4everything.wetool.plugin.support.event.handler;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.event.EventHandler;
import org.code4everything.wetool.plugin.support.event.EventMessage;

import java.util.Date;

/**
 * @author pantao
 * @since 2020/10/25
 */
@Slf4j
public abstract class BaseEventHandler<T extends EventMessage> implements EventHandler {

    @Override
    @SuppressWarnings("unchecked")
    public final void handleEvent(String eventKey, Date eventTime, EventMessage eventMessage) {
        if (shouldHandle(eventMessage)) {
            handleEvent0(eventKey, eventTime, (T) eventMessage);
        } else {
            String errMsg = "invalid event message, event key: {}, event time: {}, event message: {}";
            log.error(errMsg, eventKey, DateUtil.format(eventTime, DatePattern.NORM_DATETIME_MS_FORMAT), eventMessage);
        }
    }

    /**
     * 判断消息类型是否正确
     *
     * @param eventMessage 事件消息
     *
     * @return 是否可以处理该事件
     */
    public abstract boolean shouldHandle(EventMessage eventMessage);

    /**
     * 事件处理器
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生时间
     * @param eventMessage 事件消息
     */
    public abstract void handleEvent0(String eventKey, Date eventTime, T eventMessage);
}
