package org.code4everything.wetool.plugin.support.event.message;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.druid.JdbcOpsUtils;
import org.code4everything.wetool.plugin.support.event.EventMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2021/1/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DynamicEventMessage implements EventMessage {

    private Map<String, Object> map;

    public static DynamicEventMessage of(String key, Object value) {
        return of(Map.of(key, value));
    }

    public static DynamicEventMessage of(Map<String, Object> map) {
        return new DynamicEventMessage(map);
    }

    public Object get(String key) {
        return getMap().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key) {
        return (T) get(key);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        return JdbcOpsUtils.fastCast(get(key), clazz);
    }

    public String getString(String key) {
        return ObjectUtil.toString(get(key));
    }

    public Map<String, Object> getMap() {
        if (Objects.isNull(map)) {
            map = new HashMap<>(16);
        }
        return map;
    }
}
