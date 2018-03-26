/*
 * Created on 08.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.cell.CellDecorator;
import de.swingempire.fx.scene.control.cell.TextFieldCellDecorator;
import de.swingempire.fx.scene.control.edit.ListAutoCell.TextFieldListAutoCell;
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
public class ListAutoCell<T> extends ListCell<T> implements CellDecorator<T> {
    
    
    
    @Override
    protected void updateItem(T arg0, boolean arg1) {
        super.updateItem(arg0, arg1);
    }

    @Override
    public void cancelEdit() {
        LOG.info("editing/index " + isEditing() + getIndex() + getItem());
        super.cancelEdit();
    }

    @Override
    public void commitEdit(T arg0) {
        super.commitEdit(arg0);
    }

    @Override
    public void startEdit() {
        super.startEdit();
    }


    public static class TextFieldListAutoCell<T> extends ListAutoCell<T>  implements TextFieldCellDecorator<T> {
        
        
        public static Callback<ListView<String>, ListCell<String>> forListView() {
            return forListView(new DefaultStringConverter());
        }
        public static <T> Callback<ListView<T>, ListCell<T>> forListView(final StringConverter<T> converter) {
            return list -> new TextFieldListAutoCell<T>(converter);
        }

        /***************************************************************************
         *                                                                         *
         * Fields                                                                  *
         *                                                                         *
         **************************************************************************/
        private TextField textField;



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
        public TextFieldListAutoCell() {
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
        public TextFieldListAutoCell(StringConverter<T> converter) {
            this.getStyleClass().add("text-field-list-cell");
            setConverter(converter);
        }



        /***************************************************************************
         *                                                                         *
         * Properties                                                              *
         *                                                                         *
         **************************************************************************/

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

        

        /***************************************************************************
         *                                                                         *
         * Public API                                                              *
         *                                                                         *
         **************************************************************************/

        @Override
        public TextField getTextField() {
            if (textField == null) {
                textField = createTextField();
            }
            return textField;
        }
        
        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        @Override 
        public void cancelEdit() {
            if (isEditing()) {
                ListView list = getListView();
                if (list != null) {
                    Integer pendingEdit = (Integer) list.getProperties().get("PENDING_EDIT_KEY");
                    int myIndex = getIndex();
                    int listEditingIndex = list.getEditingIndex();
                    LOG.info("my/pending/listediting" + myIndex + " / " + pendingEdit + " / " + listEditingIndex);
                }
            }
            super.cancelEdit();
            cancelEditTextField();
//            CellUtils.cancelEdit(this, getConverter(), null);
        }

        /** {@inheritDoc} */
        @Override public void updateItem(T item, boolean empty) {
            cellUpdateItem(item, empty);
            updateItemTextField(item, empty);
//            super.updateItem(item, empty);
//            CellUtils.updateItem(this, getConverter(), null, null, textField);
        }

    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListAutoCell.class.getName());

}
