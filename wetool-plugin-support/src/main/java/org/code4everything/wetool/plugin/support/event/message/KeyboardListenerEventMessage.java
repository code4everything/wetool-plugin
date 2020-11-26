package org.code4everything.wetool.plugin.support.event.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.jnativehook.keyboard.NativeKeyEvent;

/**
 * @author pantao
 * @since 2020/11/26
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class KeyboardListenerEventMessage implements EventMessage {

    private NativeKeyEvent keyEvent;
}
