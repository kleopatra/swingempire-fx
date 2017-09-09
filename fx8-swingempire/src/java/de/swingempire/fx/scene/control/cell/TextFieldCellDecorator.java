/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

/**
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface TextFieldCellDecorator<T> extends CellDecorator<T> {

//---------------- handle TextField    
    
    TextField getTextField();
    
    default void updateItemTextField(T item, boolean empty) {
        if (empty) { //isEmpty()) {
            setText(null);
            setGraphic(null);
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
                setGraphic(null);
//                cell.setText(getItemText(cell, converter));
//                cell.setGraphic(graphic);
            }
        }

    }
    
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
        getTextField().selectAll();

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        getTextField().requestFocus();

    }
    
    default void commitEditTextField(T item) {
        
    }
    
    default void cancelEditTextField() {
        setText(itemToString(getItem()));
        setGraphic(null);
    }
    
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



}
