package org.code4everything.wetool.plugin.ftp.client.config;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;
import org.code4everything.wetool.plugin.support.util.WeUtils;

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

    private static final List<String> KEYS = Lists.newArrayList("easeFtp", "EaseFtp", "ease_ftp", "ftp", "FTP");

    private static final long serialVersionUID = 6979297033248219537L;

    /**
     * 初始化时是否打开选项卡，默认：false
     */
    private Boolean showOnStartup;

    private List<FtpInfo> ftps;

    public static FtpConfig getConfig() {
        FtpConfig config = null;
        for (String key : KEYS) {
            if (Objects.isNull(config) || CollUtil.isEmpty(config.getFtps())) {
                config = WeUtils.getConfig().getConfig(key, FtpConfig.class);
            } else {
                break;
            }
        }
        return config;
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
