package org.code4everything.wetool.plugin.support.event;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/10/25
 */
@Data
@Accessors(chain = true)
public class EventPublisher {

    private final String eventKey;

    private EventMessage eventMessage;

    public EventPublisher(String eventKey) {
        Objects.requireNonNull(eventKey);
        this.eventKey = eventKey;
    }

    public boolean publishEvent(Date eventTime) {
        return EventCenter.publishEvent(getEventKey(), eventTime, getEventMessage());
    }

    public boolean publishEvent() {
        return EventCenter.publishEvent(getEventKey(), DateUtil.date(), getEventMessage());
    }
}
