package org.code4everything.wetool.plugin.devtool.redis.jedis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JedisVO {

    private String key;

    private String type;

    private String size;

    public boolean isContainer() {
        return "container".equals(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JedisVO jedisVO = (JedisVO) o;
        if (isContainer()) {
            return Objects.equals(key, jedisVO.key) && Objects.equals(type, jedisVO.type);
        }
        return Objects.equals(key, jedisVO.key);
    }

    @Override
    public int hashCode() {
        return isContainer() ? Objects.hash(key, type) : Objects.hash(type);
    }
}
