/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import de.swingempire.fx.property.BugPropertyAdapters;

/**
 * C&P core - extracted to keep ListView from meddling with internals
 * (RT-39042)
 * 
 * PENDING JW:
 * - tried to remove listener to items, this should be the slave of the selectionModel
 *   (listening to item changes might compete with selectionModel control)
 * - didn't work out, focus not properly updated without ...
 * - nested while(c.next) in contentListener, intentional?
 * 
 * 
 * Changes:
 * - adapted ListView's itemProperty to listProperty (with additional invalidationListener)
 * - registered contentListener with itemsProperty
 * - removed itemsObserver (handled by listProperty)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
// package for testing
public class ListViewAFocusModel<T> extends FocusModel<T> {

    private final ListView<T> listView;
    private int itemCount = 0;

    ListProperty<T> itemsList;
    
    public ListViewAFocusModel(final ListView<T> listView) {
        if (listView == null) {
            throw new IllegalArgumentException("ListView can not be null");
        }

        this.listView = listView;
        itemsList = BugPropertyAdapters.listProperty(listView.itemsProperty());
        itemsList.addListener(weakItemsContentListener);
        updateItemCount();

        if (itemCount > 0) {
            focus(0);
        }
    }

    
    // Listen to changes in the listview items list, such that when it
    // changes we can update the focused index to refer to the new indices.
    private final ListChangeListener<T> itemsContentListener = c -> {
        updateItemCount();

        while (c.next()) {
            // looking at the first change
            int from = c.getFrom();
            if (getFocusedIndex() == -1 || from > getFocusedIndex()) {
                return;
            }

            c.reset();
            boolean added = false;
            boolean removed = false;
            int addedSize = 0;
            int removedSize = 0;
            while (c.next()) {
                added |= c.wasAdded();
                removed |= c.wasRemoved();
                addedSize += c.getAddedSize();
                removedSize += c.getRemovedSize();
            }

            if (added && !removed) {
                focus(getFocusedIndex() + addedSize);
            } else if (!added && removed) {
                focus(getFocusedIndex() - removedSize);
            }
        }
    };
    
    private WeakListChangeListener<T> weakItemsContentListener 
            = new WeakListChangeListener<T>(itemsContentListener);
    
    @Override protected int getItemCount() {
        return itemCount;
    }

    @Override protected T getModelItem(int index) {
        if (isEmpty()) return null;
        if (index < 0 || index >= itemCount) return null;

        return listView.getItems().get(index);
    }

    private boolean isEmpty() {
        return itemCount == -1;
    }
    
    private void updateItemCount() {
        if (listView == null) {
            itemCount = -1;
        } else {
            List<T> items = listView.getItems();
            itemCount = items == null ? -1 : items.size();
        }
    } 
}
