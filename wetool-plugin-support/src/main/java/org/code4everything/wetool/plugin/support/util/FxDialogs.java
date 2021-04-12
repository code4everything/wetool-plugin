package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.control.EditableChoiceDialog;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

/**
 * @author pantao
 * @since 2019/7/5
 **/
@Slf4j
@UtilityClass
public class FxDialogs {

    /**
     * 弹出文本框
     *
     * @param header 标题
     * @param text 内容
     *
     * @since 1.5.0
     */
    public static void showTextAreaDialog(String header, String text) {
        TextArea codeArea = new TextArea();
        codeArea.setPrefWidth(1000);
        codeArea.setPrefHeight(800);
        codeArea.appendText(text);
        showDialog(header, codeArea);
    }

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
        execFxFutureTask(() -> {
            Dialog<R> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(dialogPane);
            buildDialog(dialog, header, null);

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
     * @param items Choice可有的选项
     */
    public static void showChoice(String header, String content, Consumer<String> consumer, Collection<String> items) {
        execFxFutureTask(() -> {
            EditableChoiceDialog dialog = getEditableChoiceDialog(header, content, items);
            Optional<String> result = dialog.showAndWait();
            if (ObjectUtil.isNotNull(consumer)) {
                consumer.accept(result.orElse(null));
            }
        });
    }

    public static <T> ChoiceDialog<T> getChoiceDialog(String header, String content, Collection<? extends T> items) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>();
        buildDialog(dialog, header, content);
        dialog.getItems().addAll(items);
        return dialog;
    }

    public static EditableChoiceDialog getEditableChoiceDialog(String header, String content,
                                                               Collection<String> items) {
        EditableChoiceDialog dialog = new EditableChoiceDialog(null, items);
        buildDialog(dialog, header, content);
        return dialog;
    }

    private static void buildDialog(Dialog<?> dialog, String header, String content) {
        dialog.setTitle(AppConsts.Title.APP_TITLE);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.setResizable(true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        WeUtils.getConfig().darkIfEnabled(dialog.getDialogPane().getStylesheets()::add);
    }

    public static Future<String> showChoice(String header, String content, Collection<String> items) {
        FutureTask<String> task = new FutureTask<>(() -> {
            EditableChoiceDialog dialog = getEditableChoiceDialog(header, content, items);
            return dialog.showAndWait().orElse("");
        });
        execFxFutureTask(task);
        return task;
    }

    /**
     * 弹出文本输入框
     *
     * @param header 头部，可为Null
     * @param content 提示内容，可为Null
     * @param consumer 处理结果的回调
     */
    public static void showTextInput(String header, String content, Consumer<String> consumer) {
        execFxFutureTask(() -> {
            TextInputDialog dialog = getTextInputDialog(header, content);
            Optional<String> result = dialog.showAndWait();
            if (ObjectUtil.isNotNull(consumer)) {
                consumer.accept(result.orElse(""));
            }
        });
    }

    /**
     * 弹出文本输入框
     *
     * @param header 头部，可为Null
     * @param content 提示内容，可为Null
     */
    public static Future<String> showTextInput(String header, String content) {
        FutureTask<String> future = new FutureTask<>(() -> {
            TextInputDialog dialog = getTextInputDialog(header, content);
            return dialog.showAndWait().orElse("");
        });
        execFxFutureTask(future);
        return future;
    }

    public static TextInputDialog getTextInputDialog(String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setWidth(500);
        dialog.getDialogPane().setPrefWidth(500);
        buildDialog(dialog, header, content);
        return dialog;
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

    public static void showException(String header, Throwable e) {
        execFxFutureTask(() -> {
            Alert alert = makeAlert(header, "错误信息追踪：", Alert.AlertType.ERROR, Modality.APPLICATION_MODAL);

            // 输出异常信息
            String exception = ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE);
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
            gridPane.setPrefWidth(900);
            gridPane.setPrefHeight(600);
            gridPane.setMaxWidth(Double.MAX_VALUE);
            gridPane.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(gridPane);
            alert.showAndWait();
        });
    }

    private static void showAlert(String header, String content, Alert.AlertType alertType) {
        execFxFutureTask(() -> makeAlert(header, content, alertType, Modality.NONE).showAndWait());
    }

    private static Alert makeAlert(String header, String content, Alert.AlertType alertType, Modality modality) {
        Alert alert = new Alert(alertType);

        alert.setTitle(AppConsts.Title.APP_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.initModality(modality);
        alert.initStyle(StageStyle.DECORATED);

        WeUtils.getConfig().darkIfEnabled(alert.getDialogPane().getStylesheets()::add);
        return alert;
    }

    public void execFxFutureTask(Runnable runnable) {
        if (Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            runnable.run();
        } else {
            execFxFutureTask(runnable);
        }
    }
}
