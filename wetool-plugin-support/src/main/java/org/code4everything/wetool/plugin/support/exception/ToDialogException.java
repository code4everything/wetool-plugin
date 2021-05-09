package org.code4everything.wetool.plugin.support.exception;

import cn.hutool.core.util.StrUtil;
import javafx.scene.control.Alert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author pantao
 * @since 2021/5/9
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ToDialogException extends RuntimeException {

    protected String title;

    protected String msg;

    protected Alert.AlertType type = Alert.AlertType.NONE;

    public ToDialogException(String msg, String... args) {
        setMsg(StrUtil.format(msg, args));
    }

    public static ToDialogException ofError(String msg, String... args) {
        return new ToDialogException(msg, args).setType(Alert.AlertType.ERROR);
    }

    public static ToDialogException ofInfo(String msg, String... args) {
        return new ToDialogException(msg, args).setType(Alert.AlertType.INFORMATION);
    }

    public static ToDialogException ofWarn(String msg, String... args) {
        return new ToDialogException(msg, args).setType(Alert.AlertType.WARNING);
    }

    public static ToDialogException ofConfirm(String msg, String... args) {
        return new ToDialogException(msg, args).setType(Alert.AlertType.CONFIRMATION);
    }
}
