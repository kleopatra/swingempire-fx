/*
 * Created on 11.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTextFieldListCell<T> extends DebugListCell<T>
        implements TextFieldCellDecorator<T> {
    
    private TextField textField;

    @Override
    public void startEdit() {
        if (! isEditable() || ! getListView().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            startEditTextField();
        }
    }

    /**
     * Implemented to call super - nothing special to do?
     * 
     * Core doesn't implement it ... 
     */
    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
    }
    
    @Override
    public void cancelEdit() {
        if (ignoreCancel()) return;
//        if (!isEditing() || ignoreCancel) return;
        super.cancelEdit();
        cancelEditTextField();
//        CellUtils.cancelEdit(this, getConverter(), null);
    }
    
    /**
     * Implemented to call super (to update internal state), then 
     * updateItemTextField to update/configure visuals.
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        // same as cellUpdateItem because base listCell has no own!
        super.updateItem(item, empty);
        updateItemTextField(item, empty);
    }
    
    @Override
    public TextField getTextField() {
        if (textField == null) {
            textField = createTextField();
        }
        return textField;
    }

    // --- converter
    private ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    /**
     * The {@link StringConverter} property.
     * @return the {@link StringConverter} property
     */
    @Override
    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public DebugTextFieldListCell() {
        this(null);
    }
    /**
     * @param converter
     */
    public DebugTextFieldListCell(StringConverter<T> converter) {
        this.getStyleClass().add("text-field-list-cell");
        setConverter(converter);
    }


    public static Callback<ListView<String>, ListCell<String>> forListView() {
        return forListView(new DefaultStringConverter());
    }
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final StringConverter<T> converter) {
        return list -> new DebugTextFieldListCell<T>(converter);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugTextFieldListCell.class.getName());
}
