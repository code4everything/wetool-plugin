package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.control.cell.UnmodifiableTextFieldTableCell;
import org.code4everything.wetool.plugin.support.druid.JdbcExecutor;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
@UtilityClass
public class ScriptExecutor {

    private static final String CLASS_NAME = ScriptExecutor.class.getName();

    private static final Map<String, ExpressRunner> RUNNER_MAP = new HashMap<>(4);

    @SneakyThrows
    public static void execute(String dbName, String codes, Map<String, Object> args) {
        DefaultContext<String, Object> context = new DefaultContext<>();
        if (CollUtil.isNotEmpty(args)) {
            context.putAll(args);
        }

        ExpressRunner expressRunner = getExpressRunner(dbName);
        expressRunner.execute(codes, context, null, true, false);
    }

    public static ExpressRunner getExpressRunner(String dbName) {
        dbName = StrUtil.blankToDefault(dbName, "");
        return RUNNER_MAP.computeIfAbsent(dbName, name -> {
            ExpressRunner runner = new ExpressRunner();
            try {
                runner.addFunctionOfClassMethod("dialog", CLASS_NAME, "dialog", new Class[]{Object.class}, null);
                runner.addFunctionOfClassMethod("list", CLASS_NAME, "list", new Class[]{Object[].class}, null);
                runner.addFunctionOfClassMethod("input", CLASS_NAME, "input", new Class[]{String.class}, null);

                Class<?>[] logParamTypes = {String.class, Object[].class};
                runner.addFunctionOfClassMethod("log", CLASS_NAME, "log", logParamTypes, null);
                Class<?>[] formatParamTypes = {CharSequence.class, Object[].class};
                runner.addFunctionOfClassMethod("format", StrUtil.class, "format", formatParamTypes, null);

                if (StrUtil.isNotEmpty(name)) {
                    JdbcExecutor jdbcExecutor = JdbcExecutor.getJdbcExecutor(name);
                    Class<?>[] sqlParamTypes = {String.class, List.class};
                    runner.addFunctionOfServiceMethod("query", jdbcExecutor, "select", sqlParamTypes, null);
                    runner.addFunctionOfServiceMethod("update", jdbcExecutor, "update", sqlParamTypes, null);
                }
            } catch (Exception e) {
                FxDialogs.showException("注入脚本方法发生错误", e);
            }
            return runner;
        });
    }

    public static void log(String msg, Object... param) {
        log.info(msg, param);
    }

    public static String input(String tip) throws ExecutionException, InterruptedException {
        Future<String> future = FxDialogs.showTextInput("请输入", tip);
        return future.get();
    }

    public static List<Object> list(Object... args) {
        return Arrays.asList(args);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void dialog(Object object) {
        if (Objects.isNull(object)) {
            return;
        }
        String header = "结果";
        if (object instanceof String) {
            FxDialogs.showInformation(header, (String) object);
        } else if (object instanceof List) {
            List list = (List) object;
            if (CollUtil.isNotEmpty(list)) {
                list.removeIf(Objects::isNull);
            }
            if (CollUtil.isEmpty(list)) {
                dialog("结果为空！");
                return;
            }

            List<Map<String, Object>> tableList = new ArrayList<>();
            list.forEach(e -> tableList.add(BeanUtil.beanToMap(e)));
            Map<String, Object> map = tableList.get(0);

            VBox vBox = new VBox();
            TableView<Map<String, Object>> tableView = new TableView<>();
            map.forEach((k, v) -> {
                TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>();
                tableColumn.setEditable(true);
                tableColumn.setText(ObjectUtil.toString(k));
                tableColumn.setCellFactory(UnmodifiableTextFieldTableCell.forTableColumn());
                tableColumn.setCellValueFactory(new PropertyValueFactory<>(null) {
                    @Override
                    @SneakyThrows
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, Object>, String> param) {
                        return new SimpleObjectProperty<>(ObjectUtil.toString(param.getValue().get(k)));
                    }
                });
                tableView.getColumns().add(tableColumn);
            });

            tableView.setEditable(true);
            tableView.getItems().addAll(list);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            vBox.getChildren().add(tableView);
            vBox.setPrefWidth(1000);
            FxDialogs.showDialog(header, vBox);
        }
    }
}
