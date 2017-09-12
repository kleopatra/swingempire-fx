/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
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
        // do nothing if we are not editing .. missing in super but done in cell.cancel
      if (!isEditing()) return; 
      super.cancelEdit();
      cancelEditTextField();
//      CellUtils.cancelEdit(this, getConverter(), null);
    }

    /** {@inheritDoc} */
    @Override 
    public void updateItem(T item, boolean empty) {
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


}
