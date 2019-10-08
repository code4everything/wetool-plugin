package org.code4everything.wetool.plugin.sample;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.sample.controller.SampleController;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolSupporter implements WePluginSupporter {

    /**
     * 初始化操作
     *
     * @return 初始化是否成功，返回true时继续加载插件，否则放弃加载
     */
    @Override
    public boolean initialize() {
        log.info("initialize sample plugin");
        return true;
    }

    /**
     * 注册插件到主界面菜单，可返回NULL，可不实现此方法
     *
     * @return 返回的 {@link MenuItem} 将被添加到主界面的插件菜单
     */
    @Override
    public MenuItem registerBarMenu() {
        final MenuItem item = new MenuItem("插件示例");
        // 自定义事件监听
        item.setOnAction(e -> {
            // 注意保证fxml文件的url路径唯一性
            Node node = FxUtils.loadFxml(this, "/ease/sample/Sample.fxml");
            FxDialogs.showInformation(SampleController.TAB_NAME, "welcome to wetool plugin");
            FxUtils.openTab(node, SampleController.TAB_ID, SampleController.TAB_NAME);
        });
        return item;
    }

    /**
     * 注册插件到系统托盘菜单，可返回NULL，可不实现此方法
     *
     * @return 返回的 {@link MenuItem} 将被添加到系统托盘的插件菜单
     */
    @Override
    public java.awt.MenuItem registerTrayMenu() {
        final java.awt.MenuItem item = new java.awt.MenuItem("插件示例");
        // 自定义事件监听
        item.addActionListener(e -> FxDialogs.showInformation(SampleController.TAB_NAME, "welcome to wetool plugin"));
        return item;
    }

    /**
     * 注册成功之后的回调
     */
    @Override
    public void registered(WePluginInfo info, MenuItem barMenu, java.awt.MenuItem trayMenu) {
        log.info("plugin sample registered success");
    }
}
