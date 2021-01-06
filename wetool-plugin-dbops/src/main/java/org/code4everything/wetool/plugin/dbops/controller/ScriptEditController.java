package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.dbops.ScriptExecutor;
import org.code4everything.wetool.plugin.dbops.script.ExecuteTypeEnum;
import org.code4everything.wetool.plugin.dbops.script.QlScript;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class ScriptEditController {

    public static final Map<String, String> TYPE_2_TIP = Map.of("HANDY", "手动触发", "EVENT", "事件触发");

    private static final Map<String, String> TIP_2_TYPE = Map.of("手动触发", "HANDY", "事件触发", "EVENT");

    @FXML
    public TextField nameText;

    @FXML
    public TextField commentText;

    @FXML
    public ComboBox<String> typeBox;

    @FXML
    public ComboBox<String> dbNameBox;

    @FXML
    public TextArea qlScriptText;

    @FXML
    public ComboBox<String> eventKeyBox;

    @FXML
    public CheckBox execInFxCheck;

    private QlScript qlScript;

    @FXML
    private void initialize() {
        typeBox.getItems().addAll(TIP_2_TYPE.keySet());
        typeBox.getSelectionModel().selectLast();

        dbNameBox.getItems().add("");
        dbNameBox.getItems().addAll(DruidSource.listAllNames());
        dbNameBox.getSelectionModel().selectFirst();

        eventKeyBox.getItems().addAll(EventCenter.listEventKeys());
    }

    public QlScript getQlScript() {
        if (Objects.isNull(qlScript)) {
            qlScript = new QlScript().setUuid(IdUtil.simpleUUID());
        }

        qlScript.setName(nameText.getText());
        qlScript.setComment(commentText.getText());
        qlScript.setType(ExecuteTypeEnum.valueOf(TIP_2_TYPE.get(typeBox.getValue())));
        qlScript.setEventKey(eventKeyBox.getValue());
        qlScript.setSpecifyDbName(dbNameBox.getValue());
        qlScript.setCodes(qlScriptText.getText());
        qlScript.setExecInFx(execInFxCheck.isSelected());

        return qlScript;
    }

    public void setQlScript(QlScript qlScript) {
        if (Objects.isNull(qlScript)) {
            return;
        }

        this.qlScript = qlScript;
        typeBox.getSelectionModel().select(TYPE_2_TIP.get(qlScript.getType().name()));
        dbNameBox.getSelectionModel().select(qlScript.getSpecifyDbName());

        nameText.setText(qlScript.getName());
        commentText.setText(qlScript.getComment());
        eventKeyBox.setValue(qlScript.getEventKey());
        qlScriptText.setText(qlScript.getCodes());
        execInFxCheck.setSelected(BooleanUtil.isTrue(qlScript.getExecInFx()));
    }

    public void testScript() {
        String text = qlScriptText.getText();
        try {
            ScriptExecutor.getExpressRunner("").parseInstructionSet(text);
        } catch (Exception e) {
            FxDialogs.showException("脚本错误，注：本测试未注入事件参数", e);
            return;
        }
        FxDialogs.showInformation(null, "测试通过");
    }

    public void exportScript() {
        FxUtils.saveFile(file -> FileUtil.writeUtf8String(JSON.toJSONString(getQlScript(), true), file));
    }
}
