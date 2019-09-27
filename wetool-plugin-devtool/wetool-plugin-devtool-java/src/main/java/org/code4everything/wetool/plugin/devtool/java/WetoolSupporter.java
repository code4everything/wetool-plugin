package org.code4everything.wetool.plugin.devtool.java;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import org.code4everything.wetool.plugin.devtool.java.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/9/26
 */
public class WetoolSupporter implements WePluginSupportable {

    @Override
    public boolean initialize() {
        Menu menu = FxUtils.makePluginMenu(AppConsts.Title.DEV_TOOL);
        final WePluginSupportable that = this;
        menu.getItems().add(FxUtils.createMenuItem(CommonConsts.APP_NAME, (EventHandler<ActionEvent>) event -> {
            Node node = FxUtils.loadFxml(that, "/view/Main.fxml");
            FxUtils.openTab(node, CommonConsts.APP_ID, CommonConsts.APP_NAME);
        }));
        return true;
    }
}
