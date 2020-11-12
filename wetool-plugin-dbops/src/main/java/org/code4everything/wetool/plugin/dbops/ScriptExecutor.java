package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Holder;
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
import org.code4everything.wetool.plugin.support.druid.JdbcExecutor;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.util.*;

/**
 * @author pantao
 * @since 2020/11/11
 */
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

        ExpressRunner expressRunner = RUNNER_MAP.computeIfAbsent(dbName, name -> {
            ExpressRunner runner = new ExpressRunner();

            try {
                runner.addFunctionOfClassMethod("dialog", CLASS_NAME, "dialog", new Class[]{Object.class}, null);
                runner.addFunctionOfClassMethod("list", CLASS_NAME, "list", new Class[]{Object[].class}, null);

                Class<?>[] logParamTypes = {Object.class, Object[].class};
                runner.addFunctionOfClassMethod("log", Console.class, "log", logParamTypes, null);
                Class<?>[] formatParamTypes = {CharSequence.class, Object[].class};
                runner.addFunctionOfClassMethod("format", StrUtil.class, "format", formatParamTypes, null);

                JdbcExecutor jdbcExecutor = JdbcExecutor.getJdbcExecutor(dbName);
                Class<?>[] sqlParamTypes = {String.class, List.class};
                runner.addFunctionOfServiceMethod("query", jdbcExecutor, "select", sqlParamTypes, null);
                runner.addFunctionOfServiceMethod("update", jdbcExecutor, "update", sqlParamTypes, null);
            } catch (Exception e) {
                FxDialogs.showException("注入脚本方法发生错误", e);
            }

            return runner;
        });

        expressRunner.execute(codes, context, null, true, false);
    }

    public static String input(String tip) {
        Holder<String> holder = new Holder<>();
        FxDialogs.showTextInput("请输入", tip, holder::set);
        return holder.get();
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
                return;
            }

            List<Map<String, Object>> tableList = new ArrayList<>();
            list.forEach(e -> tableList.add(BeanUtil.beanToMap(e)));
            Map<String, Object> map = tableList.get(0);

            VBox vBox = new VBox();
            TableView<Map<String, Object>> tableView = new TableView<>();
            map.forEach((k, v) -> {
                TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>();
                tableColumn.setText(ObjectUtil.toString(k));
                tableColumn.setCellValueFactory(new PropertyValueFactory<>(null) {
                    @Override
                    @SneakyThrows
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, Object>, String> param) {
                        return new SimpleObjectProperty<>(ObjectUtil.toString(param.getValue().get(k)));
                    }
                });
                tableView.getColumns().add(tableColumn);
            });

            tableView.getItems().addAll(list);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            vBox.getChildren().add(tableView);
            FxDialogs.showDialog(header, vBox);
        }
    }
}
