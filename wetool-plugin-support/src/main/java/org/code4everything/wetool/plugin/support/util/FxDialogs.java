package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.util.ObjectUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.constant.AppConsts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author pantao
 * @since 2019/7/5
 **/
@Slf4j
@UtilityClass
public class FxDialogs {

    /**
     * 弹出对话框
     *
     * @param header 头部，可为Null
     * @param dialogPane 对话框的视图内容
     */
    public static void showDialog(String header, Node dialogPane) {
        showDialog(header, dialogPane, null, null);
    }

    /**
     * 弹出对话框
     *
     * @param header 头部，可为Null
     * @param dialogPane 对话框的视图内容
     * @param winnable 对话框的结果处理回调，可为Null
     * @param <R> 结果类型
     */
    public static <R> void showDialog(String header, Node dialogPane, DialogWinnable<R> winnable) {
        showDialog(header, dialogPane, winnable, null);
    }

    /**
     * 弹出对话框
     *
     * @param header 头部，可为Null
     * @param dialogPane 对话框的视图内容
     * @param winnable 对话框的结果处理回调，可为Null
     * @param defaultR 默认结果，可为Null
     * @param <R> 结果类型
     */
    public static <R> void showDialog(String header, Node dialogPane, DialogWinnable<R> winnable, R defaultR) {
        Platform.runLater(() -> {
            Dialog<R> dialog = new Dialog<>();
            dialog.setTitle(AppConsts.Title.APP_TITLE);
            dialog.setHeaderText(header);
            dialog.setResizable(true);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.getDialogPane().setContent(dialogPane);

            ButtonType ok = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
            if (ObjectUtil.isNotNull(winnable)) {
                dialog.setResultConverter(param -> {
                    if (param.getButtonData().isDefaultButton()) {
                        return winnable.convertResult();
                    }
                    return defaultR;
                });
                dialog.getDialogPane().getButtonTypes().add(ok);
            }
            ButtonType cancel = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(cancel);

            Optional<R> result = dialog.showAndWait();
            if (ObjectUtil.isNotNull(winnable)) {
                winnable.consumeResult(result.orElse(defaultR));
            }
        });
    }

    /**
     * 弹出Choice选择框
     *
     * @param header 头部，可为Null
     * @param content 提示内容，可为Null
     * @param consumer，处理结果的回调
     * @param items Choice可有的选项
     * @param <T> 结果类型
     */
    public static <T> void showChoice(String header, String content, Consumer<T> consumer,
                                      Collection<? extends T> items) {
        Platform.runLater(() -> {
            ChoiceDialog<T> dialog = new ChoiceDialog<>();
            dialog.setTitle(AppConsts.Title.APP_TITLE);
            dialog.setHeaderText(header);
            dialog.setContentText(content);
            dialog.setResizable(true);
            dialog.getItems().addAll(items);
            Optional<T> result = dialog.showAndWait();
            if (ObjectUtil.isNotNull(consumer)) {
                consumer.accept(result.orElse(null));
            }
        });
    }

    /**
     * 弹出文本输入框
     *
     * @param header 头部，可为Null
     * @param content 提示内容，可为Null
     * @param consumer 处理结果的回调
     */
    public static void showTextInput(String header, String content, Consumer<String> consumer) {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(AppConsts.Title.APP_TITLE);
            dialog.setHeaderText(header);
            dialog.setContentText(content);
            dialog.setResizable(true);
            Optional<String> result = dialog.showAndWait();
            if (ObjectUtil.isNotNull(consumer)) {
                consumer.accept(result.orElse(""));
            }
        });
    }

    public static void showSuccess() {
        showInformation(null, AppConsts.Tip.OPERATION_SUCCESS);
    }

    public static void showInformation(String header, String content) {
        showAlert(header, content, Alert.AlertType.INFORMATION);
    }

    public static void showError(String content) {
        showAlert(null, content, Alert.AlertType.ERROR);
    }

    public static void showException(String header, Exception e) {
        Platform.runLater(() -> {
            Alert alert = makeAlert(header, "错误信息追踪：", Alert.AlertType.ERROR, Modality.APPLICATION_MODAL);

            // 输出异常信息
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String exception = stringWriter.toString();
            log.error(exception);

            // 异常信息容易
            TextArea textArea = new TextArea(exception);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            // 设置大小
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            // 添加至面板
            GridPane gridPane = new GridPane();
            gridPane.setMaxWidth(Double.MAX_VALUE);
            gridPane.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(gridPane);
            alert.showAndWait();
        });
    }

    private static void showAlert(String header, String content, Alert.AlertType alertType) {
        Platform.runLater(() -> makeAlert(header, content, alertType, Modality.NONE).showAndWait());
    }

    private static Alert makeAlert(String header, String content, Alert.AlertType alertType, Modality modality) {
        Alert alert = new Alert(alertType);

        alert.setTitle(AppConsts.Title.APP_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.initModality(modality);
        alert.initStyle(StageStyle.DECORATED);

        return alert;
    }
}
