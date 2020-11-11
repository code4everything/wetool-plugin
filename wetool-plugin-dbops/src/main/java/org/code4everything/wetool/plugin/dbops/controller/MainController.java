package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.code4everything.wetool.plugin.dbops.script.SqlScript;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class MainController implements BaseViewController {

    public static final String TAB_ID = "ease-db-ops";

    public static final String TAB_NAME = "数据库小应用";

    public static final List<SqlScript> SCRIPTS = new ArrayList<>();

    private static final String HOME_PATH = FileUtil.getUserHomePath();

    private static final File DB_OPS_PATH = FileUtil.file(HOME_PATH, "wetool", "wetool-plugin-dbops", ".dbops");

    private static final File SCRIPT_JSON_FILE = FileUtil.file(DB_OPS_PATH, "sql-script.json");

    @FXML
    public ComboBox<String> dbNameBox;

    @FXML
    public TextField searchText;

    @FXML
    public VBox parentPane;

    @FXML
    private void initialize() {
        FileUtil.mkdir(DB_OPS_PATH);
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
        if (FileUtil.exist(SCRIPT_JSON_FILE)) {
            SCRIPTS.clear();
            String json = FileUtil.readUtf8String(SCRIPT_JSON_FILE);
            List<SqlScript> list = JSON.parseArray(StrUtil.blankToDefault(json, "[]"), SqlScript.class);
            SCRIPTS.addAll(list);
        }
    }

    public void search() {

    }
}
