package org.code4everything.wetool.plugin.everywhere;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public void debugCall() {
        openTab();
    }

    @Override
    public MenuItem registerBarMenu() {
        return FxUtils.createMenuItem(CommonConsts.APP_NAME, (EventHandler<ActionEvent>) event -> openTab());
    }

    private void openTab() {
        Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/everywhere/Main.fxml", true);
        FxUtils.openTab(node, CommonConsts.APP_ID, CommonConsts.APP_NAME);
    }
}
