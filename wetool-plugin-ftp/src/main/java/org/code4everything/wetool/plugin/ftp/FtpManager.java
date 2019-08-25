package org.code4everything.wetool.plugin.ftp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.ftp.Ftp;
import javafx.scene.control.ComboBox;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.code4everything.boot.base.function.BooleanFunction;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.controller.FtpController;
import org.code4everything.wetool.plugin.ftp.model.FtpDownload;
import org.code4everything.wetool.plugin.ftp.model.FtpUpload;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author pantao
 * @since 2019/8/24
 */
@Slf4j
@UtilityClass
public class FtpManager {

    private static final Queue<FtpUpload> UPLOAD_QUEUE = new LinkedBlockingQueue<>();

    private static final Queue<FtpDownload> DOWNLOAD_QUEUE = new LinkedBlockingQueue<>();

    private static final LastUsedInfo USED_INFO = LastUsedInfo.getInstance();

    private static final int RETRIES = 3;

    public static boolean delete(ComboBox<String> ftpName, String file) {
        try {
            if (getFtp(ftpName).delFile(file)) {
                log.info("delete file '{}' success", file);
                return true;
            } else {
                log.error("delete file '{}' failed", file);
            }
        } catch (Exception e) {
            log.error("delete ftp file error", e);
        }
        return false;
    }

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

    public static void download(ComboBox<String> ftpName, List<String> files, File path) {
        boolean shouldExe = DOWNLOAD_QUEUE.isEmpty();
        String name = ftpName.getSelectionModel().getSelectedItem();
        if (CollUtil.isNotEmpty(files)) {
            // 锁住队列，并将文件添加队列
            synchronized (DOWNLOAD_QUEUE) {
                shouldExe = DOWNLOAD_QUEUE.isEmpty();
                files.forEach(file -> DOWNLOAD_QUEUE.offer(new FtpDownload(name, file, path)));
            }
        }
        FtpController controller = BeanFactory.get(FtpController.class);
        if (shouldExe) {
            // 异步下载
            ThreadUtil.execute(() -> {
                while (!DOWNLOAD_QUEUE.isEmpty()) {
                    FtpDownload ftp = DOWNLOAD_QUEUE.poll();
                    String absPath = path.getAbsolutePath();
                    if (getFtp(ftp.getName()).exist(ftp.getFile())) {
                        controller.updateStatus("download '{}' to '{}'", ftp.getFile(), absPath);
                        try {
                            getFtp(ftp.getName()).download(ftp.getFile(), path);
                            log.info("'{}' download to '{}' success", ftp.getFile(), absPath);
                        } catch (Exception e) {
                            log.error("download failed", e);
                        }
                    } else {
                        log.error("download to path[{}] failed: file '{}' not exists", absPath, ftp.getFile());
                    }
                }
                controller.updateStatus("");
            });
        }
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

    public static void upload(ComboBox<String> ftpName, String path, List<File> files) {
        boolean shouldExe = UPLOAD_QUEUE.isEmpty();
        String name = ftpName.getSelectionModel().getSelectedItem();
        if (CollUtil.isNotEmpty(files)) {
            // 锁住队列，并将文件添加队列
            synchronized (UPLOAD_QUEUE) {
                shouldExe = UPLOAD_QUEUE.isEmpty();
                files.forEach(file -> UPLOAD_QUEUE.offer(new FtpUpload(name, path, file)));
            }
        }
        FtpController controller = BeanFactory.get(FtpController.class);
        if (shouldExe) {
            // 异步上传
            ThreadUtil.execute(() -> {
                while (!UPLOAD_QUEUE.isEmpty()) {
                    FtpUpload ftp = UPLOAD_QUEUE.poll();
                    String absPath = ftp.getFile().getAbsolutePath();
                    if (ftp.getFile().exists()) {
                        controller.updateStatus("upload '{}' to '{}'", absPath, path);
                        try {
                            boolean res = getFtp(ftp.getName()).upload(ftp.getPath(), ftp.getFile());
                            if (res) {
                                log.info("'{}' upload to '{}' success", absPath, path);
                            } else {
                                log.error("'{}' upload to '{}' failed", absPath, path);
                            }
                        } catch (Exception e) {
                            log.error("upload failed", e);
                        }
                    } else {
                        log.error("upload to path[{}] failed: file '{}' not exists", ftp.getPath(), absPath);
                    }
                }
                controller.updateStatus("");
            });
        }
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

    public static Ftp getFtp(ComboBox<String> ftpName) {
        return getFtp(ftpName.getSelectionModel().getSelectedItem());
    }

    public static Ftp getFtp(String name) {
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
