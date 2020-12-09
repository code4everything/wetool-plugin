package org.code4everything.wetool.plugin.support.event.message;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.jnativehook.mouse.NativeMouseEvent;

import java.lang.reflect.Type;

/**
 * @author pantao
 * @since 2020/11/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MouseListenerEventMessage implements EventMessage {

    @JSONField(deserializeUsing = NativeMouseEventDeserializer.class)
    private NativeMouseEvent mouseEvent;

    public static MouseListenerEventMessage of(NativeMouseEvent mouseEvent) {
        return new MouseListenerEventMessage(mouseEvent);
    }

    public static class NativeMouseEventDeserializer implements ObjectDeserializer {

        @Override
        @SuppressWarnings("unchecked")
        public NativeMouseEvent deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONObject jsonObject = parser.parseObject();
            int id = jsonObject.getIntValue("iD");
            if (id == 0) {
                id = jsonObject.getIntValue("id");
            }

            int x = jsonObject.getIntValue("x");
            int y = jsonObject.getIntValue("y");
            int clickCount = jsonObject.getIntValue("clickCount");
            int button = jsonObject.getIntValue("button");
            return new NativeMouseEvent(id, 0, x, y, clickCount, button);
        }

        @Override
        public int getFastMatchToken() {
            return 0;
        }
    }
}
