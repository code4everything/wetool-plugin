package org.code4everything.wetool.plugin.support.control;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * @author pantao
 * @since 2020/12/29
 */
public class EditableChoiceDialog extends Dialog<String> {

    private final GridPane grid;

    private final Label label;

    private final ComboBox<String> comboBox;

    private final String defaultChoice;

    public EditableChoiceDialog() {
        this(null);
    }

    public EditableChoiceDialog(String defaultValue, String... items) {
        this(defaultValue, items == null ? Collections.emptyList() : Arrays.asList(items));
    }

    public EditableChoiceDialog(String defaultValue, Collection<String> items) {
        DialogPane dialogPane = this.getDialogPane();
        this.grid = new GridPane();
        this.grid.setHgap(10.0D);
        this.grid.setMaxWidth(1.7976931348623157E308D);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.label = createContentLabel(dialogPane.getContentText());
        this.label.setPrefWidth(-1.0D);
        this.label.textProperty().bind(dialogPane.contentTextProperty());
        dialogPane.contentTextProperty().addListener(e -> this.updateGrid());
        this.setTitle(ResourceBundle.getBundle("com/sun/javafx/scene/control/skin/resources/controls").getString(
                "Dialog.confirm.title"));
        dialogPane.setHeaderText(ResourceBundle.getBundle("com/sun/javafx/scene/control/skin/resources/controls").getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("choice-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        this.comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        this.comboBox.setMinWidth(300);
        if (items != null) {
            this.comboBox.getItems().addAll(items);
        }

        this.comboBox.setMaxWidth(1.7976931348623157E308D);
        GridPane.setHgrow(this.comboBox, Priority.ALWAYS);
        GridPane.setFillWidth(this.comboBox, true);
        this.defaultChoice = this.comboBox.getItems().contains(defaultValue) ? defaultValue : null;
        if (defaultValue == null) {
            this.comboBox.getSelectionModel().selectFirst();
        } else {
            this.comboBox.getSelectionModel().select(defaultValue);
        }

        this.updateGrid();
        this.setResultConverter(e -> {
            ButtonBar.ButtonData result = e == null ? null : e.getButtonData();
            return result == ButtonBar.ButtonData.CANCEL_CLOSE ? null : this.comboBox.getEditor().getText();
        });
    }

    public final String getSelectedItem() {
        return this.comboBox.getSelectionModel().getSelectedItem();
    }

    public final void setSelectedItem(String content) {
        this.comboBox.getSelectionModel().select(content);
    }

    public final ReadOnlyObjectProperty<String> selectedItemProperty() {
        return this.comboBox.getSelectionModel().selectedItemProperty();
    }

    public final ObservableList<String> getItems() {
        return this.comboBox.getItems();
    }

    public final String getDefaultChoice() {
        return this.defaultChoice;
    }

    private void updateGrid() {
        this.grid.getChildren().clear();
        this.grid.add(this.label, 0, 0);
        this.grid.add(this.comboBox, 1, 0);
        this.getDialogPane().setContent(this.grid);
        Platform.runLater(this.comboBox::requestFocus);
    }

    private Label createContentLabel(String content) {
        Label var1 = new Label(content);
        var1.setMaxWidth(1.7976931348623157E308D);
        var1.setMaxHeight(1.7976931348623157E308D);
        var1.getStyleClass().add("content");
        var1.setWrapText(true);
        var1.setPrefWidth(360.0D);
        return var1;
    }
}
