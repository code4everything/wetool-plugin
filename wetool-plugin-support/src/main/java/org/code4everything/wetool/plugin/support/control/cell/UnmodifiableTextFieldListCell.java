package org.code4everything.wetool.plugin.support.control.cell;

import cn.hutool.core.util.ReflectUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.lang.reflect.Field;

/**
 * @author pantao
 * @since 2019/9/24
 */
public class UnmodifiableTextFieldListCell<T> extends TextFieldListCell<T> {

    public UnmodifiableTextFieldListCell(StringConverter<T> converter) {
        super(converter);
    }

    public static Callback<ListView<String>, ListCell<String>> forListView() {
        return forListView(new DefaultStringConverter());
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final StringConverter<T> converter) {
        return list -> new UnmodifiableTextFieldListCell<>(converter);
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
