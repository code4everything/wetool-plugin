package org.code4everything.wetool.plugin.dbops;

import com.ql.util.express.DynamicParamsUtil;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public boolean initialize() {
        DynamicParamsUtil.supportDynamicParams = true;
        // 初始化时加载视图，让监听事件的脚本可以后台运行
        FxUtils.loadFxml(WetoolSupporter.class, "/ease/dbops/MainView.fxml", true);
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
