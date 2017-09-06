/*
 * Created on 06.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;

import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * debugging ...
 * @author Jeanette Winzenburg, Berlin
 */
public class DTextFieldListCell<T> extends TextFieldListCell<T> {

    
    
    @Override
    public void updateIndex(int index) {
        int old = getIndex();
        boolean wasEditing = isEditing();
        super.updateIndex(index);
        if (!isEmpty()) {
            LOG.info("old/new: " + old + " / " + index + " editing: " + wasEditing + " / "+ isEditing());
            
        }
    }
    
    
    
//    ReadOnlyBooleanProperty focusAlias;
//    // cant access listeners, concrete subs are private
//    ExpressionHelper<Boolean> focusHelper;
    
    /** 
     * Trying to not accept cancel by super's listener
     * not working, getting into bad state ...
     */
    @Override
    public void cancelEdit() {
        Exception ex = new RuntimeException("dummy");
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().contains("Cell$1")) {
                LOG.info("" + stackTrace[i] + isEditing());
                return;
            }
        }
        super.cancelEdit();
    }



    public DTextFieldListCell(StringConverter<T> converter) {
        super(converter);
//        focusAlias = focusedProperty();
//        focusHelper = (ExpressionHelper<Boolean>) FXUtils.invokeGetFieldValue(ReadOnlyBooleanPropertyBase.class, focusAlias, "helper");
    }
    public DTextFieldListCell() {
        this(null);
    }

    public static Callback<ListView<String>, ListCell<String>> forListView() {
        return forListView(new DefaultStringConverter());
    }
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final StringConverter<T> converter) {
        return list -> new DTextFieldListCell<T>(converter);
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DTextFieldListCell.class.getName());
}
