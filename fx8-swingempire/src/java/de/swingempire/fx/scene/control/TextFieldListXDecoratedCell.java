/*
 * Created on 06.04.2016
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.fx.scene.control.cell.TextFieldCellDecorator;

/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * A class containing a {@link ListCell} implementation that draws a
 * {@link TextField} node inside the cell.
 *
 * <p>By default, the TextFieldListCell is rendered as a {@link Label} when not
 * being edited, and as a TextField when in editing mode. The TextField will, by
 * default, stretch to fill the entire list cell.
 *
 * @param <S> The type of the elements contained within the ListView.
 * @param <C> the type of the cell (may be same or different from T)
 * @since JavaFX 2.2
 */
public class TextFieldListXDecoratedCell<S, C> extends ListXDecoratedCell<S, C> 
    implements TextFieldCellDecorator<ListView<S>, S>{

    private TextField textField;


    /** {@inheritDoc} */
    @Override 
    public void startEdit() {
        if (!canStartEdit()) return;
//        if (! isEditable() || ! getListView().isEditable()) {
//            return;
//        }
        super.startEdit();

        if (isEditing()) {
            startEditTextField();
//            if (textField == null) {
//                textField = createTextField();
//            }
//
//            textField.setText(getCellText(getItem()));
//            setGraphic(textField);
//            setText(null);
//            textField.selectAll();
//
//            // requesting focus so that key input can immediately go into the
//            // TextField (see RT-28132)
//            textField.requestFocus();
////            CellUtils.startEdit(this, getConverter(), null, null, textField);
        }
    }

    /**
     * Implemented to call super - nothing special to do?
     * 
     * Core doesn't implement it ... 
     */
    @Override
    public void commitEdit(S newValue) {
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        if (ignoreCancel()) return;
        super.cancelEdit();
        cancelEditTextField();
//        CellUtils.cancelEdit(this, getConverter(), null);
    }

    /** {@inheritDoc} */
    @Override protected void updateItem(S item, boolean empty) {
        super.updateItem(item, empty);
        updateItemTextField(item, empty);
//        if (empty) {
//            setGraphic(null);
//            setText(null);
//        } else {
//            if (isEditing()) {
//                if (textField != null) {
//                    textField.setText(getCellText(item));
//                }
//                setText(null);
//                setGraphic(textField);
//            } else {
//                setText(getCellText(item));
//                setGraphic(null);
//            }
//        }
////        CellUtils.updateItem(this, getConverter(), null, null, textField);
    }

    @Override
    public TextField getTextField() {
        if (textField == null) {
            textField = createTextField();
        }
        return textField;
    }
    /**
     * @return
     */
    @Override
    public TextField createTextField() {
        TextField field = new TextField();
        field.setOnAction(e -> {
            if (getCellValueConverter() == null) {
                throw new IllegalStateException(
                        "Attempting to convert text input into Object, but provided "
                                + "StringConverter is null. Be sure to set a StringConverter "
                                + "in your cell factory.");
    
            }
            C cellValue = getCellValueConverter().fromString(field.getText());
            commitCellValue(cellValue);
            e.consume();
        });
        field.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
        return field;
    }

    /***************************************************************************
     *                                                                         *
     * Static cell factories                                                   *
     *                                                                         *
     **************************************************************************/
    
    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    
    private ObjectProperty<StringConverter<C>> cellValueConverter =
            new SimpleObjectProperty<StringConverter<C>>(this, "cellValueConverter");
    
    public ObjectProperty<StringConverter<C>> cellValueConverterProperty() {
        return cellValueConverter;
    }
    
    public void setCellValueConverter(StringConverter<C> cellValueConverter) {
        cellValueConverterProperty().set(cellValueConverter);
    }
    
    public StringConverter<C> getCellValueConverter() {
        return cellValueConverterProperty().get();
    }
    /**
     *  --- converter this is the converter from item string
     *  not usable for cellValue to string!!
     */
    private ObjectProperty<StringConverter<S>> converter =
            new SimpleObjectProperty<StringConverter<S>>(this, "converter");

    /**
     * The {@link StringConverter} property.
     */
    @Override
    public final ObjectProperty<StringConverter<S>> converterProperty() {
        return converter;
    }



    /**
     * Provides a {@link TextField} that allows editing of the cell content when
     * the cell is double-clicked, or when {@link ListView#edit(int)} is called.
     * This method will only work on {@link ListView} instances which are of
     * type String.
     *
     * @return A {@link Callback} that can be inserted into the
     *      {@link ListView#cellFactoryProperty() cell factory property} of a
     *      ListView, that enables textual editing of the content.
     */
    public static <S> Callback<ListXView<S, String>, ListXDecoratedCell<S, String>> forListView() {
        return forListView(new DefaultStringConverter());
    }

    /**
     * Provides a {@link TextField} that allows editing of the cell content when
     * the cell is double-clicked, or when {@link ListView#edit(int)} is called.
     * This method will work on any ListView instance, regardless of its generic
     * type. However, to enable this, a {@link StringConverter} must be provided
     * that will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * {@link ListView#onEditCommitProperty()} callback.
     *
     * @param converter A {@link StringConverter} that can convert the given String
     *      (from what the user typed in) into an instance of type T.
     * @return A {@link Callback} that can be inserted into the
     *      {@link ListView#cellFactoryProperty() cell factory property} of a
     *      ListView, that enables textual editing of the content.
     */
    public static <S, C> Callback<ListXView<S, C>, ListXDecoratedCell<S, C>> forListView(final StringConverter<C> converter) {
        return list -> new TextFieldListXDecoratedCell<S, C>(converter);
    }

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Creates a default TextFieldListCell with a null converter. Without a
     * {@link StringConverter} specified, this cell will not be able to accept
     * input from the TextField (as it will not know how to convert this back
     * to the domain object). It is therefore strongly encouraged to not use
     * this constructor unless you intend to set the converter separately.
     */
    public TextFieldListXDecoratedCell() {
        this(null);
    }

    /**
     * Creates a TextFieldListCell that provides a {@link TextField} when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any ListView instance, regardless of its generic type.
     * However, to enable this, a {@link StringConverter} must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * {@link ListView#onEditCommitProperty()} callback.
     *
     * @param converter A {@link StringConverter converter} that can convert
     *      the given String (from what the user typed in) into an instance of
     *      type T.
     */
    public TextFieldListXDecoratedCell(StringConverter<C> converter) {
        this.getStyleClass().add("text-field-list-cell");
        setCellValueConverter(converter);
    }
}
