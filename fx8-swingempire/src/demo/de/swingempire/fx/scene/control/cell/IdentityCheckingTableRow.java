/*
 * Created on 22.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;

import com.sun.javafx.scene.control.skin.TableRowSkin;


/**
 * Extended TableRow that updates its item if equal but not same.
 * Needs custom skin to update cells on invalidation of the 
 * item property.<p>
 * 
 * Looks ugly, as we have to let super doing its job and then
 * re-check the state. No way to hook anywhere else into super 
 * because all is private. <p>
 * 
 * Super might support a configuration option to check against
 * identity vs. against equality.<p>
 * 
 * Note that this is _not_ formally tested! Any execution paths calling
 * <code>updateItem(int)</code> other than through 
 * <code>indexedCell.updateIndex(int)</code> are not handled.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IdentityCheckingTableRow<T>  extends TableRow<T> {

    @Override
    public void updateIndex(int i) {
        int oldIndex = getIndex();
        T oldItem = getItem();
        boolean wasEmpty = isEmpty();
        super.updateIndex(i);
        updateItemIfNeeded(oldIndex, oldItem, wasEmpty);
        
    }

    /**
     * Here we try to guess whether super updateIndex didn't update the item if
     * it is equal to the old.
     * 
     * Strictly speaking, an implementation detail.
     * 
     * @param oldIndex cell's index before update
     * @param oldItem cell's item before update
     * @param wasEmpty cell's empty before update
     */
    protected void updateItemIfNeeded(int oldIndex, T oldItem, boolean wasEmpty) {
        // weed out the obvious
        if (oldIndex != getIndex()) return;
        if (oldItem == null || getItem() == null) return;
        if (wasEmpty != isEmpty()) return;
        // here both old and new != null, check whether the item had changed
        if (oldItem != getItem()) return;
        // unchanged, check if it should have been changed
        T listItem = getTableView().getItems().get(getIndex());
        // update if not same
        if (oldItem != listItem) {
            // doesn't help much because itemProperty doesn't fire
            updateItem(listItem, isEmpty());
        }
    }

    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableRowSkinX<>(this);
    }


    public static class TableRowSkinX<T> extends TableRowSkin<T> {

        private WeakReference<T> oldItemRef;
        private InvalidationListener itemInvalidationListener;
        private WeakInvalidationListener weakItemInvalidationListener;
        /**
         * @param tableRow
         */
        public TableRowSkinX(TableRow<T> tableRow) {
            super(tableRow);
            oldItemRef = new WeakReference<>(tableRow.getItem());
            itemInvalidationListener = o -> {
                T newItem = ((ObservableValue<T>) o).getValue();
                T oldItem = oldItemRef != null ? oldItemRef.get() : null;
                oldItemRef = new WeakReference(newItem);
                if (oldItem != null && newItem != null && oldItem.equals(newItem)) {
                    forceCellUpdate();
                }
            };
            weakItemInvalidationListener = new WeakInvalidationListener(itemInvalidationListener);
            tableRow.itemProperty().addListener(weakItemInvalidationListener);
        }
        
        /**
         * Try to force cell update for equal (but not same) items.
         * C&P'ed code from TableRowSkinBase.
         */
        private void forceCellUpdate() {
            updateCells = true;
            getSkinnable().requestLayout();

            // update the index of all children cells (RT-29849).
            // Note that we do this after the TableRow item has been updated,
            // rather than when the TableRow index has changed (as this will be
            // before the row has updated its item). This will result in the
            // issue highlighted in RT-33602, where the table cell had the correct
            // item whilst the row had the old item.
            final int newIndex = getSkinnable().getIndex();
            for (int i = 0, max = cells.size(); i < max; i++) {
                cells.get(i).updateIndex(newIndex);
            }
       }
        
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IdentityCheckingListCell.class.getName());

}
