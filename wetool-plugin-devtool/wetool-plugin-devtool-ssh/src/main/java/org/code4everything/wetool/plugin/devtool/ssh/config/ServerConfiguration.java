package org.code4everything.wetool.plugin.devtool.ssh.config;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/11/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ServerConfiguration implements BaseBean {

    /**
     * server alias, unique
     */
    private String alias;

    /**
     * server host, default: localhost
     */
    private String host;

    /**
     * server port, default: 22
     */
    private Integer port;

    /**
     * ssh username, default: root
     */
    private String username;

    /**
     * ssh password, default: root
     */
    private String password;

    private String charset;

    private List<ServerSyncConfiguration> syncs;

    public Charset getCharset() {
        return CharsetUtil.charset(charset);
    }

    public String getAlias() {
        return StrUtil.emptyToDefault(alias, getHost());
    }

    public String getHost() {
        return StrUtil.emptyToDefault(host, "localhost");
    }

    public Integer getPort() {
        return ObjectUtil.defaultIfNull(port, 22);
    }

    public String getUsername() {
        return StrUtil.emptyToDefault(username, "root");
    }

    public String getPassword() {
        return StrUtil.emptyToDefault(password, "root");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerConfiguration that = (ServerConfiguration) o;
        return getAlias().equals(that.getAlias());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAlias());
    }
}
