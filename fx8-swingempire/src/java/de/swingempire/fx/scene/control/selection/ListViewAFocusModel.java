/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;

/**
 * C&P core - extracted to keep ListView from meddling with internals
 * (RT-39042) plus some changes
 * 
 * Changes:
 * - removed itemsObserver (handled by controller, we are slave)
 * - removed listening to itemsContent (for now, regarding this as slave of SelectionModel)
 *   without doing so, we get problems with competing self-updates on changes to the items
 *   (see https://javafx-jira.kenai.com/browse/RT-38785)
 * - removed alias itemCount, query items directly instead: without listening to items,
 *   it's going out of sync  
 * - removed nested while when evaluating list change  
 * - implements FocusModelSlave: depending on some other class to take over update of
 *   focus, except when calling listChanged
 * - changed listChanged to not let focus get -1 on removes (added lower limit of 0)  
 * 
 * PENDING JW:
 * - focusModelSlave really needed? All it does here is to call its own public
 *   api, so might be directly handled in SelectionModel?  
 * 
 * PENDING JW:
 * - tried to remove listener to items, this should be the slave of the selectionModel
 *   (listening to item changes might compete with selectionModel control)
 *   didn't work out, focus not properly updated without ... tests are passing,
 *   but real navigation fails. Fixed now (was: alias itemCount out of sync) 
 *   but WHY didn't the tests fail?
 * - fixed: nested while(c.next) in contentListener, intentional?
 * - fixed: no listening, but slave need to re-introduce listening for cases 
 *   when the selectionModel doesn't take
 *   over, f.i. if !isSelected(focusedIndex)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
// package for testing
public class ListViewAFocusModel<T> extends FocusModel<T> implements FocusModelSlave<T> {

    private final ListView<T> listView;
//    private int itemCount = 0;

//    ListProperty<T> itemsList;
    
    public ListViewAFocusModel(final ListView<T> listView) {
        if (listView == null) {
            throw new IllegalArgumentException("ListView can not be null");
        }

        this.listView = listView;
//        itemsList = BugPropertyAdapters.listProperty(listView.itemsProperty());
//        itemsList.addListener(weakItemsContentListener);
//        updateItemCount();

        if (getItemCount() > 0) {
            focus(0);
        }
    }

    
    // Listen to changes in the listview items list, such that when it
    // changes we can update the focused index to refer to the new indices.
//    private final ListChangeListener<T> itemsContentListener = c -> {
////        updateItemCount();
//
////        listChanged(c);
//    };

    @Override
    public void listChanged(Change<? extends T> c) {
        c.reset();
        while (c.next()) {
            // looking at the first change
            int from = c.getFrom();
            if (getFocusedIndex() == -1 || from > getFocusedIndex()) {
                return;
            }
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
                // fix of navigation issue on remove focus at 0
                focus(Math.max(0, getFocusedIndex() - removedSize));
            }
    }
    
//    private WeakListChangeListener<T> weakItemsContentListener 
//            = new WeakListChangeListener<T>(itemsContentListener);
    
    @Override protected int getItemCount() {
        if (listView == null || listView.getItems() == null) return 0;
        return listView.getItems().size();
    }

    @Override protected T getModelItem(int index) {
        if (isEmpty()) return null;
        if (index < 0 || index >= getItemCount()) return null;

        return listView.getItems().get(index);
    }

    private boolean isEmpty() {
        return getItemCount() == -1;
    }
    
//    private void updateItemCount() {
//        if (listView == null) {
//            itemCount = -1;
//        } else {
//            List<T> items = listView.getItems();
//            itemCount = items == null ? -1 : items.size();
//        }
//    } 
}
