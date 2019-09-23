package org.code4everything.wetool.plugin.ftp.server.config;

import cn.hutool.core.util.ObjectUtil;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;
import java.util.List;

/**
 * @author pantao
 * @since 2019/9/23
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FtpServerConfig implements BaseBean, Serializable {

    private static final long serialVersionUID = 6202224313915276631L;

    /**
     * 初始化是否启动，默认：false
     */
    private Boolean startOnStartup;

    /**
     * 监听端口，默认：21
     */
    private Integer port;

    private List<FtpServerUser> users;

    public Integer getPort() {
        return ObjectUtil.defaultIfNull(port, 21);
    }

    @Generated
    public Boolean getStartOnStartup() {
        return ObjectUtil.defaultIfNull(startOnStartup, false);
    }
}
