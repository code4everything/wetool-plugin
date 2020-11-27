package org.code4everything.wetool.plugin.ftp.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import javafx.scene.control.ComboBox;
import javafx.util.Pair;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.boot.base.function.BooleanFunction;
import org.code4everything.wetool.plugin.ftp.client.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.client.config.FtpInfo;
import org.code4everything.wetool.plugin.ftp.client.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.client.controller.FtpController;
import org.code4everything.wetool.plugin.ftp.client.hutool.Ftp;
import org.code4everything.wetool.plugin.ftp.client.model.FtpDownload;
import org.code4everything.wetool.plugin.ftp.client.model.FtpUpload;
import org.code4everything.wetool.plugin.ftp.client.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;
import java.util.*;
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

    public static boolean delete(ComboBox<String> ftpName, String file, boolean isDir) {
        try {
            boolean res = isDir ? getFtp(ftpName).delDir(file) : getFtp(ftpName).delFile(file);
            if (res) {
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

    public static void download(ComboBox<String> ftpName, List<Pair<String, Boolean>> files, File path) {
        LastUsedInfo.getInstance().setLocalDir(path.getAbsolutePath());
        boolean shouldExe = DOWNLOAD_QUEUE.isEmpty();
        String name = ftpName.getSelectionModel().getSelectedItem();
        if (CollUtil.isNotEmpty(files)) {
            // 锁住队列，并将文件添加队列
            synchronized (DOWNLOAD_QUEUE) {
                shouldExe = DOWNLOAD_QUEUE.isEmpty();
                files.forEach(f -> DOWNLOAD_QUEUE.offer(new FtpDownload(name, f.getKey(), f.getValue(), path)));
            }
        }
        FtpController controller = BeanFactory.getViewObject(FtpConsts.TAB_ID + FtpConsts.TAB_NAME);
        if (shouldExe) {
            // 异步下载
            WeUtils.execute(() -> {
                while (!DOWNLOAD_QUEUE.isEmpty()) {
                    FtpDownload ftp = DOWNLOAD_QUEUE.poll();
                    String absPath = ftp.getPath().getAbsolutePath();
                    if (!getFtp(ftp.getName()).exist(ftp.getFile())) {
                        log.error("download to path[{}] failed: file '{}' not exists", absPath, ftp.getFile());
                        continue;
                    }
                    if (ftp.getDirectory()) {
                        // 本地保存路径
                        File newPath;
                        if (StrUtil.isEmpty(ftp.getFile()) || StringConsts.Sign.SLASH.equals(ftp.getFile())) {
                            newPath = ftp.getPath();
                        } else {
                            // 解析parent的文件名，获取新的保存路径
                            String parentFolder = ftp.getPath().getAbsolutePath() + File.separator;
                            newPath = new File(parentFolder + FileUtil.getName(ftp.getFile()));
                            newPath.mkdirs();
                        }
                        FTPFile[] ftpFiles = getFtp(ftp.getName()).lsFiles(ftp.getFile());
                        for (int i = 2; i < ftpFiles.length; i++) {
                            FTPFile ftpFile = ftpFiles[i];
                            String fn = StrUtil.addSuffixIfNot(ftp.getFile(), "/") + ftpFile.getName();
                            DOWNLOAD_QUEUE.offer(new FtpDownload(ftp.getName(), fn, ftpFile.isDirectory(), newPath));
                        }
                        continue;
                    }
                    controller.updateDownloadStatus("download '{}' to '{}'", ftp.getFile(), absPath);
                    try {
                        getFtp(ftp.getName()).download(ftp.getFile(), ftp.getPath());
                        log.info("'{}' download to '{}' success", ftp.getFile(), absPath);
                    } catch (Exception e) {
                        log.error("download failed", e);
                    }
                }
                controller.updateDownloadStatus("");
            });
        }
    }

    public static List<String> listChildren(ComboBox<String> ftpName, String path, boolean containsFile) {
        return listChildren(ftpName, path, containsFile, null);
    }

    public static List<String> listChildren(ComboBox<String> ftpName, String path, boolean containsFile, Map<String,
            FTPFile> map) {
        FTPFile[] ftpFiles = getFtp(ftpName).lsFiles(path);
        List<String> dirs = new ArrayList<>();
        // 忽略 . 和 ..
        for (int i = 2; i < ftpFiles.length; i++) {
            FTPFile ftpFile = ftpFiles[i];
            String file = path + ftpFile.getName();
            if (containsFile || ftpFile.isDirectory()) {
                dirs.add(file);
            }
            if (ObjectUtil.isNotNull(map)) {
                map.put(file, ftpFile);
            }
        }
        log.info("list file from ftp path: {}, result: {}", path, dirs);
        return dirs;
    }

    public static void upload(ComboBox<String> ftpName, String path, List<File> files) {
        LastUsedInfo.getInstance().setRemoteDir(path);
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
            WeUtils.execute(() -> {
                while (!UPLOAD_QUEUE.isEmpty()) {
                    FtpUpload ftp = UPLOAD_QUEUE.poll();
                    String absPath = ftp.getFile().getAbsolutePath();
                    if (!ftp.getFile().exists()) {
                        log.error("upload to path[{}] failed: file '{}' not exists", ftp.getPath(), absPath);
                        continue;
                    }
                    if (ftp.getFile().isDirectory()) {
                        String folder = StrUtil.addSuffixIfNot(ftp.getPath(), "/") + ftp.getFile().getName();
                        File[] children = ftp.getFile().listFiles();
                        if (ArrayUtil.isNotEmpty(children)) {
                            for (File file : children) {
                                UPLOAD_QUEUE.offer(new FtpUpload(ftp.getName(), folder, file));
                            }
                        }
                        continue;
                    }
                    controller.updateUploadStatus("upload '{}' to '{}'", absPath, ftp.getPath());
                    try {
                        boolean res = getFtp(ftp.getName()).upload(ftp.getPath(), ftp.getFile());
                        if (res) {
                            log.info("'{}' upload to '{}' success", absPath, ftp.getPath());
                        } else {
                            log.error("'{}' upload to '{}' failed", absPath, ftp.getPath());
                        }
                    } catch (Exception e) {
                        log.error("upload failed", e);
                    }
                }
                controller.updateUploadStatus("");
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

    public static String generateConfigKey(String ftpName) {
        return "config:" + FtpConsts.AUTHOR + FtpConsts.NAME + ftpName;
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
            FtpInfo fo = BeanFactory.get(generateConfigKey(name), () -> {
                FtpConfig.loadConfigAndParse();
                return BeanFactory.get(generateConfigKey(name));
            });
            if (fo.getAnonymous()) {
                ftp = new Ftp(fo.getHost(), fo.getPort(), "anonymous", "", fo.getCharset());
            } else {
                ftp = new Ftp(fo.getHost(), fo.getPort(), fo.getUsername(), fo.getPassword(), fo.getCharset());
            }
            BeanFactory.register(generateFtpKey(name), ftp);
            log.info("ftp[{}] connected", name);
        }
        USED_INFO.setFtpName(name);
        return ftp.reconnectIfTimeout();
    }

    private String generateFtpKey(String name) {
        return "ftp:" + FtpConsts.AUTHOR + FtpConsts.NAME + name;
    }
}
