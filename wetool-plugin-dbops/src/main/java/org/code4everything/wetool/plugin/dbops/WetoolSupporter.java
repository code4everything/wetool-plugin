package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.collection.CollUtil;
import com.ql.util.express.DynamicParamsUtil;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public boolean initialize() {
        if (CollUtil.isEmpty(DruidSource.listAllNames())) {
            log.info("database connection doesn't configured, could not load dbops plugin.");
            return false;
        }
        DynamicParamsUtil.supportDynamicParams = true;
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
