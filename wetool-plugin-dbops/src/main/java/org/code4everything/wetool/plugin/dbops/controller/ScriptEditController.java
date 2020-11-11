package org.code4everything.wetool.plugin.dbops.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class ScriptEditController {

    @FXML
    public TextField nameText;

    @FXML
    public TextField commentText;

    @FXML
    public ComboBox<String> typeBox;

    @FXML
    public ComboBox<String> dbNameBox;

    @FXML
    public TextField eventKeyText;

    @FXML
    public TextArea sqlScriptText;
}
