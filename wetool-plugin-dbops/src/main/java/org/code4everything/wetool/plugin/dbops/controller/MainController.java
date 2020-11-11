package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.io.File;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class MainController implements BaseViewController {

    public static final String TAB_ID = "ease-db-ops";

    public static final String TAB_NAME = "数据库小应用";

    private static final JSONObject SCRIPTS = new JSONObject(true);

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
            JSONObject jsonObject = JSON.parseObject(StrUtil.blankToDefault(json, "{}"), Feature.OrderedField,
                    Feature.AllowComment);
            SCRIPTS.putAll(jsonObject);
        }
    }

    public void search() {

    }

    public void addScript() {

    }
}
