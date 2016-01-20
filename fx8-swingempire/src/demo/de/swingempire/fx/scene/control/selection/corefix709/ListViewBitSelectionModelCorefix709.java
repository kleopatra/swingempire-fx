/*
 * Created on 19.01.2016
 *
 */
package de.swingempire.fx.scene.control.selection.corefix709;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ListView;
import javafx.util.Pair;

import com.sun.javafx.scene.control.behavior.ListCellBehavior;

// package for testing
public class ListViewBitSelectionModelCorefix709<T> extends MultipleSelectionModelBase<T> {

    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    public ListViewBitSelectionModelCorefix709(final ListView<T> listView) {
        if (listView == null) {
            throw new IllegalArgumentException("ListView can not be null");
        }

        this.listView = listView;


        /*
         * The following two listeners are used in conjunction with
         * SelectionModel.select(T obj) to allow for a developer to select
         * an item that is not actually in the data model. When this occurs,
         * we actively try to find an index that matches this object, going
         * so far as to actually watch for all changes to the items list,
         * rechecking each time.
         */
        itemsObserver = new InvalidationListener() {
            private WeakReference<ObservableList<T>> weakItemsRef = new WeakReference<>(listView.getItems());

            @Override public void invalidated(Observable observable) {
                ObservableList<T> oldItems = weakItemsRef.get();
                weakItemsRef = new WeakReference<>(listView.getItems());
                updateItemsObserver(oldItems, listView.getItems());
            }
        };

        this.listView.itemsProperty().addListener(new WeakInvalidationListener(itemsObserver));
        if (listView.getItems() != null) {
            this.listView.getItems().addListener(weakItemsContentObserver);
        }
        
        updateItemCount();

        updateDefaultSelection();
    }
    
    // watching for changes to the items list content
    private final ListChangeListener<T> itemsContentObserver = new ListChangeListener<T>() {
        @Override public void onChanged(Change<? extends T> c) {
            updateItemCount();
            
            while (c.next()) {
                final T selectedItem = getSelectedItem();
                final int selectedIndex = getSelectedIndex();
                
                if (listView.getItems() == null || listView.getItems().isEmpty()) {
                    selectedItemChange = c;
                    clearSelection();
                    selectedItemChange = null;
                } else if (selectedIndex == -1 && selectedItem != null) {
                    int newIndex = listView.getItems().indexOf(selectedItem);
                    if (newIndex != -1) {
                        setSelectedIndex(newIndex);
                    }
                } else if (c.wasRemoved() && 
                        c.getRemovedSize() == 1 && 
                        ! c.wasAdded() && 
                        selectedItem != null && 
                        selectedItem.equals(c.getRemoved().get(0))) {
                    // Bug fix for RT-28637
                    if (getSelectedIndex() < getItemCount()) {
                        final int previousRow = selectedIndex == 0 ? 0 : selectedIndex - 1;
                        T newSelectedItem = getModelItem(previousRow);
                        if (! selectedItem.equals(newSelectedItem)) {
                            startAtomic();
                            clearSelection(selectedIndex);
                            stopAtomic();
                            select(newSelectedItem);
                        }
                    }
                }
            }
            
            updateSelection(c);
        }
    };
    
    // watching for changes to the items list
    private final InvalidationListener itemsObserver;
    
    private WeakListChangeListener<T> weakItemsContentObserver =
            new WeakListChangeListener<>(itemsContentObserver);
    



    /***********************************************************************
     *                                                                     *
     * Internal properties                                                 *
     *                                                                     *
     **********************************************************************/

    private final ListView<T> listView;
    
    private int itemCount = 0;
    
    private int previousModelSize = 0;

