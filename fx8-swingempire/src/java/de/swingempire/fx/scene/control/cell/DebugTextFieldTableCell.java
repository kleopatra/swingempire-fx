/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTextFieldTableCell<S, T> extends DebugTableCell<S, T>
        implements TextFieldCellDecorator<T> {

    
    
    /** {@inheritDoc} */
    @Override 
    public void startEdit() {
        if (! isEditable()
                || ! getTableView().isEditable()
                || ! getTableColumn().isEditable()) {
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


    /** {@inheritDoc} */
    @Override 
    public void cancelEdit() {
        // do nothing if we are not editing or should ignore for other reasons ..
      if (ignoreCancel()) return; 
      super.cancelEdit();
      cancelEditTextField();
//      CellUtils.cancelEdit(this, getConverter(), null);
    }

    /** {@inheritDoc} */
    @Override 
    protected void updateItem(T item, boolean empty) {
        // same as cellUpdateItem because base tableCell has no own!
        super.updateItem(item, empty);
        updateItemTextField(item, empty);
    }

 //---------------- properties, constructors, factory methods   
    private TextField textField;

    @Override
    public TextField getTextField() {
        if (textField == null) {
            textField = createTextField();
//            // focused for !cellSelectionEnabled is always false
//            focusedProperty().addListener(obs -> {
//                LOG.info("cell focused: " + isFocused() + " on: " + getIndex() + " / " + getItem() );
//            });
//            textField.focusedProperty().addListener(obs -> {
//                LOG.info("field focused: " + textField.isFocused() + " on: " + getIndex() + " / " + getItem() );
//            });
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


    public DebugTextFieldTableCell() {
        this(null);
    }


    /**
     * @param converter
     */
    public DebugTextFieldTableCell(StringConverter<T> converter) {
        this.getStyleClass().add("text-field-table-cell");
        setConverter(converter);
    }
    
    public static <S> Callback<TableColumn<S,String>, TableCell<S,String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final StringConverter<T> converter) {
        return list -> new DebugTextFieldTableCell<S,T>(converter);
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugTextFieldTableCell.class.getName());
}
