package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.dbops.script.ExecuteTypeEnum;
import org.code4everything.wetool.plugin.dbops.script.SqlScript;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.DialogWinnable;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.io.File;
import java.io.IOException;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
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
        dbNameBox.getItems().addAll(DruidSource.listAllNames());
        dbNameBox.getSelectionModel().selectFirst();

        FileUtil.mkdir(DB_OPS_PATH);
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
        if (FileUtil.exist(SCRIPT_JSON_FILE)) {
            SCRIPTS.clear();
            String json = FileUtil.readUtf8String(SCRIPT_JSON_FILE);
            JSONObject jsonObject = JSON.parseObject(StrUtil.blankToDefault(json, "{}"), Feature.OrderedField,
                    Feature.AllowComment);
            SCRIPTS.putAll(jsonObject);
        }

        renderScripts();
    }

    public void search() {

    }

    public void addScript() {
        showSqlScriptEditDialog(null);
    }

    private void renderScripts() {
        parentPane.getChildren().clear();

        Insets bottom = new Insets(10, 0, 10, 0);
        Insets right = new Insets(0, 10, 0, 0);
        Insets top = new Insets(3, 0, 0, 0);
        for (String uuid : SCRIPTS.keySet()) {
            HBox hBox = new HBox();
            hBox.setId(uuid);

            SqlScript sqlScript = SCRIPTS.getObject(uuid, SqlScript.class);
            Button action = new Button(sqlScript.getName());
            Button edit = new Button("编辑");
            Label label = new Label();

            String labelText = "触发机制：" + ScriptEditController.TYPE_2_TIP.get(sqlScript.getType().name());
            if (sqlScript.getType() == ExecuteTypeEnum.EVENT) {
                labelText += "，事件订阅：" + sqlScript.getEventKey();
            }
            if (StrUtil.isNotBlank(sqlScript.getSpecifyDbName())) {
                labelText += "，指定数据源：" + sqlScript.getSpecifyDbName();
            }
            if (StrUtil.isNotBlank(sqlScript.getComment())) {
                labelText += "，说明：" + sqlScript.getComment();
            }
            label.setText(labelText);

            HBox.setHgrow(action, Priority.NEVER);
            HBox.setHgrow(edit, Priority.NEVER);
            HBox.setHgrow(label, Priority.ALWAYS);
            HBox.setMargin(action, right);
            HBox.setMargin(edit, right);
            HBox.setMargin(label, top);
            hBox.getChildren().addAll(action, edit, label);

            VBox.setVgrow(hBox, Priority.NEVER);
            Separator separator = new Separator();
            VBox.setVgrow(separator, Priority.NEVER);
            VBox.setMargin(separator, bottom);
            parentPane.getChildren().addAll(hBox, separator);
        }
    }

    private void showSqlScriptEditDialog(SqlScript sqlScript) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(MainController.class.getResource("/ease/dbops/ScriptEditView.fxml"));

        Node node;
        try {
            node = fxmlLoader.load();
        } catch (IOException e) {
            log.error(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            FxDialogs.showError("加载视图发生异常");
            return;
        }

        ScriptEditController controller = fxmlLoader.getController();
        controller.setSqlScript(sqlScript);
        FxDialogs.showDialog("编辑SQL脚本", node, new DialogWinnable<String>() {

            @Override
            public String convertResult() {
                return "ok";
            }

            @Override
            public void consumeResult(String result) {
                if (StrUtil.isEmpty(result)) {
                    return;
                }
                SqlScript newScript = controller.getSqlScript();
                SCRIPTS.put(newScript.getUuid(), newScript);
                ThreadUtil.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), SCRIPT_JSON_FILE));
            }
        });
    }
}
