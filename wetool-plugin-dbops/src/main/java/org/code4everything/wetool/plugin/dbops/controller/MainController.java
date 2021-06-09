package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Holder;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.dbops.ScriptExecutor;
import org.code4everything.wetool.plugin.dbops.WetoolSupporter;
import org.code4everything.wetool.plugin.dbops.script.ExecuteTypeEnum;
import org.code4everything.wetool.plugin.dbops.script.QlScript;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.exception.ToDialogException;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.DialogWinnable;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
public class MainController implements BaseViewController {

    public static final String TAB_ID = "ease-db-ops";

    public static final String TAB_NAME = "JavaQL脚本小程序";

    private static final JSONObject SCRIPTS = new JSONObject(true);

    private static final String HOME_PATH = FileUtil.getUserHomePath();

    private static final File DB_OPS_PATH = FileUtil.file(HOME_PATH, "wetool", "wetool-plugin-dbops", ".dbops");

    private static final Map<String, Set<String>> EVENT_SCRIPT = new HashMap<>(8);

    public static File scriptJsonFile = FileUtil.file(DB_OPS_PATH, "ql-script.json");

    public static File subScriptFile = FileUtil.file(DB_OPS_PATH, "sub-script.json");

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

        readScript();
        renderScripts(null);
    }

    private void readScript() {
        if (!FileUtil.exist(scriptJsonFile)) {
            return;
        }
        if (FileUtil.exist(subScriptFile)) {
            try {
                String subScript = FileUtil.readUtf8String(subScriptFile);
                ScriptExecutor.GLOBAL_VARS.putAll(JSON.parseObject(subScript));
            } catch (Exception e) {
                FxDialogs.showException("加载子脚本失败", e);
            }
        }
        SCRIPTS.clear();
        log.info("reload script");
        String json = FileUtil.readUtf8String(scriptJsonFile);
        JSONObject jsonObject = JSON.parseObject(StrUtil.blankToDefault(json, "{}"), Feature.OrderedField, Feature.AllowComment);
        SCRIPTS.putAll(jsonObject);
    }

    public void search() {
        renderScripts(searchText.getText());
    }

    public void addScript() {
        showQlScriptEditDialog(null);
    }

    private void renderScripts(String search) {
        log.info("render script");
        parentPane.getChildren().clear();

        Insets bottom = new Insets(10, 0, 10, 0);
        Insets right = new Insets(0, 10, 0, 0);
        Insets top = new Insets(3, 0, 0, 0);

        EventHandler<ActionEvent> editHandler = actionEvent -> {
            Button button = (Button) actionEvent.getSource();
            String uuid = button.getParent().getId();
            showQlScriptEditDialog(getScript(uuid));
        };
        EventHandler<ActionEvent> removeHandler = actionEvent -> {
            Button button = (Button) actionEvent.getSource();
            String uuid = button.getParent().getId();
            SCRIPTS.remove(uuid);
            WeUtils.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), scriptJsonFile));
            renderScripts(null);
        };
        EventHandler<ActionEvent> actionHandler = actionEvent -> {
            Button button = (Button) actionEvent.getSource();
            String uuid = button.getParent().getId();
            execScript(getScript(uuid));
        };

        for (String uuid : SCRIPTS.keySet()) {
            QlScript qlScript = getScript(uuid);
            if (StrUtil.isNotBlank(search)) {
                if (!qlScript.getName().contains(search) && !qlScript.getComment().contains(search)) {
                    continue;
                }
            }

            String name = MainController.TAB_NAME + "-ease-dbops/" + qlScript.getName();
            if (BooleanUtil.isTrue(qlScript.getRegister2Search())) {
                FxUtils.registerAction(name, actionEvent -> execScript(qlScript));
            } else {
                FxUtils.unregisterAction(name);
            }

            HBox hBox = new HBox();
            hBox.setId(uuid);

            Button action = new Button(qlScript.getName());
            action.setOnAction(actionHandler);
            Button edit = new Button("编辑");
            edit.setOnAction(editHandler);
            Button remove = new Button("删除");
            remove.setOnAction(removeHandler);
            Label label = new Label();

            String labelText = "触发机制：" + qlScript.getType().getDesc();
            if (qlScript.getType() == ExecuteTypeEnum.EVENT) {
                labelText += "，订阅事件：" + qlScript.getEventKey();
                eventSubscribe(qlScript.getEventKey(), uuid);
            } else if (qlScript.getType() == ExecuteTypeEnum.CRON) {
                labelText += "，CRON表达式：" + qlScript.getEventKey();
                addCron(qlScript.getUuid(), qlScript.getEventKey());
            }
            if (StrUtil.isNotBlank(qlScript.getSpecifyDbName())) {
                labelText += "，指定数据源：" + qlScript.getSpecifyDbName();
            }
            if (StrUtil.isNotBlank(qlScript.getComment())) {
                labelText += "，说明：" + qlScript.getComment();
            }
            label.setText(labelText);

            HBox.setHgrow(action, Priority.NEVER);
            HBox.setHgrow(edit, Priority.NEVER);
            HBox.setHgrow(remove, Priority.NEVER);
            HBox.setHgrow(label, Priority.ALWAYS);
            HBox.setMargin(action, right);
            HBox.setMargin(edit, right);
            HBox.setMargin(remove, right);
            HBox.setMargin(label, top);
            hBox.getChildren().addAll(action, edit, remove, label);

            VBox.setVgrow(hBox, Priority.NEVER);
            Separator separator = new Separator();
            VBox.setVgrow(separator, Priority.NEVER);
            VBox.setMargin(separator, bottom);
            parentPane.getChildren().addAll(hBox, separator);
        }
    }

    private void execScript(QlScript qlScript) {
        String dbName = StrUtil.blankToDefault(qlScript.getSpecifyDbName(), dbNameBox.getValue());

        if (BooleanUtil.isTrue(qlScript.getExecInFx())) {
            Platform.runLater(() -> {
                try {
                    ScriptExecutor.execute(dbName, qlScript.getCodes(), null);
                } catch (Exception e) {
                    FxDialogs.showException("执行脚本失败", e);
                }
            });
        } else {
            WeUtils.execute(() -> {
                try {
                    ScriptExecutor.execute(dbName, qlScript.getCodes(), null);
                } catch (Exception e) {
                    FxDialogs.showException("执行脚本失败", e);
                }
            });
        }
    }

    private void showQlScriptEditDialog(QlScript qlScript) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setClassLoader(WetoolSupporter.class.getClassLoader());
        fxmlLoader.setLocation(MainController.class.getResource("/ease/dbops/ScriptEditView.fxml"));

        Node node;
        try {
            node = fxmlLoader.load();
        } catch (IOException e) {
            log.error(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            throw ToDialogException.ofError("加载视图发生异常");
        }

        ScriptEditController controller = fxmlLoader.getController();
        controller.setQlScript(qlScript);
        FxDialogs.showDialog("编辑QL脚本", node, new DialogWinnable<String>() {

            @Override
            public String convertResult() {
                return "ok";
            }

            @Override
            public void consumeResult(String result) {
                if (StrUtil.isEmpty(result)) {
                    return;
                }
                QlScript newScript = controller.getQlScript();
                SCRIPTS.put(newScript.getUuid(), newScript);
                renderScripts(null);
                WeUtils.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), scriptJsonFile));
            }
        });
    }

    private void eventSubscribe(String eventKey, String uuid) {
        if (StrUtil.isBlank(eventKey) || StrUtil.isBlank(uuid)) {
            return;
        }

        Set<String> scripts = EVENT_SCRIPT.computeIfAbsent(eventKey, s -> new HashSet<>());
        if (CollUtil.isEmpty(scripts)) {
            // 为空说明eventKey还未订阅事件
            EventCenter.subscribeEvent(eventKey, (key, date, eventMessage) -> {
                Set<String> set = EVENT_SCRIPT.get(key);
                if (CollUtil.isEmpty(set)) {
                    return;
                }
                set.forEach(e -> {
                    QlScript qlScript = getScript(e);
                    if (Objects.isNull(qlScript) || qlScript.getType() != ExecuteTypeEnum.EVENT || !key.equals(qlScript.getEventKey())) {
                        return;
                    }

                    Map<String, Object> args = new HashMap<>(4, 1);
                    args.put("eventKey", key);
                    args.put("eventTime", date);
                    if (Objects.nonNull(eventMessage)) {
                        args.put("eventMessage", eventMessage);
                        args.put("messageClass", eventMessage.getClass().getName());
                    }

                    executeByEventCall(qlScript, args);
                });
            });
        }

        scripts.add(uuid);
    }

    private void executeByEventCall(QlScript qlScript, Map<String, Object> args) {
        String dbName = StrUtil.blankToDefault(qlScript.getSpecifyDbName(), dbNameBox.getValue());
        if (BooleanUtil.isTrue(qlScript.getExecInFx())) {
            Platform.runLater(() -> {
                try {
                    ScriptExecutor.execute(dbName, qlScript.getCodes(), args);
                } catch (Exception x) {
                    String errMsg = "execute event script error: {}";
                    log.error(errMsg, ExceptionUtil.stacktraceToString(x, Integer.MAX_VALUE));
                }
            });
        } else {
            try {
                ScriptExecutor.execute(dbName, qlScript.getCodes(), args);
            } catch (Exception x) {
                String errMsg = "execute event script error: {}";
                log.error(errMsg, ExceptionUtil.stacktraceToString(x, Integer.MAX_VALUE));
            }
        }
    }

    public void searchIfEnter(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, this::search);
    }

    public void importQl() {
        boolean success = importQl(ClipboardUtil.getStr());
        if (success) {
            WeUtils.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), scriptJsonFile));
            renderScripts(null);
        } else {
            FxUtils.chooseFile(this::openFile);
        }
    }

    private void addCron(String uuid, String cron) {
        if (StrUtil.isBlank(uuid) || StrUtil.isBlank(cron)) {
            return;
        }

        CronUtil.remove(uuid);
        CronUtil.schedule(uuid, cron, new Task() {

            private final String innerUuid = uuid;

            @Override
            public void execute() {
                QlScript qlScript = getScript(innerUuid);
                if (Objects.isNull(qlScript) || qlScript.getType() != ExecuteTypeEnum.CRON) {
                    CronUtil.remove(uuid);
                    return;
                }
                executeByEventCall(qlScript, Map.of("cron", qlScript.getEventKey(), "date", new Date()));
            }
        });

        if (!CronUtil.getScheduler().isStarted()) {
            CronUtil.start();
        }
    }

    private QlScript getScript(String uuid) {
        return SCRIPTS.getObject(uuid, QlScript.class);
    }

    private boolean importQl(String str) {
        log.info("import script");
        try {
            QlScript qlScript = JSON.parseObject(str, QlScript.class);
            if (StrUtil.isNotBlank(qlScript.getUuid())) {
                SCRIPTS.put(qlScript.getUuid(), qlScript);
                return true;
            }
            LinkedHashMap<String, QlScript> map = JSON.parseObject(str, new TypeReference<>() {}, Feature.AllowComment);
            Holder<Boolean> holder = new Holder<>(false);
            map.forEach((k, v) -> {
                if (StrUtil.isNotBlank(k)) {
                    SCRIPTS.put(k, v);
                    holder.set(true);
                }
            });
            return holder.get();
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
        }
        return false;
    }

    public void export() {
        log.info("export script");
        FxUtils.saveFile(this::saveFile);
    }

    @Override
    public String getSavingContent() {
        return JSON.toJSONString(SCRIPTS, true);
    }

    @Override
    public void setFileContent(String content) {
        boolean success = importQl(content);
        if (success) {
            WeUtils.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), scriptJsonFile));
            renderScripts(null);
        }
    }

    public void reload() {
        readScript();
        renderScripts(null);
    }

    public void listSubScripts() {
        Pane pane = FxUtils.loadFxml(WetoolSupporter.class, "/ease/dbops/SubScript.fxml", false);
        FxDialogs.showDialog("子脚本（回调脚本）", pane);
    }

    public void openDoc() {
        // @formatter:off
        FxUtils.openLink("https://gitee.com/code4everything/wetool-plugin/blob/master/wetool-plugin-repository/ease-dbops/readme.md");
        // @formatter:on
    }
}
