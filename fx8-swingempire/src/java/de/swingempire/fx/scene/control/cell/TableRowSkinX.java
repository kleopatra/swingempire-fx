/*
 * Created on 07.11.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.ref.WeakReference;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableRow;

import com.sun.javafx.scene.control.skin.TableRowSkin;

/**
 * Skin that updates child cells in an InvalidationListener if
 * super's changeListener can't (that is if oldItem.equals(newItem)).
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowSkinX<T> extends TableRowSkin<T> {

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
            oldItemRef = new WeakReference<>(newItem);
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