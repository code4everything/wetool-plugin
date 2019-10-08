package org.code4everything.wetool.plugin.qiniu;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import org.code4everything.wetool.plugin.qiniu.constant.QiniuConsts;
import org.code4everything.wetool.plugin.qiniu.util.ConfigUtils;
import org.code4everything.wetool.plugin.qiniu.util.DialogUtils;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.util.Callable;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.Optional;

/**
 * @author pantao
 * @since 2019/8/27
 */
public class WetoolSupporter implements WePluginSupporter, Callable<Tab> {

    @Override
    public MenuItem registerBarMenu() {
        MenuItem item = new MenuItem(QiniuConsts.TAB_NAME);
        item.setOnAction(event -> {
            Node node = FxUtils.loadFxml(this, QiniuConsts.QINIU_VIEW_URL);
            FxUtils.openTab(node, QiniuConsts.TAB_ID, QiniuConsts.TAB_NAME, this);
            ConfigUtils.loadConfig();
        });
        return item;
    }

    @Override
    public void call(Tab tab) {
        tab.setOnCloseRequest(event -> {
            Optional<ButtonType> result = DialogUtils.showConfirmation(QiniuConsts.CONFIRM_EXIT);
            if (result.isPresent() && result.get() != ButtonType.OK) {
                // 取消退出事件
                event.consume();
            }
        });
    }
}
