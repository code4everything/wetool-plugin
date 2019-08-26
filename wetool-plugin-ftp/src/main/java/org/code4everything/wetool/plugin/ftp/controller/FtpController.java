package org.code4everything.wetool.plugin.ftp.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.util.*;

/**
 * @author pantao
 * @since 2019/8/25
 */
@Slf4j
public class FtpController implements BaseViewController {

    private final Map<String, FTPFile> ftpFileMap = new HashMap<>(64);

    @FXML
    public TextField localPath;

    @FXML
    public ListView<File> localFiles;

    @FXML
    public TextField remotePath;

    @FXML
    public ListView<String> remoteFiles;

    @FXML
    public ComboBox<String> ftpName;

    @FXML
    public Label uploadStatus;

    @FXML
    public Label downloadStatus;

    @FXML
    private void initialize() {
        log.info("open tab for {}[{}]", FtpConsts.NAME, FtpConsts.AUTHOR);
        BeanFactory.registerView(FtpConsts.TAB_ID, FtpConsts.TAB_NAME, this);

        uploadStatus.setText("");
        downloadStatus.setText("");


        localFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        LastUsedInfo info = LastUsedInfo.getInstance();
        localPath.setText(info.getLocalDir());
        remotePath.setText(info.getRemoteDir());

        ftpName.getItems().addAll(info.getFtpNames());
        ftpName.getSelectionModel().select(info.getFtpName());
    }

    public void updateUploadStatus(String status, Object... params) {
        updateStatus(uploadStatus, status, FtpConsts.UPLOAD_COMPLETED, params);
    }

    public void updateDownloadStatus(String status, Object... params) {
        updateStatus(downloadStatus, status, FtpConsts.DOWNLOAD_COMPLETED, params);
    }

    @Override
    public void dragFileOver(DragEvent event) {
        FxUtils.acceptCopyMode(event);
    }

    @Override
    public void dragFileDropped(DragEvent event) {
        FxUtils.dropFiles(event, this::openMultiFiles);
    }

    @Override
    public void openMultiFiles(List<File> files) {
        openFile(files.get(0));
    }

    @Override
    public void openFile(File file) {
        localPath.setText(file.getAbsolutePath());
    }

    public void makeLocalDir() {
        if (StrUtil.isNotEmpty(localPath.getText())) {
            File path = FileUtil.mkdir(localPath.getText());
            listLocalFiles(path.getParentFile());
        }
    }

    public void chooseFolder() {
        FxUtils.chooseFolder(file -> localPath.setText(file.getAbsolutePath()));
    }

    public void deleteLocalFile() {
        getSelectedLocalFiles(false).forEach(File::delete);
        listLocalFiles(new File(getLocalPath()));
    }

    public void makeRemoteDir() {
        String path = remotePath.getText();
        if (StrUtil.isNotEmpty(path) && !StringConsts.Sign.SLASH.equals(path)) {
            FtpManager.getFtp(ftpName).mkDirs(remotePath.getText());
            listRemoteFiles(parseParentPath(path));
        }
    }

    public void download() {
        FtpManager.download(ftpName, getSelectedRemoteFiles(true), new File(getLocalPath()));
    }

    public void deleteRemoteFile() {
        List<String> files = getSelectedRemoteFiles(false);
        files.forEach(file -> FtpManager.delete(ftpName, file, ftpFileMap.get(file).isDirectory()));
        listRemoteFiles(getRemotePath());
    }

