package org.code4everything.wetool.plugin.ftp;

import cn.hutool.extra.ftp.Ftp;
import javafx.scene.control.ComboBox;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.code4everything.boot.base.function.BooleanFunction;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/24
 */
@Slf4j
@UtilityClass
public class FtpManager {

    private static final LastUsedInfo USED_INFO = LastUsedInfo.getInstance();

    private static final int RETRIES = 3;

    public static void download(ComboBox<String> ftpName, String file, File path) {
        USED_INFO.setLocalSaveDir(path.getAbsolutePath());
        USED_INFO.setDownloadFile(file);
        retry(() -> {
            getFtp(ftpName).download(file, path);
            log.info("'{}' download to '{}' success", file, path.getAbsolutePath());
            FxDialogs.showSuccess();
            return true;
        });
    }

    public static boolean exists(ComboBox<String> ftpName, String file) {
        return getFtp(ftpName).exist(file);
    }

    public static List<String> listChildren(ComboBox<String> ftpName, String path, boolean containsFile) {
        FTPFile[] ftpFiles = getFtp(ftpName).lsFiles(path);
        List<String> dirs = new ArrayList<>();
        // 忽略 . 和 ..
        for (int i = 2; i < ftpFiles.length; i++) {
            FTPFile ftpFile = ftpFiles[i];
            if (containsFile || ftpFile.isDirectory()) {
                dirs.add(path + ftpFile.getName());
            }
        }
        log.info("list file from ftp path: {}, result: {}", path, dirs);
        return dirs;
    }

    public static void upload(ComboBox<String> ftpName, String path, File file) {
        USED_INFO.setRemoteSaveDir(path);
        USED_INFO.setUploadFile(file.getAbsolutePath());
        retry(() -> {
            if (getFtp(ftpName).upload(path, file)) {
                log.info("'{}' upload to '{}' success", file.getAbsoluteFile(), path);
                FxDialogs.showSuccess();
                return true;
            }
            return false;
        });
    }

    public static boolean isFtpNotSelected(ComboBox<String> comboBox) {
        return !USED_INFO.getFtpNames().contains(comboBox.getSelectionModel().getSelectedItem());
    }

    static String generateConfigKey(String ftpName) {
        return FtpConsts.AUTHOR + FtpConsts.NAME + ftpName;
    }

    private static void retry(BooleanFunction func) {
        Exception ex = null;
        for (int retry = RETRIES; retry > 0; retry--) {
            try {
                if (func.call()) {
                    return;
                }
            } catch (Exception e) {
                ex = e;
            }
        }
        if (Objects.isNull(ex)) {
            FxDialogs.showError(FtpConsts.FTP_ERROR);
        } else {
            FxDialogs.showException(FtpConsts.FTP_ERROR, ex);
        }
    }

    private static Ftp getFtp(ComboBox<String> ftpName) {
        String name = ftpName.getSelectionModel().getSelectedItem();
        Ftp ftp = BeanFactory.get(generateFtpKey(name));
        if (Objects.isNull(ftp)) {
            FtpConfig g = BeanFactory.get(generateConfigKey(name));
            if (g.getAnonymous()) {
                ftp = new Ftp(g.getHost(), g.getPort(), "anonymous", "", g.getCharset());
            } else {
                ftp = new Ftp(g.getHost(), g.getPort(), g.getUsername(), g.getPassword(), g.getCharset());
            }
            if (g.getReconnect()) {
                ftp.reconnectIfTimeout();
            }
            BeanFactory.register(generateFtpKey(name), ftp);
            log.info("ftp[{}] connected", name);
        }
        USED_INFO.setFtpName(name);
        return ftp;
    }

    private String generateFtpKey(String name) {
        return FtpConsts.AUTHOR + FtpConsts.NAME + name + FtpConsts.FTP;
    }
}
