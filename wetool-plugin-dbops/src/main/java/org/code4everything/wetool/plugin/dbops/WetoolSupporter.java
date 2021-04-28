package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jfoenix.controls.JFXDatePicker;
import com.ql.util.express.DynamicParamsUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.event.EventMode;
import org.code4everything.wetool.plugin.support.event.EventPublisher;
import org.code4everything.wetool.plugin.support.func.FunctionCenter;
import org.code4everything.wetool.plugin.support.func.MethodCallback;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public boolean initialize() {
        DynamicParamsUtil.supportDynamicParams = true;

        ScriptExecutor.GLOBAL_VARS.put("currDir", FileUtils.currentWorkDir());
        ScriptExecutor.GLOBAL_VARS.put("lineSep", FileUtil.getLineSeparator());
        ScriptExecutor.GLOBAL_VARS.put("fileSep", File.separator);
        ScriptExecutor.GLOBAL_VARS.put("userHome", FileUtil.getUserHomePath());

        // 初始化时加载视图，让监听事件的脚本可以后台运行
        FxUtils.loadFxml(WetoolSupporter.class, "/ease/dbops/MainView.fxml", true);

        Optional<EventPublisher> optional = EventCenter.registerEvent("wetool_dbops_initialized", EventMode.MULTI_SUB);
        optional.ifPresent(eventPublisher -> eventPublisher.publishEvent(DateUtil.date()));

        // 注册脚本执行方法
        FunctionCenter.registerFunc(new MethodCallback() {
            @Override
            public String getUniqueMethodName() {
                return "ease-dbops-script-execute";
            }

            @Override
            public String getDescription() {
                // @formatter:off
                return "执行QL脚本，对应方法：ScriptExecutor.execute(@Nullable String dbName, String codes, @Nullable Map<String, Object> args)";
                // @formatter:on
            }

            @Override
            public List<Class<?>> getParamTypes() {
                return List.of(String.class, String.class, Map.class);
            }

            @Override
            @SuppressWarnings("unchecked")
            public Object callMethod(List<Object> params) {
                return ScriptExecutor.execute((String) params.get(0), (String) params.get(1), (Map<String, Object>) params.get(2));
            }
        });

        FxUtils.registerAction("dbops global*", event -> {
            String name = StrUtil.removePrefix(event.getSource().toString(), "dbops global").trim();
            String value = ObjectUtil.toString(ScriptExecutor.GLOBAL_VARS.get(name));
            FxDialogs.showTextAreaDialog(StrUtil.format("在DBOPS中 {} 的全局变量", name), value);
        });

        return true;
    }

    @Override
    public MenuItem registerBarMenu() {
        return FxUtils.createBarMenuItem(MainController.TAB_NAME, actionEvent -> debugCall());
    }

    @Override
    public void initBootIfConfigured() {
        Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/dbops/MainView.fxml", true);
        FxUtils.openTab(node, MainController.TAB_ID, MainController.TAB_NAME);
    }
}