    public void listLocalFiles(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, () -> listLocalFiles(new File(getLocalPath())));
    }

    public void upload() {
        FtpManager.upload(ftpName, getRemotePath(), getSelectedLocalFiles(true));
    }

    public void set2LocalPath(MouseEvent mouseEvent) {
        FxUtils.doubleClicked(mouseEvent, () -> {
            File file = getSelectedLocalFiles(false).get(0);
            if (file.isDirectory()) {
                localPath.setText(file.getAbsolutePath());
                listLocalFiles(file);
            }
        });
    }

    public void listRemoteFiles(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, () -> listRemoteFiles(getRemotePath()));
    }

    public void set2RemotePath(MouseEvent event) {
        FxUtils.doubleClicked(event, () -> {
            String path = getSelectedRemoteFiles(false).get(0);
            if (ftpFileMap.get(path).isDirectory()) {
                remotePath.setText(path);
                listRemoteFiles(path);
            }
        });
    }

    private void listRemoteFiles(String path) {
        if (ftpFileMap.containsKey(path) && !ftpFileMap.get(path).isDirectory()) {
            // 如果已知路径不是文件夹，则不继续
            return;
        }
        String slash = StringConsts.Sign.SLASH;
        remoteFiles.getItems().clear();
        ftpFileMap.clear();
        if (!slash.equals(path)) {
            remoteFiles.getItems().add(parseParentPath(path));
        }
        remoteFiles.getItems().add(setType2Folder(path));
        path = StrUtil.addSuffixIfNot(path, slash);
        remoteFiles.getItems().addAll(FtpManager.listChildren(ftpName, path, true, ftpFileMap));
    }

    private String parseParentPath(String remotePath) {
        String slash = StringConsts.Sign.SLASH;
        String parent = StrUtil.addPrefixIfNot(StrUtil.removeSuffix(remotePath, slash), slash);
        int idx = parent.lastIndexOf(StringConsts.Sign.SLASH);
        parent = StrUtil.emptyToDefault(parent.substring(0, idx), slash);
        return setType2Folder(parent);
    }

    private String setType2Folder(String path) {
        if (!ftpFileMap.containsKey(path)) {
            FTPFile ftpFile = new FTPFile();
            ftpFile.setType(1);
            ftpFileMap.put(path, ftpFile);
        }
        return path;
    }

    private void listLocalFiles(File path) {
        if (path.exists() && path.isDirectory()) {
            localFiles.getItems().clear();
            if (ObjectUtil.isNotNull(path.getParentFile())) {
                localFiles.getItems().add(path.getParentFile());
            }
            localFiles.getItems().add(path);
            localFiles.getItems().addAll(FileUtil.ls(path.getAbsolutePath()));
        }
    }

    private List<String> getSelectedRemoteFiles(boolean addTypedIfEmpty) {
        List<String> selectedFiles = remoteFiles.getSelectionModel().getSelectedItems();
        if (Objects.isNull(selectedFiles)) {
            selectedFiles = new ArrayList<>();
        }
        if (CollUtil.isEmpty(selectedFiles) && addTypedIfEmpty && FtpManager.getFtp(ftpName).exist(getRemotePath())) {
            selectedFiles.add(getRemotePath());
        }
        return selectedFiles;
    }

    private String getRemotePath() {
        if (StrUtil.isEmpty(remotePath.getText())) {
            remotePath.setText(StringConsts.Sign.SLASH);
        }
        return remotePath.getText();
    }

    private String getLocalPath() {
        if (StrUtil.isEmpty(localPath.getText())) {
            localPath.setText(FileUtil.getUserHomePath());
        }
        return localPath.getText();
    }

    private List<File> getSelectedLocalFiles(boolean addTypedIfEmpty) {
        List<File> selectedFiles = localFiles.getSelectionModel().getSelectedItems();
        if (Objects.isNull(selectedFiles)) {
            selectedFiles = new ArrayList<>();
        }
        if (CollUtil.isEmpty(selectedFiles) && addTypedIfEmpty && FileUtil.exist(localPath.getText())) {
            selectedFiles.add(new File(localPath.getText()));
        }
        return selectedFiles;
    }

    private void updateStatus(Label statusLabel, String status, String tip, Object... params) {
        Platform.runLater(() -> {
            if (StrUtil.isEmpty(status)) {
                FxDialogs.showInformation(null, tip);
            }
            statusLabel.setText(StrUtil.format(status, params));
        });
    }
}
