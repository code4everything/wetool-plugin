package org.code4everything.wetool.plugin.devtool.redis.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class MainController implements BaseViewController {

    @FXML
    public TextField currentServerDb;

    @FXML
    public TreeView redisExplorer;

    @FXML
    public TabPane redisExplorerTab;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
    }

    public void openConfigFile() {

    }

    public void reloadConfig() {

    }
}
