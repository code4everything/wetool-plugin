package org.code4everything.wetool.plugin.dbops;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class WetoolSupporter implements WePluginSupporter {

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
