package org.code4everything.wetool.plugin.support.event.message;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.lang.reflect.Type;

/**
 * @author pantao
 * @since 2020/11/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KeyboardListenerEventMessage implements EventMessage {

    @JSONField(deserializeUsing = NativeKeyEventDeserializer.class)
    private NativeKeyEvent keyEvent;

    public static KeyboardListenerEventMessage of(NativeKeyEvent keyEvent) {
        return new KeyboardListenerEventMessage(keyEvent);
    }

    public String toKeyText() {
        return NativeKeyEvent.getKeyText(keyEvent.getKeyCode());
    }

    public static class NativeKeyEventDeserializer implements ObjectDeserializer {

        @Override
        @SuppressWarnings("unchecked")
        public NativeKeyEvent deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONObject jsonObject = parser.parseObject();
            int id = jsonObject.getIntValue("iD");
            if (id == 0) {
                id = jsonObject.getIntValue("id");
            }
            NativeKeyEvent event = new NativeKeyEvent(id, 0, 0, 0, '0');
            BeanUtil.copyProperties(jsonObject, event);
            return event;
        }

        @Override
        public int getFastMatchToken() {
            return 0;
        }
    }
}
