package org.code4everything.wetool.plugin.devtool.java.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.devtool.java.constant.CommonConsts;
import org.code4everything.wetool.plugin.devtool.java.loader.DevClassLoader;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxUtils;


/**
 * @author pantao
 * @since 2019/9/26
 */
public class MainController implements BaseViewController {

    @FXML
    public TextField class4Javap;

    @FXML
    public TextField targetFile;

    @FXML
    public TextField class4BeanJson;

    @FXML
    public TextField classes4BeanJson;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        class4Javap.setOnMouseClicked(e -> FxUtils.chooseFile(file -> class4Javap.setText(file.getAbsolutePath())));
        class4BeanJson.setOnMouseClicked(e -> FxUtils.chooseFile(file -> class4BeanJson.setText(file.getAbsolutePath())));
        classes4BeanJson.setOnMouseClicked(e -> FxUtils.chooseFolder(file -> classes4BeanJson.setText(file.getAbsolutePath())));
    }

    public void chooseTargetFile() {
        FxUtils.saveFile(file -> targetFile.setText(file.getAbsolutePath()));
    }

    public void javap() {
        if (StrUtil.isEmpty(targetFile.getText())) {
            chooseTargetFile();
        }
        if (StrUtil.isNotEmpty(targetFile.getText())) {
            FileUtil.writeUtf8String(RuntimeUtil.execForStr("javap", "-c", class4Javap.getText()),
                    targetFile.getText());
            FxUtils.openFile(targetFile.getText());
        }
    }

    public void convertBean2Json() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (FileUtil.exist(class4BeanJson.getText())) {
            DevClassLoader loader = new DevClassLoader(classes4BeanJson.getText(), class4BeanJson.getText());
            Class<?> clazz = loader.loadClass(null);
            if (ObjectUtil.isNull(clazz)) {
                return;
            }
            Object object = clazz.newInstance();
            FxUtils.saveFile(file -> {
                FileUtil.writeUtf8String(JSON.toJSONString(object, SerializerFeature.WriteMapNullValue,
                        SerializerFeature.PrettyFormat), file);
                FxUtils.openFile(file);
            });
        }
    }
}
