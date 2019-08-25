package org.code4everything.wetool.plugin.ftp.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import org.code4everything.boot.base.constant.StringConsts;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
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

        saveDir.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> endCaretPosition());
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
            endCaretPosition();
            String path = StrUtil.addSuffixIfNot(saveDir.getValue(), StringConsts.Sign.SLASH);
            List<String> children = childrenMap.get(path);
            if (CollUtil.isEmpty(children)) {
                // 从FTP服务器列出子目录
                children = FtpManager.listChildren(ftpName, path, false);
                childrenMap.put(path, children);
            }
            saveDir.getItems().clear();
            saveDir.getItems().add(path);
            saveDir.getItems().addAll(children);
            saveDir.getSelectionModel().selectFirst();
            saveDir.show();
        });
    }

    /**
     * 将光标移到尾部
     */
    private void endCaretPosition() {
        // 通过失去焦点，获取焦点，片刻获取变化后的值
        ftpName.requestFocus();
        saveDir.requestFocus();
        saveDir.getEditor().positionCaret(Integer.MAX_VALUE);
    }
}
