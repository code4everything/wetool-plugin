package org.code4everything.wetool.plugin.thirdparty.downloader;

import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2020/1/14
 */
public class WetoolSupporter implements WePluginSupporter {

    private static final String TAB_ID = "ease-thirdparty-downloader";

    private static final String TAB_NAME = "蜗牛下载器";

    private boolean initialized = false;

    @Override
    public MenuItem registerBarMenu() {
        return FxUtils.createBarMenuItem(TAB_NAME, actionEvent -> openTab());
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        return FxUtils.createTrayMenuItem(TAB_NAME, e -> Platform.runLater(() -> {
            FxUtils.getStage().show();
            openTab();
        }));
    }

    @Override
    public void debugCall() {
        openTab();
    }

    private void openTab() {
        if (!initialized) {
            SystemContext.info();
            final boolean enable = SystemContext.listen();
            if (enable) {
                SystemContext.init();
                GuiManager.getInstance().init((String[]) null);
            }
            initialized = true;
        }
        Node node = FxUtils.loadFxml(WetoolSupporter.class, "/fxml/main.fxml", true);
        FxUtils.openTab(node, TAB_ID, TAB_NAME);
    }
}
