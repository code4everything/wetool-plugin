package org.code4everything.wetool.plugin.thirdparty.downloader;

import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.Objects;

/**
 * @author pantao
 * @since 2020/1/14
 */
public class WetoolSupporter implements WePluginSupporter {

    private static final String TAB_ID = "ease-thirdparty-downloader";

    private static final String TAB_NAME = "蜗牛下载器";

    private boolean initialized = false;

    private Scene scene;

    @Override
    public MenuItem registerBarMenu() {
        MenuItem barMenuItem = FxUtils.createBarMenuItem(TAB_NAME, actionEvent -> initBootIfConfigured());
        barMenuItem.setId(TAB_ID);
        return barMenuItem;
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        return FxUtils.createTrayMenuItem(TAB_NAME, e -> Platform.runLater(() -> {
            FxUtils.getStage().show();
            initBootIfConfigured();
        }));
    }

    @Override
    public void debugCall() {
        Platform.runLater(this::initBootIfConfigured);
    }

    @Override
    public void initBootIfConfigured() {
        if (!initialized) {
            SystemContext.info();
            final boolean enable = SystemContext.listen();
            if (enable) {
                SystemContext.init();
                GuiManager.getInstance().init((String[]) null);
            }
            initialized = true;
        }

        Pane pane = FxUtils.loadFxml(WetoolSupporter.class, "/fxml/main.fxml", true);
        Objects.requireNonNull(pane);
        if (Objects.isNull(scene)) {
            scene = new Scene(pane);
        }
        scene.setRoot(pane);
        FxUtils.getStage().setScene(scene);
    }
}
