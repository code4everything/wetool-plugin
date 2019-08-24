package org.code4everything.wetool.plugin.ftp.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import org.apache.commons.net.ftp.FTPFile;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pantao
 * @since 2019/8/24
 */
public class UploadDialogController implements BaseViewController {

    private final Map<String, List<String>> childrenMap = new HashMap<>(16);

    @FXML
    public ComboBox<String> ftpName;

    @FXML
    public ComboBox<String> saveDir;

    @FXML
    public TextField uploadFile;

    @FXML
    private void initialize() {
        LastUsedInfo lastUsedInfo = LastUsedInfo.getInstance();
        ftpName.getItems().addAll(lastUsedInfo.getFtpNames());
        ftpName.getSelectionModel().select(lastUsedInfo.getUploadFtpName());

        saveDir.setValue(lastUsedInfo.getRemoteSaveDir());
        uploadFile.setText(lastUsedInfo.getUploadFile());
    }

    public void chooseFile() {
        FxUtils.chooseFile(file -> uploadFile.setText(file.getAbsolutePath()));
    }

    public void upload() {
        if (FtpManager.isFtpNotSelected(ftpName)) {
            FxDialogs.showError(FtpConsts.SELECT_FTP);
            return;
        }
        File file = new File(StrUtil.nullToEmpty(uploadFile.getText()));
        if (!file.exists()) {
            FxDialogs.showError(FtpConsts.FILE_NOT_EXISTS);
            return;
        }
        FtpManager.upload(ftpName, saveDir.getValue(), file);
    }

    @Override
    public void dragFileOver(DragEvent event) {
        FxUtils.acceptCopyMode(event);
    }

    @Override
    public void dragFileDropped(DragEvent event) {
        FxUtils.dropFiles(event, file -> uploadFile.setText(file.get(0).getAbsolutePath()));
    }

    public void keyReleased(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, () -> {
            if (FtpManager.isFtpNotSelected(ftpName)) {
                FxDialogs.showError(FtpConsts.SELECT_FTP);
                return;
            }
            String path = StrUtil.nullToEmpty(saveDir.getValue());
            List<String> children = childrenMap.get(path);
            if (CollUtil.isEmpty(children)) {
                // 从FTP服务器列出子目录
                List<FTPFile> files = FtpManager.listChildren(ftpName, path, false);
                if (CollUtil.isNotEmpty(files)) {
                    final List<String> list = new ArrayList<>();
                    files.forEach(file -> list.add(file.getName()));
                    children = list;
                    childrenMap.put(path, children);
                }
            }
            saveDir.getItems().clear();
            saveDir.getItems().addAll(children);
        });
    }
}
