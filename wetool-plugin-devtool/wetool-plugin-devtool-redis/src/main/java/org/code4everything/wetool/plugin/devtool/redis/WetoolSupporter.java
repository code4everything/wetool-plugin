package org.code4everything.wetool.plugin.devtool.redis;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import org.code4everything.wetool.plugin.devtool.redis.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class WetoolSupporter implements WePluginSupporter {

    @Override
    public boolean initialize() {
        Menu menu = FxUtils.makePluginMenu(AppConsts.Title.DEV_TOOL);
        menu.getItems().add(FxUtils.createBarMenuItem(CommonConsts.APP_NAME, event -> openTab()));
        return true;
    }

    @Override
    public void debugCall() {
        openTab();
    }

    private void openTab() {
        Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/devtool/redis/Main.fxml", true);
        FxUtils.openTab(node, CommonConsts.APP_ID, CommonConsts.APP_NAME);
    }
}
