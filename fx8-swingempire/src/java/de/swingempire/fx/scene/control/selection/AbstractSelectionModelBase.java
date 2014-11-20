/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;

/**
 * Replacement of MultipleSelectionModelBase. Uses TransformLists to handle selectedIndices/-items.
 * 
 * Note: for now, this assumes an observableList as backing items.<p>
 * 
 * PENDING JW: 
 * 
 * - DONE: enforce mode always
 * - DONE: fully support uncontained selectedItem
 * - DONE: implement navigational methods
 * - implement anchored and selectionModelJ 
 * - DONE: even with indicesList, model must listen to items changes to take care of uncontained 
 *   selection (f.i., there are other parts the indices can't handle for itself, like special
 *   casing selectedIndex)
 *   
 * Partly done (hacked for now):  
 * - think about eventBus: would it help to make the model first receive the notification itself,
 *   and then pass it on to the indices? That's more or less the current process in core 
 *   implementations  (which has all handling hard-coded)
 * - relying on the sequence of calling listeners (first-in, first-notified): clients
 *   must first create the indicesList, then the indexedItems, then register themselves.
 *   It's brittle, obviously.   
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class AbstractSelectionModelBase<T> extends MultipleSelectionModel<T> {

    protected IndicesList<T> indicesList;
    protected IndexMappedList<T> indexedItems;
    /**
     * The default listener installed on backing list to monitor list changes.
     */
    protected ListChangeListener<T> itemsContentListener = c -> itemsChanged(c);
    /**
     * The weak listener that subclasses should registered on the backing list.
     */
    protected WeakListChangeListener<T> weakItemsContentListener = 
            new WeakListChangeListener<T>(itemsContentListener);
    
    public AbstractSelectionModelBase() {
        // PENDING JW: better not, need to special case re-setting same 
        // selectedIndex anyway
//        selectedIndexProperty().addListener(valueModel -> {
//            // we used to lazily retrieve the selected item, but now we just
//            // do it when the selection changes. This is hardly likely to be
//            // expensive, and we still lazily handle the multiple selection
//            // cases over in MultipleSelectionModel.
//            setSelectedItem(getModelItem(getSelectedIndex()));
//        });
        
    }
    
    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return indicesList;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return indexedItems;
    }

    /**
     * PENDING JW: need to enforce single mode 
     * @param index
     * @param indices
     */
    @Override
    public void selectIndices(int index, int... indices) {
        int toBeSelectedIndex;
        int[] all;
        int indicesSize = indices != null ? indices.length : 0;
        if (indicesSize == 0) {
            // PENDING JW: check for allowed
            toBeSelectedIndex = index;
        } else {
            toBeSelectedIndex = indices[indices.length -1];
        }
        
        if (indicesSize == 0 || getSelectionMode() == SelectionMode.SINGLE) {
            all = new int[1];
            all[0] = toBeSelectedIndex;
        } else {
            all = new int[indicesSize + 1];
            all[0] = index;
            System.arraycopy(indices, 0, all, 1, indicesSize);
        }   
        if (getSelectionMode() == SelectionMode.SINGLE) {
            indicesList.setIndices(all);
        } else {
            indicesList.addIndices(all);
        }
        syncSingleSelectionState(toBeSelectedIndex);
//        setSelectedIndex(toBeSelectedIndex);
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to do nothing if start or end are off range.
     * 
     * partly reverted: end might be -1 for descending range - is
     * handled correctly by super.
     * 
     * @param start one boundary of the range, must be strictly valid
     * @param end the other boundary of the range, may be one off range because
     *    it is exclusiv
     */
    @Override
    public void selectRange(int start, int end) {
        if (!isSelectable(start) || end < -1) return;
        super.selectRange(start, end);
    }

    protected void syncSingleSelectionState(int selectedIndex) {
        setSelectedIndex(selectedIndex);
        if (selectedIndex > -1) {
            setSelectedItem(getModelItem(selectedIndex));
        } else {
            // PENDING JW: do better? can be uncontained item
            setSelectedItem(null);
        } 
        focus(selectedIndex);
    }
    @Override
    public void selectAll() {
        if (getSelectionMode() == SelectionMode.SINGLE) return;
        indicesList.setAllIndices();
        syncSingleSelectionState(getItemCount() - 1);
    }

    @Override
    public void clearAndSelect(int index) {
        if (!isSelectable(index)) return;
        indicesList.setIndices(index);
        syncSingleSelectionState(index);
    }

    /**
     * @param index
     * @return
     */
    protected boolean isSelectable(int index) {
        return index > -1 && index < getItemCount();
    }

    @Override
    public void select(int index) {
        if (!isSelectable(index)) return;
        if (getSelectionMode() == SelectionMode.SINGLE) {
            clearAndSelect(index);
        } else {
            indicesList.addIndices(index);
            syncSingleSelectionState(index);
        }
    }

    @Override
    public void select(T obj) {
        int index = indicesList.getSource().indexOf(obj);
        if (index > 0) {
            select(index);
        } else {
            setSelectedIndex(-1);
            setSelectedItem(obj);
        }
    }

    /**
     * PENDING JW: still missing sync of single selection state
     */
    @Override
    public void clearSelection(int index) {
//        if (!isSelectable(index));
        indicesList.clearIndices(index);
        
    }

    @Override
    public void clearSelection() {
        indicesList.clearAllIndices();
        syncSingleSelectionState(-1);
    }

    @Override
    public boolean isSelected(int index) {
        return indicesList.contains(index);
    }

    @Override
    public boolean isEmpty() {
        return indicesList.isEmpty();
    }

    @Override
    public void selectPrevious() {
        int previous = getCurrentIndex() - 1;
        if (previous < 0) previous = getItemCount() - 1;
        if (previous < 0) return;
        select(previous);
    }

    /**
     * @return
     */
    private int getCurrentIndex() {
        int current = getFocusedIndex();
        if (current < -1) current = getSelectedIndex();
        return current;
    }

    @Override
    public void selectNext() {
        int current = getCurrentIndex();
        if (current < 0 && getItemCount() == 0) return;
        int next = current + 1;
        select(next);

    }

    @Override
    public void selectFirst() {
        if (getItemCount() == 0) return;
        select(0);
    }
    @Override
    public void selectLast() {
        if (getItemCount() == 0) return;
        select(getItemCount() - 1);
    }
    /**
     * Returns the number of items in the data model that underpins the control.
     * An example would be that a ListView selection model would likely return
     * <code>listView.getItems().size()</code>. The valid range of selectable
     * indices is between 0 and whatever is returned by this method.
     */
    protected int getItemCount() {
        return indicesList.getSource().size();
    };
    
    /**
     * Returns the item at the given index. An example using ListView would be
     * <code>listView.getItems().get(index)</code>.
     * 
     * @param index The index of the item that is requested from the underlying
     *      data model.
     * @return Returns null if the index is out of bounds, or an element of type
     *      T that is related to the given index.
     */
    protected T getModelItem(int index) {
        if (index < 0 || index >= getItemCount()) return null;
        return indicesList.getSource().get(index);
    };
    
    
    /**
     * IndicesList/IndexMappedItems have taken care of updating selectedIndices/-items,
     * here we need to update selectedIndex/selectedItem.
     *  
     * @param c the change received from the backing items list
     */
    protected void itemsChanged(Change<? extends T> c) {
        if (indicesList.size() != indexedItems.size()) 
            throw new IllegalStateException("expected internal update to be done!");
        int oldSelectedIndex = getSelectedIndex();
        T oldSelectedItem = getSelectedItem();
        if (oldSelectedIndex < 0) {
            // no selected index, check selectedItem:
            // it had not been part of the items before the change (if it had
            // the selectedIndex wouldn't be < 0) but now might be
            if (oldSelectedItem != null && getItems().contains(oldSelectedItem)) {
                int selectedIndex = getItems().indexOf(oldSelectedItem);
                select(selectedIndex);
            }
            
            return;
        }
        // we had selectedItem/index pair that was part of the model
        // first handle empty list
        if (getItems().isEmpty()) {
            clearSelection();
            return;
        }
        
        // Note JW: isSelected tests whether the given index is part of selectedIndices
        // (even doc'ed as such - invalidly in SelectionModel)
        // vs. testing against -1 (which might or not be an option)

        // oldSelectedItem still in selectedItems
        if (indexedItems.contains(oldSelectedItem)) {
            int indexedIndex = indexedItems.indexOf(oldSelectedItem);
            int itemsIndex = indicesList.getSourceIndex(indexedIndex);
            syncSingleSelectionState(itemsIndex);
            return;
        } else { // selectedItem removed, clean up
            // no, need handle modifications at selectedIndex:
            // indices/items simply remove - we might need to do better
            // f.i. on set of single element or remove at selected 
            // latter is RT-30931
//            syncSingleSelectionState(-1);
        }
        
        // test if item at selectedIndex had been replaced
        if (indicesList.contains(oldSelectedIndex)) {
            int newSelectedIndex = -1;
            while (c.next()) {
                // PENDING JW: this is not good enough for multiple subchanges
                if (c.wasReplaced() && c.getRemoved().contains(oldSelectedItem)) {
                   newSelectedIndex = c.getFrom() + c.getRemoved().indexOf(oldSelectedItem);
                   break;
                }
            }
            if (newSelectedIndex > -1) {
                select(newSelectedIndex);
                return;
            }
        }
        // index still valid
        if (oldSelectedIndex < getItems().size() 
            && !indicesList.contains(oldSelectedIndex)) {
            T newSelectedItem = getItems().get(oldSelectedIndex);
            String msg = "old/new/setSelectedItem: " + getSelectedItem() + "/" + newSelectedItem;
            select(oldSelectedIndex);
//            LOG.info(msg + " / " + getSelectedItem());
            
        }
    }

    protected ObservableList<? extends T> getItems() {
        return indicesList.getSource();
    }
    
    protected abstract FocusModel<T> getFocusModel();
    protected abstract void focus(int index);
    protected abstract int getFocusedIndex();
    

}
