package org.code4everything.wetool.plugin.ftp.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/8/25
 */
@Slf4j
public class FtpController implements BaseViewController {

    @FXML
    public TextField localPath;

    @FXML
    public ListView<File> localFiles;

    @FXML
    public TextField remotePath;

    @FXML
    public ListView<String> remoteFiles;

    @FXML
    public Label statusLabel;

    @FXML
    public ComboBox<String> ftpName;

    @FXML
    private void initialize() {
        log.info("open tab for {}[{}]", FtpConsts.NAME, FtpConsts.AUTHOR);
        BeanFactory.registerView(FtpConsts.TAB_ID, FtpConsts.TAB_NAME, this);
        localFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        LastUsedInfo info = LastUsedInfo.getInstance();
        localPath.setText(info.getLocalDir());
        remotePath.setText(info.getRemoteDir());

        ftpName.getItems().addAll(info.getFtpNames());
        ftpName.getSelectionModel().select(info.getFtpName());
    }

    public void updateStatus(String statusText, Object... params) {
        Platform.runLater(() -> {
            if (StrUtil.isEmpty(statusText)) {
                FxDialogs.showInformation(null, "上传完成");
            }
            statusLabel.setText(StrUtil.format(statusText, params));
        });
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
            FileUtil.mkdir(localPath.getText());
        }
    }

    public void chooseFolder() {
        FxUtils.chooseFolder(file -> localPath.setText(file.getAbsolutePath()));
    }

    public void deleteLocalFile() {
        getSelectedLocalFiles(false).removeIf(File::delete);
    }

    public void downloadButtonClicked() {

    }

    public void makeRemoteDir() {
        if (StrUtil.isNotEmpty(remotePath.getText())) {
            FtpManager.getFtp(ftpName).mkDirs(remotePath.getText());
        }
    }

    public void downloadMenuClicked() {

    }

    public void deleteRemoteFile() {
        getSelectedRemoteFiles(false).removeIf(file -> FtpManager.delete(ftpName, file));
    }

    public void upload() {
        FtpManager.upload(ftpName, getRemotePath(), getSelectedLocalFiles(true));
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
        return StrUtil.nullToDefault(remotePath.getText(), StringConsts.Sign.SLASH);
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
}
