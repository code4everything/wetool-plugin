package org.code4everything.wetool.plugin.devtool.redis.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

import java.util.Objects;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ConnectionConfiguration implements BaseBean {

    /**
     * using host if is empty, this is the unique key
     */
    private String alias;

    /**
     * localhost is default
     */
    private String host;

    /**
     * 6379 is default
     */
    private Integer port;

    /**
     * should be empty
     */
    private String password;

    /**
     * false is default
     */
    private Boolean initialConnect;

    /**
     * the redis db list
     */
    private Set<Integer> dbs;

    @Generated
    public String getAlias() {
        return StrUtil.emptyToDefault(alias, getHost());
    }

    @Generated
    public String getHost() {
        return StrUtil.emptyToDefault(host, "localhost");
    }

    @Generated
    public int getPort() {
        return ObjectUtil.defaultIfNull(port, 6379);
    }

    @Generated
    public boolean getInitialConnect() {
        return ObjectUtil.defaultIfNull(initialConnect, false);
    }

    @Generated
    public Set<Integer> getDbs() {
        return ObjectUtil.defaultIfNull(dbs, CollUtil.newHashSet(0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionConfiguration that = (ConnectionConfiguration) o;
        return getAlias().equals(that.getAlias());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAlias());
    }
}
