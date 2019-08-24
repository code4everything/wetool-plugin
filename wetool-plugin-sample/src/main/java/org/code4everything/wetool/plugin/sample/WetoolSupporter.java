package org.code4everything.wetool.plugin.sample;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.sample.controller.SampleController;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolSupporter implements WePluginSupportable {

    @Override
    public void initialize() {
        log.info("initialize sample plugin");
    }

    @Override
    public MenuItem registerBarMenu() {
        final MenuItem item = new MenuItem("插件示例");
        item.setOnAction(e -> {
            Node node = FxUtils.loadFxml(this, "/Sample.fxml");
            FxDialogs.showInformation(SampleController.TAB_NAME, "welcome to wetool plugin");
            FxUtils.openTab(node, SampleController.TAB_ID, SampleController.TAB_NAME);
        });
        return item;
    }

    @Override
    public java.awt.MenuItem registerTrayMenu() {
        final java.awt.MenuItem item = new java.awt.MenuItem("插件示例");
        item.addActionListener(e -> FxDialogs.showInformation(SampleController.TAB_NAME, "welcome to wetool plugin"));
        return item;
    }
}
