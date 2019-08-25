package org.code4everything.wetool.plugin.ftp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import org.code4everything.wetool.plugin.ftp.FtpManager;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.ftp.model.LastUsedInfo;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

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
        LastUsedInfo f = LastUsedInfo.getInstance();
        super.initialize(f.getFtpNames(), f.getDownloadFtpName(), f.getDownloadFile(), f.getLocalSaveDir(), true);
    }

    @Override
    public void choosePath() {
        FxUtils.chooseFolder(file -> localPath.setText(getFolder(file)));
    }

    @Override
    public void dragFileDropped(DragEvent event) {
        FxUtils.dropFiles(event, file -> localPath.setText(getFolder(file.get(0))));
    }

    public void download() {
        if (FtpManager.isFtpNotSelected(ftpName)) {
            FxDialogs.showError(FtpConsts.SELECT_FTP);
            return;
        }
        if (!FtpManager.exists(ftpName, remotePath.getValue())) {
            FxDialogs.showError(FtpConsts.FILE_NOT_EXISTS);
            return;
        }
        downloadButton.setDisable(true);
        downloadButton.setText("下载中。。。");
        FtpManager.download(ftpName, remotePath.getValue(), new File(localPath.getText()));
        downloadButton.setDisable(false);
        downloadButton.setText("下载");
    }

    private String getFolder(File file) {
        return file.isFile() ? file.getParent() : file.getAbsolutePath();
    }
}
