package org.code4everything.wetool.plugin.everywhere;

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
    public MenuItem registerBarMenu() {
        return FxUtils.createBarMenuItem(CommonConsts.APP_NAME, event -> initBootIfConfigured());
    }

    @Override
    public void initBootIfConfigured() {
        Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/everywhere/Main.fxml", true);
        FxUtils.openTab(node, CommonConsts.APP_ID, CommonConsts.APP_NAME);
    }
}
