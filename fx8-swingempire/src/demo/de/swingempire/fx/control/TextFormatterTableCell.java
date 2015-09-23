/*
 * Created on 22.09.2015
 *
 */
package de.swingempire.fx.control;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.control.Cell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

/**
 * Experimenting with TextFormatter
 * 
 * PENDING
 * - doesn't help with commit on focus lost (same problem as with plain listener to focusedProperty,
 *   namely: the cell is no longer editing because it had been cancelled earlier in the process)
 * - listening to the formatters value property isn't good enough if the edited value is unchanged.   
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFormatterTableCell<S, T> extends TableCell<S, T> {

    private TextField textField;
    private boolean editStarted;
    
    public TextFormatterTableCell(StringConverter<T> converter) {
        this.getStyleClass().add("text-field-table-cell");
        setConverter(converter);
    }

    /** {@inheritDoc} */
    @Override public void startEdit() {
        if (! isEditable() 
                || ! getTableView().isEditable() 
                || ! getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = createTextField(getConverter());
            }
            doStartEdit();
//            CellUtils.startEdit(this, getConverter(), null, null, textField);
        }
    }

    /**
     * 
     */
    private void doStartEdit() {
//        if (editStarted) return;
        TextFormatter<T> formatter = (TextFormatter<T>) textField.getTextFormatter();
        editStarted = false;
        formatter.setValue(getItem());
        editStarted = true;
        setText(null);
        setGraphic(textField);
        textField.selectAll();
        textField.requestFocus();
    }

    /**
     * 
     * Listening to the formatter's valueProperty isn't good enough
     * - might not fire because old == newValue
     * 
     * 
     * @param converter
     * @return
     */
    private TextField createTextField(StringConverter<T> converter) {
        TextField field = new TextField();
        TextFormatter<T> formatter = new TextFormatter<>(converter);
        field.setTextFormatter(formatter);
        // PENDING wire to commits
        formatter.valueProperty().addListener((s, ov, nv) -> {
            LOG.info("value changed in formatter: " + ov + "/" + nv);
//                formatter.setValue(null);
                if (editStarted) {
                    // same problem as with plain focuslistener:
                    // isEditing == false and commitEdit does nothing
                    doCommitEdit(nv);
                    editStarted = false;
            }    
        });
        return field;
    }

    private void doCommitEdit(T newValue) {
        if (isEditing()) {
            // editing had not been canceled by table, super can handle it completely
            commitEdit(newValue);
            return;
        }
        
        final TableView<S> table = getTableView();
        if (table != null) {
            TablePosition position = new TablePosition(getTableView(), getIndex(), getTableColumn());
            // Inform the TableView of the edit being ready to be committed.
            CellEditEvent editEvent = new CellEditEvent(
                table,
                position,
//                table.getEditingCell(),
                TableColumn.editCommitEvent(),
                newValue
            );

            Event.fireEvent(getTableColumn(), editEvent);
        }

        // inform parent classes of the commit, so that they can switch us
        // out of the editing state.
        // This MUST come before the updateItem call below, otherwise it will
        // call cancelEdit(), resulting in both commit and cancel events being
        // fired (as identified in RT-29650)
        // nothing to do, the flag is already reset
//        super.commitEdit(newValue);

        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);

        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);

            // request focus back onto the table, only if the current focus
            // owner has the table as a parent (otherwise the user might have
            // clicked out of the table entirely and given focus to something else.
            // It would be rude of us to request it back again.
//            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);
        }
    }


    private void invokeSetEditingCell() {
        TableView table = getTableView();
        TablePosition position = new TablePosition(getTableView(), getIndex(), getTableColumn());
        Class caller = TableView.class;
        try {
            Method method = caller.getDeclaredMethod("setEditingCell", TablePosition.class );
            method.setAccessible(true);
            method.invoke(table, position);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        
    }
    private void invokeSetEditing(boolean editing) {
        Class caller = Cell.class;
        try {
            Method method = caller.getDeclaredMethod("setEditing", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(this, editing);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /** {@inheritDoc} */
    @Override public void cancelEdit() {
        super.cancelEdit();
        textField.cancelEdit();
        setGraphic(null);
//        CellUtils.cancelEdit(this, getConverter(), null);
    }
    
    /** {@inheritDoc} */
    @Override public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                setText(null);
                setGraphic(textField);
            } else {
                String text = getConverter().toString(getItem());
                setText(text);
                if (textField != null && getGraphic() == textField) {
                    textField.setText(null);
                }
                setGraphic(null);
            }
        }
//        CellUtils.updateItem(this, getConverter(), null, null, textField);
    }
    
    // --- converter
    private ObjectProperty<StringConverter<T>> converter = 
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    /**
     * The {@link StringConverter} property.
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() { 
        return converter; 
    }
    
    /** 
     * Sets the {@link StringConverter} to be used in this cell.
     */
    public final void setConverter(StringConverter<T> value) { 
        converterProperty().set(value); 
    }
    
    /**
     * Returns the {@link StringConverter} used in this cell.
     */
    public final StringConverter<T> getConverter() { 
        return converterProperty().get(); 
    }  
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFormatterTableCell.class.getName());
}
