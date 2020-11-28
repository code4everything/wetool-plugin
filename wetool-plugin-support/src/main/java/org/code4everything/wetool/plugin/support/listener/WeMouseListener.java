package org.code4everything.wetool.plugin.support.listener;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.event.message.MouseListenerEventMessage;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

/**
 * @author pantao
 * @since 2020/11/28
 */
@Slf4j
public class WeMouseListener implements NativeMouseInputListener {

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        log.debug("mouse clicked");
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        MouseListenerEventMessage message = MouseListenerEventMessage.of(nativeMouseEvent);
        EventCenter.publishEvent(EventCenter.EVENT_MOUSE_PRESSED, DateUtil.date(), message);
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        MouseListenerEventMessage message = MouseListenerEventMessage.of(nativeMouseEvent);
        EventCenter.publishEvent(EventCenter.EVENT_MOUSE_RELEASED, DateUtil.date(), message);
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        MouseListenerEventMessage message = MouseListenerEventMessage.of(nativeMouseEvent);
        EventCenter.publishEvent(EventCenter.EVENT_MOUSE_MOTION, DateUtil.date(), message);
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        log.debug("mouse dragged");
    }
}
