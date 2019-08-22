package org.code4everything.wetool.plugin.sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.code4everything.wetool.plugin.sample.controller.SampleController;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.util.Objects;

/**
 * 仅用于脱离WeTool的测试
 *
 * @author pantao
 * @since 2019/8/22
 */
public class SampleApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = FxUtils.loadFxml("/Sample.fxml");
        stage.setScene(new Scene(Objects.requireNonNull(root)));
        stage.setTitle(SampleController.TAB_NAME);
        stage.show();
    }
}
