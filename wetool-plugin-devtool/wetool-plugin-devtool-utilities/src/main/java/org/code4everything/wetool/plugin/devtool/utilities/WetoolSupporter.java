package org.code4everything.wetool.plugin.devtool.utilities;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import org.code4everything.wetool.plugin.devtool.utilities.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/11/4
 */
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public boolean initialize() {
        Menu menu = FxUtils.makePluginMenu(AppConsts.Title.DEV_TOOL);
        menu.getItems().add(FxUtils.createMenuItem(CommonConsts.APP_NAME, (EventHandler<ActionEvent>) event -> {
            Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/devtool/utilities/Main.fxml", true);
            FxUtils.openTab(node, CommonConsts.APP_ID, CommonConsts.APP_NAME);
        }));
        return true;
    }
}
