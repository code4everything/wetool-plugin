package org.code4everything.wetool.plugin.support.event.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;

/**
 * @author pantao
 * @since 2020/10/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QuickStartEventMessage implements EventMessage {

    private String location;

    public static QuickStartEventMessage of(String location) {
        return new QuickStartEventMessage(location);
    }
}
