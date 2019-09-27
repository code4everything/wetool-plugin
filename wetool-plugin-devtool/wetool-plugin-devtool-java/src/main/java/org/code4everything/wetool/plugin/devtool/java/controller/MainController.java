package org.code4everything.wetool.plugin.devtool.java.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.devtool.java.constant.CommonConsts;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/9/26
 */
public class MainController implements BaseViewController {

    @FXML
    public TextField classFile;

    @FXML
    public TextField targetFile;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        classFile.setOnMouseClicked(e -> FxUtils.chooseFile(file -> classFile.setText(file.getAbsolutePath())));
    }

    public void chooseTargetFile() {
        FxUtils.saveFile(file -> targetFile.setText(file.getAbsolutePath()));
    }

    public void javap() {
        if (StrUtil.isEmpty(targetFile.getText())) {
            chooseTargetFile();
        }
        if (StrUtil.isNotEmpty(targetFile.getText())) {
            FileUtil.writeUtf8String(RuntimeUtil.execForStr("javap", "-c", classFile.getText()), targetFile.getText());
            FxUtils.openFile(targetFile.getText());
        }
    }
}
