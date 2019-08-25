package org.code4everything.wetool.plugin.ftp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;

/**
 * @author pantao
 * @since 2019/8/24
 */
public class DownloadDialogController extends AbstractDialogController {

    @FXML
    public Button downloadButton;

    @FXML
    private void initialize() {
        LastUsedInfo info = LastUsedInfo.getInstance();
        super.initialize(info, info.getDownloadFile(), info.getLocalSaveDir(), true);
    }

    @Override
    public void choosePath() {
        FxUtils.chooseFolder(file -> localPath.setText(file.getAbsolutePath()));
    }

    @Override
    public void dragFileDropped(DragEvent event) {
        FxUtils.dropFiles(event, file -> localPath.setText(WeUtils.parseFolder(file.get(0))));
    }

    public void download() {
        if (FtpManager.isFtpNotSelected(ftpName)) {
            FxDialogs.showError(FtpConsts.SELECT_FTP);
            return;
        }
        downloadButton.setDisable(true);
        if (!FtpManager.exists(ftpName, remotePath.getValue())) {
            FxDialogs.showError(FtpConsts.FILE_NOT_EXISTS);
            downloadButton.setDisable(false);
            return;
        }
        downloadButton.setText("下载中。。。");
        FtpManager.download(ftpName, remotePath.getValue(), new File(localPath.getText()));
        downloadButton.setDisable(false);
        downloadButton.setText("下载");
    }
}
