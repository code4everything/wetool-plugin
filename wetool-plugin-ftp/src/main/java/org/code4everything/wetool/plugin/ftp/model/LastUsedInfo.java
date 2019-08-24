package org.code4everything.wetool.plugin.ftp.model;

import lombok.Data;
import lombok.Generated;
import lombok.ToString;

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

    private String downloadFtpName;

    private String uploadFtpName;

    private String remoteSaveDir;

    private String uploadFile;

    private String downloadFile;

    private String localSaveDir;

    private Set<String> ftpNames;

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
    public String getDownloadFtpName() {
        return Objects.isNull(downloadFtpName) ? uploadFtpName : downloadFtpName;
    }

    @Generated
    public String getUploadFtpName() {
        return Objects.isNull(uploadFtpName) ? downloadFtpName : uploadFtpName;
    }
}
