/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.swingempire.fx.scene.control.selection.MultipleSelectionModelBase.ShiftParams;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Plain copy of core, for playing with extensions.
 */ 
public class ListViewBitSetSelectionModel<T> extends MultipleSelectionModelBase<T> {


    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    public ListViewBitSetSelectionModel(final ListView<T> listView) {
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

        this.listView.itemsProperty().addListener(weakItemsObserver);
        if (listView.getItems() != null) {
            this.listView.getItems().addListener(weakItemsContentObserver);
//            updateItemsObserver(null, this.listView.getItems());
        }
        
        updateItemCount();
    }
    
    // watching for changes to the items list content
    private final ListChangeListener<T> itemsContentObserver = new ListChangeListener<T>() {
        @Override public void onChanged(Change<? extends T> c) {
            updateItemCount();
            
            while (c.next()) {
                final T selectedItem = getSelectedItem();
                final int selectedIndex = getSelectedIndex();
                
                if (listView.getItems() == null || listView.getItems().isEmpty()) {
                    clearSelection();
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
                        T newSelectedItem = getModelItem(selectedIndex);
                        if (! selectedItem.equals(newSelectedItem)) {
                            setSelectedItem(newSelectedItem);
                        }
                    }
                }
            }
            
            updateSelection(c);
        }
    };
    
    // watching for changes to the items list
    private final ChangeListener<ObservableList<T>> itemsObserver = (valueModel, oldList, newList) -> {
            updateItemsObserver(oldList, newList);
    };
    
    private WeakListChangeListener<T> weakItemsContentObserver =
            new WeakListChangeListener<T>(itemsContentObserver);
    
    private WeakChangeListener<ObservableList<T>> weakItemsObserver = 
            new WeakChangeListener<ObservableList<T>>(itemsObserver);
    
    private void updateItemsObserver(ObservableList<T> oldList, ObservableList<T> newList) {
        // update listeners
        if (oldList != null) {
            oldList.removeListener(weakItemsContentObserver);
        }
        if (newList != null) {
            newList.addListener(weakItemsContentObserver);
        }
        
        updateItemCount();

        // when the items list totally changes, we should clear out
        // the selection and focus
        int newValueIndex = -1;
        if (newList != null) {
            T selectedItem = getSelectedItem();
            if (selectedItem != null) {
                newValueIndex = newList.indexOf(selectedItem);
            }
        }

        setSelectedIndex(newValueIndex);
        focus(newValueIndex);
    }



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
                shiftSelection(c.getFrom(), shift, null);
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
        
        previousModelSize = getItemCount();
    }



    /***********************************************************************
     *                                                                     *
     * Public selection API                                                *
     *                                                                     *
     **********************************************************************/



    /** {@inheritDoc} */
    @Override protected void focus(int row) {
        if (listView.getFocusModel() == null) return;
        listView.getFocusModel().focus(row);

//        listView.accSendNotification(Attribute.SELECTED_ROWS);
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

    private void updateItemCount() {
        if (listView == null) {
            itemCount = -1;
        } else {
            List<T> items = listView.getItems();
            itemCount = items == null ? -1 : items.size();
        }
    } 
}



