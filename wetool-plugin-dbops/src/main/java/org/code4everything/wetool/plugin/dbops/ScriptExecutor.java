package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.parse.ExpressPackage;
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
import org.code4everything.wetool.plugin.support.http.HttpService;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import oshi.software.os.OSProcess;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
@UtilityClass
public class ScriptExecutor {

    public static final Map<String, Object> GLOBAL_VARS = new ConcurrentHashMap<>(8);

    private static final String CLASS_NAME = ScriptExecutor.class.getName();

    private static final Map<String, ExpressRunner> RUNNER_MAP = new ConcurrentHashMap<>(4);

    private static final ThreadLocal<Map<String, Object>> TEMP_VARS = new ThreadLocal<>();

    @SneakyThrows
    public static Object execute(String dbName, String codes, Map<String, Object> args) {
        if (StrUtil.isBlank(codes)) {
            return null;
        }

        DefaultContext<String, Object> context = new DefaultContext<>();
        if (MapUtil.isNotEmpty(GLOBAL_VARS)) {
            context.putAll(GLOBAL_VARS);
        }
        if (MapUtil.isNotEmpty(args)) {
            context.putAll(args);
        }

        // 内置变量
        context.put("now", DateUtil.date());
        context.put("dbName", dbName);

        ExpressRunner expressRunner = getExpressRunner(dbName);

        Map<String, Object> tempMap = new HashMap<>(8);
        tempMap.put("dbName", StrUtil.nullToEmpty(dbName));
        TEMP_VARS.set(tempMap);
        try {
            return expressRunner.execute(codes, context, null, true, false);
        } finally {
            TEMP_VARS.remove();
        }
    }

    public static ExpressRunner getExpressRunner(String dbName) {
        dbName = StrUtil.blankToDefault(dbName, "");
        return RUNNER_MAP.computeIfAbsent(dbName, name -> {
            ExpressRunner runner = new ExpressRunner();
            ExpressPackage expressPackage = runner.getRootExpressPackage();
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.util");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.factory");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.http");
            expressPackage.addPackage("cn.hutool.core.util");
            expressPackage.addPackage("cn.hutool.core.collection");
            expressPackage.addPackage("cn.hutool.core.date");
            expressPackage.addPackage("cn.hutool.core.io");
            expressPackage.addPackage("cn.hutool.core.lang");
            expressPackage.addPackage("cn.hutool.core.map");

            try {
                Class<?>[] stringParamType = {String.class};

                runner.addFunctionOfClassMethod("dialog", CLASS_NAME, "dialog", new Class[]{Object.class}, null);
                runner.addFunctionOfClassMethod("list", CLASS_NAME, "list", new Class[]{Object[].class}, null);
                runner.addFunctionOfClassMethod("exec", CLASS_NAME, "exec", new Class[]{String.class}, null);
                runner.addFunctionOfClassMethod("input", CLASS_NAME, "input", stringParamType, null);
                runner.addFunctionOfClassMethod("processes", CLASS_NAME, "processes", stringParamType, null);
                Class<?>[] httpParamTypes = {String.class, String.class};
                runner.addFunctionOfClassMethod("http0", CLASS_NAME, "http0", httpParamTypes, null);
                httpParamTypes = new Class[]{int.class, String.class, String.class};
                runner.addFunctionOfClassMethod("http1", CLASS_NAME, "http1", httpParamTypes, null);
                Class<?>[] globalParamTypes = {String.class, Object.class};
                runner.addFunctionOfClassMethod("global", CLASS_NAME, "global", globalParamTypes, null);

                runner.addFunctionOfClassMethod("get", HttpUtil.class, "get", stringParamType, null);
                Class<?>[] runParamTypes = {String[].class};
                runner.addFunctionOfClassMethod("run", RuntimeUtil.class, "execForStr", runParamTypes, null);
                Class<?>[] postTypes = {String.class, String.class};
                runner.addFunctionOfClassMethod("post", HttpUtil.class, "post", postTypes, null);

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

    public static boolean http0(String api, String varKey) {
        return http1(HttpService.DEFAULT_PORT, api, varKey);
    }

    public static boolean http1(int port, String api, String varKey) {
        String dbName = ObjectUtil.toString(TEMP_VARS.get().get("dbName"));
        try {
            HttpService.exportHttp(port, api, (req, resp, params, body) -> {
                Map<String, Object> args = Map.of("req", req, "resp", resp, "params", params, "body", body);
                return execute(dbName, ObjectUtil.toString(GLOBAL_VARS.get(varKey)), args);
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public static void global(String key, Object value) {
        GLOBAL_VARS.put(key, value);
    }

    public static void put(String key, Object value) {
        TEMP_VARS.get().put(key, value);
    }

    public static Object exec(String varKey) {
        String dbName = ObjectUtil.toString(TEMP_VARS.get().get("dbName"));
        return execute(dbName, ObjectUtil.toString(GLOBAL_VARS.get(varKey)), TEMP_VARS.get());
    }

    public static List<OSProcess> processes(String name) {
        List<OSProcess> processes = OshiUtil.getOs().getProcesses();
        if (StrUtil.isEmpty(name)) {
            return processes;
        }
        return processes.stream().filter(process -> StrUtil.containsIgnoreCase(process.getName(), name)).collect(Collectors.toList());
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
        if (object instanceof List) {
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
        } else {
            FxDialogs.showInformation(header, ObjectUtil.toString(object));
        }
    }
}
