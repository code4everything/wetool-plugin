package org.code4everything.wetool.plugin.ftp.config;

import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

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

    public static final String KEY_CAMEL = "easeFtp";

    public static final String KEY_LOWER = "ease-ftp";

    private static final long serialVersionUID = 6979297033248219537L;

    private Boolean showOnStartup;

    private List<FtpInfo> ftps;

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
