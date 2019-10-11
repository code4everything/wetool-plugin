package org.code4everything.wetool.plugin.ftp.client.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.*;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/24
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FtpConfig implements BaseBean, Serializable {

    private static final String PATH = "conf" + File.separator + "ftp-client-config.json";

    private static final long serialVersionUID = 6979297033248219537L;

    /**
     * 初始化时是否打开选项卡，默认：false
     */
    private Boolean showOnStartup;

    private List<FtpInfo> ftps;

    public static FtpConfig getConfig() {
        String path = StrUtil.emptyToDefault(WeUtils.parsePathByOs(PATH), FileUtils.currentWorkDir(PATH));
        if (FileUtil.exist(path)) {
            return JSON.parseObject(FileUtil.readUtf8String(path), FtpConfig.class);
        }
        return null;
    }

    public static List<FtpInfo> getFtps(FtpConfig ftpConfig) {
        if (Objects.isNull(ftpConfig) || Objects.isNull(ftpConfig.getFtps())) {
            return new ArrayList<>();
        }
        return ftpConfig.getFtps();
    }

    public void addFtp(FtpInfo ftpInfo) {
        if (Objects.isNull(ftps)) {
            ftps = new ArrayList<>();
        }
        ftps.add(ftpInfo);
    }

    @Generated
    public boolean getShowOnStartup() {
        return Boolean.TRUE.equals(showOnStartup);
    }
}
