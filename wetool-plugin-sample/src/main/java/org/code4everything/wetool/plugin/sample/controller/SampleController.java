package org.code4everything.wetool.plugin.sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import org.code4everything.wetool.plugin.support.WePluginInfo;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

/**
 * @author pantao
 * @since 2019/8/22
 */
public class SampleController implements WePluginSupportable {

    /**
     * 自定义tabId，用来防止与其他插件发生名称冲突
     */
    public static final String TAB_ID = "sample";

    /**
     * 自定义tabName，Tab选项卡的标题
     */
    public static final String TAB_NAME = "插件示例";

    @FXML
    public TextArea textArea;

    @FXML
    public void initialize() {
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
    }

    @Override
    public WePluginInfo getInfo() {
        return null;
    }

    @Override
    public MenuItem registerPlugin() {
        return null;
    }
}
