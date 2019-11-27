package org.code4everything.wetool.plugin.everywhere.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class MainController implements BaseViewController {

    @FXML
    public CheckBox folderCheck;

    @FXML
    public CheckBox fileCheck;

    @FXML
    public CheckBox contentCheck;

    @FXML
    public TextField searchText;

    @FXML
    public TableView fileTable;

    @FXML
    public TableColumn nameColumn;

    @FXML
    public TableColumn pathColumn;

    @FXML
    public TableColumn sizeColumn;

    @FXML
    public TableColumn timeColumn;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
    }

    public void openConfigFile(ActionEvent actionEvent) {

    }

    public void reloadConfig(ActionEvent actionEvent) {

    }

    public void findEverywhere(ActionEvent actionEvent) {

    }
}
