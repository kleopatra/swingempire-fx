/*
 * Created on 04.06.2014
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.fx.scene.control.cell.DebugListCell;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;

/**
 * Mimicking the valueFactory mechanism as used in TableCell.<p>
 * 
 * Note: this is useful _only_ for showing, _not_ for editing! The ListCell must be
 * typed to the item type (that's the typing expected by the cellFactory).
 * As a consequence it will talk in the language of that type: start/commitEdit will
 * carry it. There is no clean way to intercept the edit event, nor the editHandler - all
 * done and hard-wired in super!
 * 
 * @param <T> the type of the data item
 * @param <C> the type of the cell (may be same or different from T)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListXDecoratedCell<T, C> extends DebugListCell<T>   {

    private InvalidationListener cellValueListener;
    private WeakInvalidationListener weakCellValueListener;
    
    /**
     * The observableValue that's shown by this cell.
     */
    private ObservableValue<C> currentObservable;
    /** flag to force an update of the item. Nasty as it is, it's used similarly as
     *  in TableCell. The difference is that we reset in isItemChanged (vs. in
     *  layoutChildren) because updateItem(int) is private. Alternatively, could
     *  reflective access super's method and call updateItem(-1) in layoutChildren
     *  just as TableCell. 
     */
    private boolean observableValueDirty;
    
    public ListXDecoratedCell() {
        initProperties();
    }

    private void initProperties() {
        cellValueListener = observable -> {
            observableValueDirty = true;
            requestLayout();
            };
        weakCellValueListener = new WeakInvalidationListener(cellValueListener);
    }

    /**
     * Overridden to return true if the observable is dirty, otherwise
     * returns super.<p>
     * 
     * Note: the dirty flag should better be handled in layoutChildren to
     * ensure the item is updated before painting. Can't, though, because
     * updateItem(int) is private in super.
     * 
     */
    @Override
    protected boolean isItemChanged(T oldItem, T newItem) {
        if (observableValueDirty) {
            observableValueDirty = false;
            return true;
        }
        return super.isItemChanged(oldItem, newItem);
    }

    /**
     * Overridden to bind to an observable value related to the
     * given item, if any. The observable to bind to is controlled
     * by the listView's CellValueFactory. 
     * <p>
     * Note: in contrast to super this implementation updates its
     * visual state as well!
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        // same as cellUpdateItem because DebugListCell does not have its own
        // implementation
        super.updateItem(item, empty);
        
        updateObservableValue(item);
        updateItemNode(item, empty);
//        if (empty) {
//            setText(null);
//            setGraphic(null);
//        } else {
//            /**
//             * This label is used if the item associated with this cell is to be
//             * represented as a String. While we will lazily instantiate it we
//             * never clear it, being more afraid of object churn than a minor
//             * "leak" (which will not become a "major" leak).
//             */
//            String text = getCellText(item);
//            setText(text);
//            setGraphic(null);
//        }
    }

    
    /**
     * PENDING JW: item not used? Should fall-back to item.toString
     * if we have no observableValue?
     * 
     * @param item
     * @return
     */
    protected String getCellText(T item) {
        C value = getCellValue();
        return value instanceof String ? (String) value : String.valueOf(value);
    }

    @Override
    public String itemToString(T item) {
        return getCellText(item);
    }

    protected C getCellValue() {
        return currentObservable != null ? currentObservable.getValue() : null;
    }
    
    
    @Override
    public void commitEdit(T newValue) {
        throw new UnsupportedOperationException("trying to commit on item " + newValue);
    }

    protected void commitCellValue(C cellValue) {
        if (! isEditing()) return;
        cellCommitEdit(getItem());
        updateItem(getItem(), false);
        ListView<T> list = getListView();
        if (list != null) {
            int editingIndex = list.getEditingIndex();
            // this should be the same as cell index, if not, something is wrong!
            if (!(list.getEditingIndex() == getIndex())) 
                throw new IllegalStateException("on commitEdit, list editing index must be same as my own: "
                        + getIndex() + " but was: " + editingIndex);

            list.edit(-1);
            list.fireEvent(new ListXView.EditXEvent<>(getListView(), 
            ListView.<T>editCommitEvent(),
            null,
            getIndex(),
            cellValue));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateObservableValue(T item) {
        if (currentObservable != null) {
            currentObservable.removeListener(weakCellValueListener);
        }
        currentObservable = ((ListXView<T, C>) getListView()).getCellObservableValue(item);
        if (currentObservable != null) {
            currentObservable.addListener(weakCellValueListener);
        }
        
    }

}
