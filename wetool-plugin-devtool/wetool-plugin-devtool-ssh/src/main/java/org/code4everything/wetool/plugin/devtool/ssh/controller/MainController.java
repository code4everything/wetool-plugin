package org.code4everything.wetool.plugin.devtool.ssh.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.devtool.ssh.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

/**
 * @author pantao
 * @since 2019/11/24
 */
public class MainController implements BaseViewController {

    @FXML
    public ComboBox serverCombo;

    @FXML
    public TextField currPathText;

    @FXML
    public TabPane terminalTabPane;

    @FXML
    public ListView fileList;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
//        TerminalBuilder terminalBuilder = new TerminalBuilder();
//        TerminalTab terminal = terminalBuilder.newTerminal();

    }

    public void openConfigFile(ActionEvent actionEvent) {

    }

    public void reloadConfig(ActionEvent actionEvent) {

    }

    public void openLocalTerminal(ActionEvent actionEvent) {

    }

    public void openRemoteTerminal(ActionEvent actionEvent) {

    }

    public void uploadOnBtn(ActionEvent actionEvent) {

    }

    public void downloadAndOpen(ActionEvent actionEvent) {

    }

    public void uploadOnMenu(ActionEvent actionEvent) {

    }

    public void download(ActionEvent actionEvent) {

    }

    public void delete(ActionEvent actionEvent) {

    }

    public void copyPath(ActionEvent actionEvent) {

    }
}
