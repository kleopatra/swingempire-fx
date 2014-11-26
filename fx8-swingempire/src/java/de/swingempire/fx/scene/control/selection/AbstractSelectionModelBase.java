/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;
import de.swingempire.fx.control.SynchScrollBars;

/**
 * Replacement of MultipleSelectionModelBase. Uses TransformLists to handle selectedIndices/-items.
 * 
 * Note: for now, this assumes an observableList as backing items.<p>
 * 
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
     * Implemented to call itemsChanged.
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
     * @param index must be valid?
     * @param indices 
     */
    @Override
    public void selectIndices(int index, int... indices) {
        if (indices == null) indices = new int[0];
        List<Integer> validIndices = new ArrayList<>();
        if (isSelectable(index)) validIndices.add(index);
        for (Integer value : indices) {
            if (isSelectable(value)) validIndices.add(value);
        }
        if (validIndices.isEmpty()) return;
        // arggghhh ....
        int[] all = new int[validIndices.size()];
        for (int i = 0; i < validIndices.size(); i++) {
            all[i] = validIndices.get(i);
        }
        doSelectIndices(true, all);
    }

    /**
     * Adds/sets the given indices. Enforces selectionMode. Last index in
     * array will be selectedIndex.
     * 
     * @param add flag to control add/set, if true will be added, will 
     *    be set otherwise.
     * @param indices the indices to set, must have minimal length of 1 and only
     *   contain indices that are in-range.
     * @throws NullPointerException if indices are null
     */
    protected void doSelectIndices(boolean add, int... indices) {
        Objects.requireNonNull(indices, "indices must not be null");
        if (getSelectionMode() == SelectionMode.SINGLE) {
            add = false;
            indices = new int[] {indices[indices.length -1]};
        }        
        if (add) {    
            indicesList.addIndices(indices);
        } else {
            indicesList.setIndices(indices);
        }
        syncSingleSelectionState(indices[indices.length - 1]);
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

    /**
     * Updates single selectionState to selectedIndex. Called whenever
     * selectedIndices are changed, either directly from the modifying methods
     * or on receiving changes from the backing list in itemsContentChanged.
     * <p>
     * 
     * This impl implementation 
     * <li> sets selectedIndex
     * <li> sets selectedItem to item at selectedIndex or null if -1
     * <li> focus to selectedIndex
     * 
     * @param selectedIndex
     */
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
        doSelectIndices(false, index);
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
        doSelectIndices(true, index);
    }

    @Override
    public void select(T obj) {
        int index = getItems().indexOf(obj);
        if (index > -1) {
            select(index);
        } else {
            selectExternalItem(obj);
        }
    }

    /**
     * Selects an item that is not contained in the backing list and updates
     * single selection state as needed.<p>
     * 
     * This implementation clears selected index.
     * 
     * @param obj
     */
    protected void selectExternalItem(T obj) {
        setSelectedItem(obj);
        setSelectedIndex(-1);
    }

    /**
     */
    @Override
    public void clearSelection(int index) {
        if (!isSelected(index)) return;
        indicesList.clearIndices(index);
        syncSingleSelectionState(-1);
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
                // need to select vs. sync because can't yet be in selectedIndices
                if (indicesList.contains(selectedIndex)) 
                    throw new IllegalStateException("new selectedIndex cannot be in indices " + selectedIndex);
                select(selectedIndex);
            }
            return;
        }
        
        // since here: oldSelectedIndex > -1 expected and oldSelectedItem != null
        if (oldSelectedItem == null) 
            throw new IllegalStateException("selectedItem must not be null for index: " + oldSelectedIndex);
        if (oldSelectedIndex < 0) throw new IllegalStateException("expected positive selectedIndex");

        // we had selectedItem/index pair that was part of the model
        // first handle empty items
        if (getItems().isEmpty()) {
            clearSelection();
            return;
        }
        // sanity: selectedIndex had been in indicesList (cough ..)
        List<Integer> oldIndices = indicesList.getOldIndices();
        if (!oldIndices.contains(oldSelectedIndex))
            throw new IllegalStateException("oldSelectedIndex: " + oldSelectedIndex + 
                    " expected in oldIndices, but was not: " + oldIndices);
        // Note JW: isSelected tests whether the given index is part of selectedIndices
        // (even doc'ed as such - invalidly in SelectionModel)
        // vs. testing against -1 (which might or not be an option)

        // oldSelectedItem still in selectedItems
        // happens if anything removed above the selectedIndex,
        // quick way to update index
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
            // syncSingleSelectionState(-1);
        }
        
        // test if item at selectedIndex had been replaced
        // PENDING JW: is this the correct logic to test for a replaced?
        // doesn't look so .. might want to do it the other way round: 
        // test for a replaced, then look for the index?
        // anyway, verified that we reach it on items.set(selectedIndex, newItem);
        if (indicesList.contains(oldSelectedIndex)) {
            int newSelectedIndex = -1;
            // JW: reset, just to be on the safe side, if it were accessed above
            c.reset();
            while (c.next()) {
                // PENDING JW: this is not good enough for multiple subchanges
                if (c.wasReplaced() && c.getRemoved().contains(oldSelectedItem)) {
                   newSelectedIndex = c.getFrom() + c.getRemoved().indexOf(oldSelectedItem);
                   break;
                }
            }
            if (newSelectedIndex > -1) {
                // the assumption is that the item at oldindex had been replaced 
                // implies that oldIndex == newIndex
                if (newSelectedIndex != oldSelectedIndex) 
                    throw new IllegalStateException("same old/new index expected but were " 
                            + oldSelectedIndex + "/" +newSelectedIndex);
                // index-related state unchanged, update item only
                syncSingleSelectionState(newSelectedIndex);
                return;
            }
            LOG.info("do we get here? " + c);
        }
        // check empty selectedItems/indices: selectedIndex/item was removed
        // need to adjust the selectedIndex by the removed items and decide
        // what to do with the result (??)
        // not good enough, anyway: selectedIndex might be removed, other selection
        // not removed - empty most probably is a special case of remove?
        if (indicesList.isEmpty()) {
            c.reset();
        }
        
        
        // index still valid - no meaning in itself, though...
//        if (oldSelectedIndex < getItems().size() 
//            && !indicesList.contains(oldSelectedIndex)) {
//            // PENDING JW: when do we get here? 
//            // get here on remove at selectedIndex, also multiple removes
//            T newSelectedItem = getItems().get(oldSelectedIndex);
//            String msg = "old/new/setSelectedItem: " + getSelectedItem() + "/" + newSelectedItem;
//            select(oldSelectedIndex);
////            LOG.info(msg + " / " + getSelectedItem());
//            return;
//        }

        // wrong expectation: selectedIndex/item might have been removed
        // but other selections not!
//        if (!indicesList.isEmpty()) 
//            throw new IllegalStateException("expected empty indices, but was: " + indicesList);
//        if (!indexedItems.isEmpty()) 
//            throw new IllegalStateException("expected empty items, but was: " + indexedItems);
        
        // before the change in items, both selectedIndex and selectedItem have been
        // part of the list, the change removed them
        c.reset();
        
        // Permutation, remove (where?)
//        LOG.info("missed anything? " + c);
        // oldSelectedIndex >= getItemCount()
        if (getItemCount() > 0) {
            select(getItemCount() - 1);
        } else {
            syncSingleSelectionState(-1);
        }
    }

    protected ObservableList<? extends T> getItems() {
        return indicesList.getSource();
    }
    
    protected abstract FocusModel<T> getFocusModel();
    protected abstract void focus(int index);
    protected abstract int getFocusedIndex();
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractSelectionModelBase.class.getName());
}
