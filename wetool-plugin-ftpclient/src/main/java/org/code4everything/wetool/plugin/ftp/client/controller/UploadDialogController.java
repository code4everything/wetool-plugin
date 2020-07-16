package org.code4everything.wetool.plugin.ftp.client.controller;

import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import org.code4everything.wetool.plugin.ftp.client.FtpManager;
import org.code4everything.wetool.plugin.ftp.client.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.client.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;

/**
 * @author pantao
 * @since 2019/8/24
 */
public class UploadDialogController extends AbstractDialogController {

    @FXML
    public Button uploadButton;

    @FXML
    private void initialize() {
        LastUsedInfo info = LastUsedInfo.getInstance();
        super.initialize(info, info.getRemoteSaveDir(), info.getUploadFile(), false);
    }

    @Override
    public void choosePath() {
        FxUtils.chooseFile(file -> localPath.setText(file.getAbsolutePath()));
    }

    public void upload() {
        if (FtpManager.isFtpNotSelected(ftpName)) {
            FxDialogs.showError(FtpConsts.SELECT_FTP);
            return;
        }
        File file = new File(StrUtil.nullToEmpty(localPath.getText()));
        if (!file.exists()) {
            FxDialogs.showError(FtpConsts.FILE_NOT_EXISTS);
            return;
        }
        uploadButton.setDisable(true);
        uploadButton.setText("上传中。。。");
        FtpManager.upload(ftpName, remotePath.getValue(), file);
        uploadButton.setDisable(false);
        uploadButton.setText("上传");
    }

    @Override
    public void dragFileDropped(DragEvent event) {
        FxUtils.dropFiles(event, file -> localPath.setText(file.get(0).getAbsolutePath()));
    }
}
