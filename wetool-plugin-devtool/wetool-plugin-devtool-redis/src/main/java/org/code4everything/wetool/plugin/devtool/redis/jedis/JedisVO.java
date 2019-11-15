package org.code4everything.wetool.plugin.devtool.redis.jedis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
}
