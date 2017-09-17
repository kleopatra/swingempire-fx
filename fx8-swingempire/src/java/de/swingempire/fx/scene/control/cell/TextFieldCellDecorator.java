/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import jdk.internal.jline.internal.Log;

/**
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface TextFieldCellDecorator<T> extends CellDecorator<T> {

//---------------- handle TextField    
    
    TextField getTextField();
    
    /**
     * Default implementation at the same level of concreteness as the default XXCell
     * implementation (NOT the base XXCell! which have none). Updates this
     * cell's text/graphic properties depending on empty on isEditing.
     * 
     * Copied from CellUtils.updateItem
     * 
     * @param item
     * @param empty
     */
    default void updateItemTextField(T item, boolean empty) {
        boolean isExpectedEmpty = empty ? (item == null) : false;
        int last = -1;
        if (!empty) {
//            String text = itemToString(item);
//            String lastCh = text.substring(text.length() - 1);
//            last = Integer.valueOf(lastCh);
        }
        if (last >= 0 && last != getIndex()) {
            LOG.info("UPDATE_TEXTFIELD: unexpected content - item/param/index/counter " + getItem() + " / " + item 
                + " / " + getIndex() + " / " + getCounter());
//            new RuntimeException("who is calling? \n").printStackTrace();
        }
//        boolean isExpectedItem = item != null ? 
        if (empty) { //isEmpty()) {
            setText(null);
            setGraphic(null);
            getTextField().setText(null);
        } else {
            if (isEditing()) {
                getTextField().setText(itemToString(item));
                setText(null);
                setGraphic(getTextField());
//                if (textField != null) {
//                    textField.setText(getItemText(cell, converter));
//                }
//                cell.setText(null);
//                
//                if (graphic != null) {
//                    hbox.getChildren().setAll(graphic, textField);
//                    cell.setGraphic(hbox);
//                } else {
//                    cell.setGraphic(textField);
//                }
            } else {
                setText(itemToString(item));
                getTextField().setText(itemToString(item));
                setGraphic(null);
//                cell.setText(getItemText(cell, converter));
//                cell.setGraphic(graphic);
            }
        }

    }
    
    /**
     * Default implementation with updates the textField as appropriatee
     * and sets it as graphic of this cell, text of this cell is null.
     * 
     * Copied from CellUtils.editStart.
     * PENDING: HBox for treeItem graphics not supported.
     */
    default void startEditTextField() {
        if (!isEditing()) return;
        // copied and adjusted from CellUtils
        getTextField().setText(itemToString(getItem()));
        
        setText(null);
        setGraphic(getTextField());
        
//        if (graphic != null) {
//            hbox.getChildren().setAll(graphic, textField);
//            cell.setGraphic(hbox);
//        } else {
//            cell.setGraphic(textField);
//        }
//        

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        getTextField().requestFocus();
        getTextField().selectAll();

    }
    
    /**
     * Implemented to do nothing .. really needed?
     * @param item
     */
    default void commitEditTextField(T item) {
        
    }
    
    /**
     * Default implementation to update text/graphic property of this cell.
     * Copied from CellUtils.cancelEdit.
     */
    default void cancelEditTextField() {
        setText(itemToString(getItem()));
        setGraphic(null);
    }
    
    /**
     * Copied from core CellUtils.createTextField
     * @return
     */
    default TextField createTextField() {
        final TextField textField = new TextField(itemToString(getItem()));

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction(event -> {
            if (getConverter() == null) {
                throw new IllegalStateException(
                        "Attempting to convert text input into Object, but provided "
                                + "StringConverter is null. Be sure to set a StringConverter "
                                + "in your cell factory.");
            }
            commitEdit(getConverter().fromString(textField.getText()));
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
        return textField;
    }
    
    @Override
    default String itemToString(T item) {
        if (getConverter() != null) {
            return getConverter().toString(item);
        }
        return item != null ? item.toString() : "null";
    }
//------------------------ converter    
    /**
     * The {@link StringConverter} property.
     * @return the {@link StringConverter} property
     */
    ObjectProperty<StringConverter<T>> converterProperty();
        

    /**
     * Sets the {@link StringConverter} to be used in this cell.
     * @param value the {@link StringConverter} to be used in this cell
     */
    default void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the {@link StringConverter} used in this cell.
     * @return the {@link StringConverter} used in this cell
     */
    default StringConverter<T> getConverter() {
        return converterProperty().get();
    }


    @SuppressWarnings("unused")
    static final Logger LOG = Logger
            .getLogger(TextFieldCellDecorator.class.getName());
}
