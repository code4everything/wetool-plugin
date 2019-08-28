package org.code4everything.wetool.plugin.qiniu.util;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author pantao
 * @since 2019/8/28
 */
@Slf4j
@UtilityClass
public class Dialogs {

    private static final String CANCEL_BUTTON_TEXT = "取消";

    /**
     * 弹出输入框
     *
     * @param title 标题
     * @param header 信息头
     * @param content 内容
     * @param defaultValue 输入框默认值
     *
     * @return 输入的内容
     */
    public static String showInputDialog(String title, String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setResizable(true);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 获取一个{@link GridPane}对象
     *
     * @return {@link GridPane}
     */
    public static GridPane getGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        return grid;
    }

    /**
     * 获得一个{@link Dialog}对象
     *
     * @param title 标题
     * @param ok 确认按钮
     *
     * @return {@link Dialog}
     */
    public static Dialog<String[]> getDialog(String title, ButtonType ok) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        dialog.initModality(Modality.APPLICATION_MODAL);

        // 自定义确认和取消按钮
        ButtonType cancel = new ButtonType(CANCEL_BUTTON_TEXT, ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);
        return dialog;
    }
}
