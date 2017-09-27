/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTextFieldTreeCell<T> extends DebugTreeCell<T>
        implements TextFieldCellDecorator<TreeView<T>, T> {
    
    /** {@inheritDoc} */
    @Override 
    public void startEdit() {
        if (!canStartEdit()) return;
//        if (! isEditable() || ! getTreeView().isEditable()) {
//            return;
//        }
        super.startEdit();

        if (isEditing()) {
            startEditTextField();

//            StringConverter<T> converter = getConverter();
//            if (textField == null) {
//                textField = CellUtils.createTextField(this, converter);
//            }
//            if (hbox == null) {
//                hbox = new HBox(CellUtils.TREE_VIEW_HBOX_GRAPHIC_PADDING);
//            }
//
//            CellUtils.startEdit(this, converter, hbox, getTreeItemGraphic(), textField);
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
        // do nothing if we are not editing .. 
      if (ignoreCancel()) return; 
      super.cancelEdit();
      cancelEditTextField();
//      CellUtils.cancelEdit(this, getConverter(), null);

//        super.cancelEdit();
//        CellUtils.cancelEdit(this, getConverter(), getTreeItemGraphic());
    }

    /** {@inheritDoc} 
     * 
     * PENDING: core extends DefaultTreeCell which implements the graphic handling,
     * is updateItemTextField good enough (minus treeItem graphic handling .. which is 
     * missing here) 
     */
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


    public DebugTextFieldTreeCell() {
        this(null);
    }


    /**
     * @param converter
     */
    public DebugTextFieldTreeCell(StringConverter<T> converter) {
        this.getStyleClass().add("text-field-table-cell");
        setConverter(converter);
    }
    
    public static Callback<TreeView<String>, TreeCell<String>> forTreeView() {
        return forTreeView(new DefaultStringConverter());
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(
            final StringConverter<T> converter) {
        return list -> new DebugTextFieldTreeCell<T>(converter);
    }


}
