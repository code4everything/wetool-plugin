package org.code4everything.wetool.plugin.ftp.config;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/26
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FtpInfo implements BaseBean, Serializable {

    private static final long serialVersionUID = 1176607642640475618L;

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
     * 是否默认为默认的FTP，默认：false
     */
    private Boolean select;

    /**
     * 是否在使用时才开始连接ftp，而不是初始化就连接，默认：true
     */
    private Boolean lazyConnect;

    public boolean isInitConnect() {
        return Boolean.FALSE.equals(lazyConnect);
    }

    @Generated
    public String getHost() {
        return StrUtil.emptyToDefault(host, "127.0.0.1");
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
    public boolean getSelect() {
        return Boolean.TRUE.equals(select);
    }
}
