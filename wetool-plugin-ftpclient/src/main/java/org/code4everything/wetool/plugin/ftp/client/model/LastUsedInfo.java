package org.code4everything.wetool.plugin.ftp.client.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Generated;
import lombok.ToString;
import org.code4everything.boot.base.constant.StringConsts;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/8/24
 */
@Data
@ToString
public class LastUsedInfo {

    private static final LastUsedInfo LAST_USED_INFO = new LastUsedInfo();

    private String ftpName;

    private String remoteSaveDir;

    private String uploadFile;

    private String downloadFile;

    private String localSaveDir;

    private Set<String> ftpNames;

    private String localDir;

    private String remoteDir;

    private LastUsedInfo() {}

    public static LastUsedInfo getInstance() {
        return LAST_USED_INFO;
    }

    public void addFtpName(String ftpName) {
        if (Objects.isNull(ftpNames)) {
            ftpNames = new HashSet<>(4, 1);
        }
        ftpNames.add(ftpName);
    }

    @Generated
    public String getLocalDir() {
        return StrUtil.emptyToDefault(localDir, FileUtil.getUserHomePath());
    }

    @Generated
    public String getRemoteDir() {
        return StrUtil.emptyToDefault(remoteDir, StringConsts.Sign.SLASH);
    }
}
