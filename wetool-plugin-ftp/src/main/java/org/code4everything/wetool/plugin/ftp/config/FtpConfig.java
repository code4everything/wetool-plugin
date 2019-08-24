package org.code4everything.wetool.plugin.ftp.config;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/24
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FtpConfig implements BaseBean {

    /**
     * 自定义FTP连接名
     */
    @NonNull
    private String name;

    /**
     * 主机，默认：127.0.0.1
     */
    private String host;

    /**
     * 端口，默认：21
     */
    private Integer port;

    /**
     * 是否匿名登录FTP，默认：false
     */
    private Boolean anonymous;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 编码，默认：utf-8
     */
    private String charset;

    /**
     * 是否自动重连，默认：false
     */
    private Boolean reconnect;

    @Generated
    public String getHost() {
        return StrUtil.isEmpty(host) ? "127.0.0.1" : host;
    }

    @Generated
    public int getPort() {
        return Objects.isNull(port) ? 21 : port;
    }

    @Generated
    public boolean getAnonymous() {
        return Boolean.TRUE.equals(anonymous);
    }

    @Generated
    public Charset getCharset() {
        return CharsetUtil.charset(charset);
    }

    @Generated
    public boolean getReconnect() {
        return Boolean.TRUE.equals(reconnect);
    }
}
