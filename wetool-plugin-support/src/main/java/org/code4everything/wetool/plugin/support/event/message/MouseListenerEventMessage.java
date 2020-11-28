package org.code4everything.wetool.plugin.support.event.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.jnativehook.mouse.NativeMouseEvent;

/**
 * @author pantao
 * @since 2020/11/28
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class MouseListenerEventMessage implements EventMessage {

    private NativeMouseEvent mouseEvent;

    public static MouseListenerEventMessage of(NativeMouseEvent mouseEvent) {
        return new MouseListenerEventMessage(mouseEvent);
    }
}
