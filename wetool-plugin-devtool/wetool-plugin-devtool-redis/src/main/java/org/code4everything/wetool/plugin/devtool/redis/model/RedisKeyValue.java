package org.code4everything.wetool.plugin.devtool.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * @author pantao
 * @since 2020/10/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RedisKeyValue {

    @NonNull
    private String key;

    @NonNull
    private String value;

    @NonNull
    private Integer expire;

    @NonNull
    private String type;
}
