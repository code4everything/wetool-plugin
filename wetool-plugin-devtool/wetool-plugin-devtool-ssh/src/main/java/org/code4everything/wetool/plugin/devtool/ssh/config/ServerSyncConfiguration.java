package org.code4everything.wetool.plugin.devtool.ssh.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

/**
 * @author pantao
 * @since 2019/12/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ServerSyncConfiguration implements BaseBean {

    private Boolean enable;

    /**
     * remote directory, no recursive
     */
    private String remoteDir;

    private String fileFilter;

    private String localDir;

    /**
     * synchronize delay, default: 60s
     */
    private Integer delay;

    @Generated
    public boolean getEnable() {
        return BooleanUtil.isTrue(enable) && StrUtil.isNotEmpty(remoteDir);
    }

    @Generated
    public String getLocalDir() {
        return StrUtil.emptyToDefault(localDir, FileUtil.getUserHomePath());
    }

    @Generated
    public int getDelay() {
        return ObjectUtil.defaultIfNull(delay, 60);
    }
}
