package org.code4everything.wetool.plugin.support.event.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;

/**
 * @author pantao
 * @since 2020/10/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClipboardChangedEventMessage implements EventMessage {

    private String clipboardText;

    public static ClipboardChangedEventMessage of(String clipboardText) {
        return new ClipboardChangedEventMessage(clipboardText);
    }
}
