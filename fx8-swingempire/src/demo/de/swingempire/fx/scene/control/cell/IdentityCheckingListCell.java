/*
 * Created on 22.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.scene.control.ListCell;

/**
 * Overridden updateIndex to check if item.equals but not same and
 * update item if needed.
 * 
 * Core handling:
 * indexedCell.updateIndex(newIndex); // called always
 * indexedCell.indexChanged(oldIndex, newIndex); // package private, implemented by
 *    concrete cells, called always
 * listCell.updateItem(newIndex); // private, called from indexChanged on listChangeListener
 *   // decides about calling or not updateItem(newValue, empty)
 * does not call if newValue.equals(oldValue)  
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IdentityCheckingListCell<T> extends ListCell<T> {

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
        LOG.info("item unchanged? " + (oldItem == getItem()));
        // weed out the obvious
        if (oldIndex != getIndex()) return;
        if (oldItem == null || getItem() == null) return;
        if (wasEmpty != isEmpty()) return;
        // here both old and new != null, check whether the item had changed
        if (oldItem != getItem()) return;
        // unchanged, check if it should have been changed
        T listItem = getListView().getItems().get(getIndex());
        // update if not same
        if (oldItem != listItem) {
            updateItem(listItem, isEmpty());
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IdentityCheckingListCell.class.getName());
}
