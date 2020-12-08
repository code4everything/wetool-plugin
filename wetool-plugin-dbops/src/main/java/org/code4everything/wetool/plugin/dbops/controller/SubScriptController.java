package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.code4everything.wetool.plugin.dbops.ScriptExecutor;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

/**
 * @author pantao
 * @since 2020/12/6
 */
public class SubScriptController {

    private final JSONObject subScript = new JSONObject();

    @FXML
    public ComboBox<String> globalVarName;

    @FXML
    public TextArea subScriptArea;

    @FXML
    private void initialize() {
        if (FileUtil.exist(MainController.subScriptFile)) {
            String json = FileUtil.readUtf8String(MainController.subScriptFile);
            subScript.putAll(JSON.parseObject(json));
        }

        globalVarName.getItems().addAll(subScript.keySet());
        globalVarName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> subScriptArea.setText(StrUtil.nullToEmpty(subScript.getString(newValue))));
        globalVarName.getSelectionModel().selectFirst();
    }

    public void saveSubScripts() {
        String varKey = globalVarName.getValue();
        String script = subScriptArea.getText();
        if (StrUtil.isBlank(varKey) || StrUtil.isBlank(script)) {
            return;
        }
        subScript.put(varKey, script);
        FileUtil.writeUtf8String(JSON.toJSONString(subScript, true), MainController.subScriptFile);
        if (!globalVarName.getItems().contains(varKey)) {
            globalVarName.getItems().add(varKey);
        }
        ScriptExecutor.GLOBAL_VARS.put(varKey, script);
        FxDialogs.showSuccess();
    }

    public void remove() {
        String varKey = globalVarName.getValue();
        subScript.remove(varKey);
        ScriptExecutor.GLOBAL_VARS.remove(varKey);
        FileUtil.writeUtf8String(JSON.toJSONString(subScript, true), MainController.subScriptFile);
    }
}
