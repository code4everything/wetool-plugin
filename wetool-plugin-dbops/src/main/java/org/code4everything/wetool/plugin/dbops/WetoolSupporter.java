package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.ql.util.express.DynamicParamsUtil;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.event.EventMode;
import org.code4everything.wetool.plugin.support.event.EventPublisher;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.util.Optional;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public boolean initialize() {
        Optional<EventPublisher> optional = EventCenter.registerEvent("wetool_dbops_initialized", EventMode.MULTI_SUB);
        ScriptExecutor.GLOBAL_VARS.put("currDir", FileUtils.currentWorkDir());
        ScriptExecutor.GLOBAL_VARS.put("lineSep", FileUtil.getLineSeparator());
        ScriptExecutor.GLOBAL_VARS.put("fileSep", File.separator);
        ScriptExecutor.GLOBAL_VARS.put("userHome", FileUtil.getUserHomePath());
        DynamicParamsUtil.supportDynamicParams = true;
        // 初始化时加载视图，让监听事件的脚本可以后台运行
        FxUtils.loadFxml(WetoolSupporter.class, "/ease/dbops/MainView.fxml", true);
        optional.ifPresent(eventPublisher -> eventPublisher.publishEvent(DateUtil.date()));
        return true;
    }

    @Override
    public MenuItem registerBarMenu() {
        return FxUtils.createBarMenuItem(MainController.TAB_NAME, actionEvent -> debugCall());
    }

    @Override
    public void debugCall() {
        Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/dbops/MainView.fxml", true);
        FxUtils.openTab(node, MainController.TAB_ID, MainController.TAB_NAME);
    }
}
