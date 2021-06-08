package org.code4everything.wetool.plugin.sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

/**
 * @author pantao
 * @since 2019/8/22
 */
@Slf4j
public class SampleController implements BaseViewController {

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
    private void initialize() {
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
    }

    @Override
    public void setFileContent(String content) {
        log.info("set file content");
        textArea.setText(content);
    }

    @Override
    public String getSavingContent() {
        return textArea.getText();
    }
}
