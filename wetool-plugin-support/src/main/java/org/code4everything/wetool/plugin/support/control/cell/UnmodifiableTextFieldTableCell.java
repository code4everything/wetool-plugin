package org.code4everything.wetool.plugin.support.control.cell;

import cn.hutool.core.util.ReflectUtil;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.lang.reflect.Field;

/**
 * @author pantao
 * @since 2019/10/8
 */
public class UnmodifiableTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {

    public UnmodifiableTextFieldTableCell(StringConverter<T> converter) {
        super(converter);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter) {
        return list -> new UnmodifiableTextFieldTableCell<>(converter);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        try {
            Field field = ReflectUtil.getField(getClass(), "textField");
            field.setAccessible(true);
            ((TextField) field.get(this)).setEditable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