    // Listen to changes in the listview items list, such that when it 
    // changes we can update the selected indices bitset to refer to the 
    // new indices.
    // At present this is basically a left/right shift operation, which
    // seems to work ok.
    private void updateSelection(Change<? extends T> c) {
//        // debugging output
//        System.out.println(listView.getId());
//        if (c.wasAdded()) {
//            System.out.println("\tAdded size: " + c.getAddedSize() + ", Added sublist: " + c.getAddedSubList());
//        }
//        if (c.wasRemoved()) {
//            System.out.println("\tRemoved size: " + c.getRemovedSize() + ", Removed sublist: " + c.getRemoved());
//        }
//        if (c.wasReplaced()) {
//            System.out.println("\tWas replaced");
//        }
//        if (c.wasPermutated()) {
//            System.out.println("\tWas permutated");
//        }
        c.reset();

        List<Pair<Integer, Integer>> shifts = new ArrayList<>();
        while (c.next()) {
            if (c.wasReplaced()) {
                if (c.getList().isEmpty()) {
                    // the entire items list was emptied - clear selection
                    clearSelection();
                } else {
                    int index = getSelectedIndex();
                    
                    if (previousModelSize == c.getRemovedSize()) {
                        // all items were removed from the model
                        clearSelection();
                    } else if (index < getItemCount() && index >= 0) {
                        // Fix for RT-18969: the list had setAll called on it
                        // Use of makeAtomic is a fix for RT-20945
                        startAtomic();
                        clearSelection(index);
                        stopAtomic();
                        select(index);
                    } else {
                        // Fix for RT-22079
                        clearSelection();
                    }
                }
            } else if (c.wasAdded() || c.wasRemoved()) {
                int shift = c.wasAdded() ? c.getAddedSize() : -c.getRemovedSize();
                shifts.add(new Pair<>(c.getFrom(), shift));
            } else if (c.wasPermutated()) {

                // General approach:
                //   -- detected a sort has happened
                //   -- Create a permutation lookup map (1)
                //   -- dump all the selected indices into a list (2)
                //   -- clear the selected items / indexes (3)
                //   -- create a list containing the new indices (4)
                //   -- for each previously-selected index (5)
                //     -- if index is in the permutation lookup map
                //       -- add the new index to the new indices list
                //   -- Perform batch selection (6)

                // (1)
                int length = c.getTo() - c.getFrom();
                HashMap<Integer, Integer> pMap = new HashMap<Integer, Integer>(length);
                for (int i = c.getFrom(); i < c.getTo(); i++) {
                    pMap.put(i, c.getPermutation(i));
                }

                // (2)
                List<Integer> selectedIndices = new ArrayList<Integer>(getSelectedIndices());


                // (3)
                clearSelection();

                // (4)
                List<Integer> newIndices = new ArrayList<Integer>(getSelectedIndices().size());

                // (5)
                for (int i = 0; i < selectedIndices.size(); i++) {
                    int oldIndex = selectedIndices.get(i);

                    if (pMap.containsKey(oldIndex)) {
                        Integer newIndex = pMap.get(oldIndex);
                        newIndices.add(newIndex);
                    }
                }

                // (6)
                if (!newIndices.isEmpty()) {
                    if (newIndices.size() == 1) {
                        select(newIndices.get(0));
                    } else {
                        int[] ints = new int[newIndices.size() - 1];
                        for (int i = 0; i < newIndices.size() - 1; i++) {
                            ints[i] = newIndices.get(i + 1);
                        }
                        selectIndices(newIndices.get(0), ints);
                    }
                }
            }
        }

        if (!shifts.isEmpty()) {
            shiftSelection(shifts, null);
        }
        
        previousModelSize = getItemCount();
    }



    /***********************************************************************
     *                                                                     *
     * Public selection API                                                *
     *                                                                     *
     **********************************************************************/

    /** {@inheritDoc} */
    @Override public void selectAll() {
        // when a selectAll happens, the anchor should not change, so we store it
        // before, and restore it afterwards
        final int anchor = ListCellBehavior.getAnchor(listView, -1);
        super.selectAll();
        ListCellBehavior.setAnchor(listView, anchor, false);
    }

    /** {@inheritDoc} */
    @Override public void clearAndSelect(int row) {
        ListCellBehavior.setAnchor(listView, row, false);
        super.clearAndSelect(row);
    }

    /** {@inheritDoc} */
    @Override protected void focus(int row) {
        if (listView.getFocusModel() == null) return;
        listView.getFocusModel().focus(row);

        listView.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM);
    }

    /** {@inheritDoc} */
    @Override protected int getFocusedIndex() {
        if (listView.getFocusModel() == null) return -1;
        return listView.getFocusModel().getFocusedIndex();
    }

    @Override protected int getItemCount() {
        return itemCount;
    }

    @Override protected T getModelItem(int index) {
        List<T> items = listView.getItems();
        if (items == null) return null;
        if (index < 0 || index >= itemCount) return null;

        return items.get(index);
    }



    /***********************************************************************
     *                                                                     *
     * Private implementation                                              *
     *                                                                     *
     **********************************************************************/

    private void updateItemCount() {
        if (listView == null) {
            itemCount = -1;
        } else {
            List<T> items = listView.getItems();
            itemCount = items == null ? -1 : items.size();
        }
    }

    private void updateItemsObserver(ObservableList<T> oldList, ObservableList<T> newList) {
        // update listeners
        if (oldList != null) {
            oldList.removeListener(weakItemsContentObserver);
        }
        if (newList != null) {
            newList.addListener(weakItemsContentObserver);
        }

        updateItemCount();
        updateDefaultSelection();
    }

    private void updateDefaultSelection() {
        // when the items list totally changes, we should clear out
        // the selection and focus
        int newSelectionIndex = -1;
        int newFocusIndex = -1;
        if (listView.getItems() != null) {
            T selectedItem = getSelectedItem();
            if (selectedItem != null) {
                newSelectionIndex = listView.getItems().indexOf(selectedItem);
                newFocusIndex = newSelectionIndex;
            }

            // PENDING JW: commented to make compileable
            // we put focus onto the first item, if there is at least
            // one item in the list
//            if (listView.selectFirstRowByDefault && newFocusIndex == -1) {
//                newFocusIndex = listView.getItems().size() > 0 ? 0 : -1;
//            }
        }

        clearSelection();
        select(newSelectionIndex);
        focus(newFocusIndex);
    }
}


