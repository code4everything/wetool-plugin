package org.code4everything.wetool.plugin.ftp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import org.code4everything.wetool.plugin.ftp.constant.FtpConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.util.List;

/**
 * @author pantao
 * @since 2019/8/25
 */
public class FtpController implements BaseViewController {

    @FXML
    public TextField localPath;

    @FXML
    public ListView localFiles;

    @FXML
    public TextField remotePath;

    @FXML
    public ListView remoteFiles;

    @FXML
    public Label statusLabel;

    @FXML
    private void initialize() {
        BeanFactory.registerView(FtpConsts.TAB_ID, FtpConsts.TAB_NAME, this);
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

    }

    public void makeLocalDir() {

    }

    public void chooseFolder() {

    }

    public void uploadMenuClicked() {

    }

    public void deleteLocalFile() {

    }

    public void uploadButtonClicked() {

    }

    public void downloadButtonClicked() {

    }

    public void makeRemoteDir() {

    }

    public void downloadMenuClicked() {

    }

    public void deleteRemoteFile() {

    }
}
