package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.*;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
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
import javafx.stage.DirectoryChooser;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.control.cell.UnmodifiableTextFieldTableCell;
import org.code4everything.wetool.plugin.support.druid.JdbcExecutor;
import org.code4everything.wetool.plugin.support.http.HttpService;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;
import oshi.software.os.OSProcess;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
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

    private static final ThreadLocal<DefaultContext<String, Object>> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    @SneakyThrows
    public static Object execute(String dbName, String codes, Map<String, Object> args) {
        if (StrUtil.isBlank(codes)) {
            return null;
        }

        if (codes.length() < 500 && codes.startsWith("file:")) {
            File file = FileUtil.file(StrUtil.removeSuffix(codes.substring(5), ";").trim());
            if (FileUtil.exist(file)) {
                String fileContent = FileUtil.readUtf8String(file);
                if (codes.equals(fileContent)) {
                    // 处理循环引用
                    FxDialogs.showError("非法调用");
                    return null;
                }
                return execute(dbName, fileContent, args);
            }
        }

        DefaultContext<String, Object> context = new DefaultContext<>();
        context.putAll(GLOBAL_VARS);
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
        CONTEXT_THREAD_LOCAL.set(context);
        try {
            return expressRunner.execute(codes, context, null, true, false);
        } finally {
            TEMP_VARS.remove();
            CONTEXT_THREAD_LOCAL.remove();
        }
    }

    public static ExpressRunner getExpressRunner(String dbName) {
        dbName = StrUtil.blankToDefault(dbName, "");
        return RUNNER_MAP.computeIfAbsent(dbName, name -> {
            ExpressRunner runner = new ExpressRunner();
            ExpressPackage expressPackage = runner.getRootExpressPackage();
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.util");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.factory");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.event");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.http");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.druid");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.cache");
            expressPackage.addPackage("org.code4everything.wetool.plugin.support.func");
            expressPackage.addPackage("com.alibaba.fastjson");
            expressPackage.addPackage("cn.hutool.core.util");
            expressPackage.addPackage("cn.hutool.core.collection");
            expressPackage.addPackage("cn.hutool.core.date");
            expressPackage.addPackage("cn.hutool.core.io");
            expressPackage.addPackage("cn.hutool.core.lang");
            expressPackage.addPackage("cn.hutool.core.map");

            try {
                importInnerMethods(runner);
                importOuterMethods(runner);

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

    private static void importInnerMethods(ExpressRunner runner) throws Exception {
        Class<?>[] types = {};
        runner.addFunctionOfClassMethod("chooseSaveFile", CLASS_NAME, "chooseSaveFile", types, null);
        runner.addFunctionOfClassMethod("chooseMultiFile", CLASS_NAME, "chooseMultiFile", types, null);
        runner.addFunctionOfClassMethod("chooseFile", CLASS_NAME, "chooseFile", types, null);
        runner.addFunctionOfClassMethod("chooseFolder", CLASS_NAME, "chooseFolder", types, null);

        types = new Class<?>[]{Object.class};
        runner.addFunctionOfClassMethod("dialog", CLASS_NAME, "dialog", types, null);
        runner.addFunctionOfClassMethod("append", CLASS_NAME, "append", types, null);

        types = new Class<?>[]{Object[].class};
        runner.addFunctionOfClassMethod("list", CLASS_NAME, "list", types, null);

        types = new Class<?>[]{String.class};
        runner.addFunctionOfClassMethod("exec", CLASS_NAME, "exec", types, null);
        runner.addFunctionOfClassMethod("input", CLASS_NAME, "input", types, null);
        runner.addFunctionOfClassMethod("processes", CLASS_NAME, "processes", types, null);
        runner.addFunctionOfClassMethod("pushThisEvent2Remote", CLASS_NAME, "pushThisEvent2Remote", types, null);
        runner.addFunctionOfClassMethod("getGlobal", CLASS_NAME, "getGlobal", types, null);

        types = new Class<?>[]{List.class};
        runner.addFunctionOfClassMethod("random", CLASS_NAME, "random", types, null);

        types = new Class<?>[]{String.class, List.class};
        runner.addFunctionOfClassMethod("choice", CLASS_NAME, "choice", types, null);

        types = new Class<?>[]{String.class, String.class};
        runner.addFunctionOfClassMethod("http0", CLASS_NAME, "http0", types, null);
        runner.addFunctionOfClassMethod("request", CLASS_NAME, "request", types, null);

        types = new Class<?>[]{int.class, String.class, String.class};
        runner.addFunctionOfClassMethod("http1", CLASS_NAME, "http1", types, null);

        types = new Class<?>[]{String.class, Object.class};
        runner.addFunctionOfClassMethod("global", CLASS_NAME, "global", types, null);

        types = new Class<?>[]{String.class, Object[].class};
        runner.addFunctionOfClassMethod("log", CLASS_NAME, "log", types, null);

        types = new Class<?>[]{String.class, String[].class};
        runner.addFunctionOfClassMethod("join", CLASS_NAME, "join", types, null);

        types = new Class<?>[]{File.class, String.class};
        runner.addFunctionOfClassMethod("save", CLASS_NAME, "save", types, null);
    }

    private static void importOuterMethods(ExpressRunner runner) throws Exception {
        Class<?>[] types = {};
        runner.addFunctionOfClassMethod("getClipboard", ClipboardUtil.class, "getStr", types, null);

        types = new Class<?>[]{String.class};
        runner.addFunctionOfClassMethod("get", HttpUtil.class, "get", types, null);
        runner.addFunctionOfClassMethod("setClipboard", ClipboardUtil.class, "setStr", types, null);

        types = new Class<?>[]{String[].class};
        runner.addFunctionOfClassMethod("run", RuntimeUtil.class, "execForStr", types, null);

        types = new Class<?>[]{String.class, String.class};
        runner.addFunctionOfClassMethod("post", HttpUtil.class, "post", types, null);

        types = new Class<?>[]{CharSequence.class, Object[].class};
        runner.addFunctionOfClassMethod("format", StrUtil.class, "format", types, null);

        types = new Class<?>[]{Object.class, String.class};
        runner.addFunctionOfClassMethod("evalJson", JSONPath.class, "eval", types, null);
    }

    @SneakyThrows
    public static String choice(String tip, List<String> items) {
        return FxDialogs.showChoice(MainController.TAB_NAME, tip, items).get();
    }

    public static HttpRequest request(String method, String url) {
        method = StrUtil.trim(method).toLowerCase();
        switch (method) {
            case "post":
                return HttpRequest.post(url);
            case "put":
                return HttpRequest.put(url);
            case "head":
                return HttpRequest.head(url);
            case "patch":
                return HttpRequest.patch(url);
            case "delete":
                return HttpRequest.delete(url);
            case "trace":
                return HttpRequest.trace(url);
            case "get":
                return HttpRequest.get(url);
            default:
                return HttpRequest.options(url);
        }
    }

    public static Object getGlobal(String key) {
        return GLOBAL_VARS.get(key);
    }

    public static Object random(List<Object> list) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.get(RandomUtil.randomInt(0, list.size()));
    }

    public static String join(String delimiter, String... params) {
        if (ArrayUtil.isEmpty(params)) {
            return StrUtil.EMPTY;
        }
        StringJoiner joiner = new StringJoiner(delimiter);
        for (String param : params) {
            joiner.add(param);
        }
        return joiner.toString();
    }

    public static StringBuilder append(Object param) {
        String key = "java_lang_StringBuilder";
        Object builder = TEMP_VARS.get().get(key);

        StringBuilder stringBuilder;
        if (Objects.isNull(builder)) {
            stringBuilder = new StringBuilder();
            TEMP_VARS.get().put(key, stringBuilder);
        } else if (builder instanceof StringBuilder) {
            stringBuilder = (StringBuilder) builder;
        } else {
            TEMP_VARS.get().remove(key);
            return append(param);
        }

        return stringBuilder.append(param);
    }

    @SneakyThrows
    public static File chooseSaveFile() {
        FutureTask<File> task = new FutureTask<>(() -> FxUtils.getFileChooser().showSaveDialog(FxUtils.getStage()));
        FxDialogs.execFxFutureTask(task);
        File file = task.get();
        FxUtils.handleFileCallable(file, null);
        return file;
    }

    public static boolean save(File file, String content) {
        if (Objects.isNull(file)) {
            return false;
        }
        FileUtil.writeUtf8String(content, file);
        return true;
    }

    @SneakyThrows
    public static List<File> chooseMultiFile() {
        FutureTask<List<File>> task = new FutureTask<>(() -> FxUtils.getFileChooser().showOpenMultipleDialog(FxUtils.getStage()));
        FxDialogs.execFxFutureTask(task);
        List<File> files = task.get();
        FxUtils.handleFileListCallable(files, null);
        return files;
    }

    @SneakyThrows
    public static File chooseFile() {
        FutureTask<File> task = new FutureTask<>(() -> FxUtils.getFileChooser().showOpenDialog(FxUtils.getStage()));
        FxDialogs.execFxFutureTask(task);
        File file = task.get();
        FxUtils.handleFileCallable(file, null);
        return file;
    }

    @SneakyThrows
    public static File chooseFolder() {
        FutureTask<File> task = new FutureTask<>(() -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(AppConsts.Title.APP_TITLE);
            chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
            return chooser.showDialog(FxUtils.getStage());
        });
        FxDialogs.execFxFutureTask(task);
        File file = task.get();
        FxUtils.handleFileCallable(file, null);
        return file;
    }

    public static void pushThisEvent2Remote(String postApi) {
        String body = JSON.toJSONString(CONTEXT_THREAD_LOCAL.get());
        log.debug("push event to remote: {}", body);
        HttpUtil.post(postApi, body);
    }

    public static boolean http0(String api, String varKey) {
        return http1(HttpService.getDefaultPort(), api, varKey);
    }

    public static boolean http1(int port, String api, String varKey) {
        String dbName = ObjectUtil.toString(getTempVarsMap().get("dbName"));
        try {
            HttpService.exportHttp(port, api, (req, resp, params, body) -> {
                Map<String, Object> args = Map.of("req", req, "resp", resp, "params", params, "body", body);
                getTempVarsMap().putAll(args);
                getTempVarsMap().put("dbName", dbName);
                try {
                    return exec(varKey);
                } finally {
                    TEMP_VARS.remove();
                }
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
        getTempVarsMap().put(key, value);
    }

    @SneakyThrows
    public static Object exec(String varKey) {
        Map<String, Object> args = getTempVarsMap();
        String dbName = ObjectUtil.toString(args.get("dbName"));
        String codes = StrUtil.emptyToDefault(ObjectUtil.toString(GLOBAL_VARS.get(varKey)), varKey);
        Future<Object> future = WeUtils.executeAsync(() -> execute(dbName, codes, args));
        return future.get();
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
        return ListUtil.list(false, args);
    }

    @SuppressWarnings({"rawtypes"})
    public static void dialog(Object object) {
        if (Objects.isNull(object)) {
            return;
        }
        String header = "结果";
        if (object instanceof List) {
            List<?> list = (List) object;
            if (CollUtil.isNotEmpty(list)) {
                list = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
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
                        Object value = param.getValue().get(k);
                        return new SimpleObjectProperty<>(Objects.isNull(value) ? StrUtil.EMPTY : ObjectUtil.toString(value));
                    }
                });
                tableView.getColumns().add(tableColumn);
            });

            tableView.setEditable(true);
            tableView.getItems().addAll(tableList);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            vBox.getChildren().add(tableView);
            vBox.setPrefWidth(1000);
            FxDialogs.showDialog(header, vBox);
        } else {
            FxDialogs.showInformation(header, ObjectUtil.toString(object));
        }
    }

    private static Map<String, Object> getTempVarsMap() {
        Map<String, Object> map = TEMP_VARS.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>(8);
            TEMP_VARS.set(map);
        }
        return map;
    }
}
