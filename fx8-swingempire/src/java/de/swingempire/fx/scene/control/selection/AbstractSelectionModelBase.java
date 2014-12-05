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

/**
 * Replacement of MultipleSelectionModelBase. Lets IndexedItemsController manage 
 * the technicalities of selectedIndices/-Items lists. Handles semantics of 
 * multiple selection and sync to single selection state.  
 * 
 * <p> 
 * 
 * NOTE: for now, this assumes an ObservableList as backing items. Most probably
 * extendable to not (need another implementation of IndexedItems) such that
 * applicable for tree structures as well. <p>
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

    protected IndexedItemsController<T> controller;
//    protected IndicesList<T> indicesList;
//    protected IndexMappedList<T> indexedItems;
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
    
//-------------- MultipleSelectionModel api
    
    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return controller.getIndices();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return controller.getIndexedItems();
    }

    /**
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

    @Override
    public void selectAll() {
        if (getSelectionMode() == SelectionMode.SINGLE) return;
        controller.setAllIndices();
        syncSingleSelectionState(getItemCount() - 1);
    }

//------------------- SelectionModel api
    
    @Override
    public void clearAndSelect(int index) {
        if (!isSelectable(index)) return;
        doSelectIndices(false, index);
    }

    @Override
    public void select(int index) {
        if (!isSelectable(index)) return;
        doSelectIndices(true, index);
    }

    @Override
    public void select(T obj) {
        // PENDING JW: here we assume the backing data 
        // being a list!
        int index = getItems().indexOf(obj);
        if (index > -1) {
            select(index);
        } else {
            selectExternalItem(obj);
        }
    }

    /**
     * PENDING JW: this incorrectly used to clear the selectedIndex/Item even if != index.
     * Need to check what really should happen, similar to remove selectedItem in 
     * list? Unsolved in core as well .. though core doesn't touch focus/selectedIndex.
     * <p>
     * For now, doing similar: clear out single selection state (but leaving focus untouched) 
     * if index was last selected, do nothing otherwise.
     * 
     * @see MultipleSelectionIssues#testIndicesSelectedIndexIsUpdatedAfterUnselect()
     */
    @Override
    public void clearSelection(int index) {
        if (!isSelected(index)) return;
        controller.clearIndices(index);
        if (isEmpty()) {
            syncSingleSelectionState(-1, false);
        }    
    }

    @Override
    public void clearSelection() {
        controller.clearAllIndices();
        syncSingleSelectionState(-1);
    }

    @Override
    public boolean isSelected(int index) {
        return getSelectedIndices().contains(index);
    }

    @Override
    public boolean isEmpty() {
        return getSelectedIndices().isEmpty();
    }

//------------------- navigational methods    
    @Override
    public void selectPrevious() {
        int previous = getCurrentIndex() - 1;
        if (previous < 0) previous = getItemCount() - 1;
        if (previous < 0) return;
        select(previous);
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

//--------------------- workhorsesd 
    
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
            controller.addIndices(indices);
        } else {
            controller.setIndices(indices);
        }
        syncSingleSelectionState(indices[indices.length - 1]);
    }
    
    /**
     * Updates single selectionState (including focus) to selectedIndex. 
     * @param selectedIndex the new selectedIndex
     * 
     * @see #syncSingleSelectionState(int, boolean)
     */
    protected void syncSingleSelectionState(int selectedIndex) {
        syncSingleSelectionState(selectedIndex, true);
    }

    /**
     * Updates single selectionState to selectedIndex. Called whenever
     * selectedIndices are changed, either directly from the modifying methods
     * or on receiving changes from the backing list in itemsContentChanged.
     * <p>
     * 
     * This implementation 
     * <li> sets selectedIndex
     * <li> sets selectedItem to item at selectedIndex or null if -1
     * <li> focus to selectedIndex
     * 
     * @param selectedIndex the new selectedIndex
     * @param withFocus a boolean indicating whether focus should be synced to the index 
     *   as well
     */
    protected void syncSingleSelectionState(int selectedIndex, boolean withFocus) {
        setSelectedIndex(selectedIndex);
        if (selectedIndex > -1) {
            setSelectedItem(getModelItem(selectedIndex));
        } else {
            // PENDING JW: do better? can be uncontained item
            setSelectedItem(null);
        } 
        if (withFocus) {
            focus(selectedIndex);
        }
    }

    /**
     * Returns a boolean indicating whether the given index can be selected.
     * This implementations returns true if in valid range -1 < index < getItemCount.
     * 
     * @param index
     * @return
     */
    protected boolean isSelectable(int index) {
        return index > -1 && index < getItemCount();
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
     * @return
     */
    private int getCurrentIndex() {
        int current = getFocusedIndex();
        if (current < -1) current = getSelectedIndex();
        return current;
    }

    /**
     * IndicesList/IndexMappedItems have taken care of updating selectedIndices/-items,
     * here we need to update selectedIndex/selectedItem.
     * <p>
     * 
     * PENDING JW: focus handling incorrect - if focus != selected before the change,
     * the focus has to be treated separately
     *  
     * @param c the change received from the backing items list
     */
    protected void itemsChanged(Change<? extends T> c) {
        int oldSelectedIndex = getSelectedIndex();
        T oldSelectedItem = getSelectedItem();
        int oldFocus = getFocusedIndex();
        boolean sameFocus = oldFocus == oldSelectedIndex;
        
                
        // ------- handle short-cuts ----------------
        // short-cut 1: no selectedIndex
        // can't be changed to selected by changes of items so there is
        // basically nothing to do, except checking  
        // if we had an external selected item that's now contained
        // if so select, otherwise do nothing
        if (oldSelectedIndex < 0) {
            // no selected index, check selectedItem:
            // it had not been part of the items before the change (if it had
            // the selectedIndex wouldn't be < 0) but now might be
            if (oldSelectedItem != null && getItems().contains(oldSelectedItem)) {
                int selectedIndex = getItems().indexOf(oldSelectedItem);
                // need to select vs. sync because can't yet be in selectedIndices
                select(selectedIndex);
            } else if (!sameFocus){ // still unselected but need to handle focus
                updateFocus(c);
            }
            return;
        }
        
        // since here we had a selectedIndex/item pair that was contained in the list
        // oldSelectedIndex > -1 expected and oldSelectedItem != null
        // temporarily throw for sanity
        if (oldSelectedItem == null) 
            throw new IllegalStateException("selectedItem must not be null for index: " + oldSelectedIndex);
        if (oldSelectedIndex < 0) throw new IllegalStateException("expected positive selectedIndex");

        // short-cut 2: empty items - clear selection
        if (getItems().isEmpty()) {
            clearSelection();
            return;
        }
        
        // Note JW: isSelected tests whether the given index is part of selectedIndices
        // (even doc'ed as such - invalidly in SelectionModel)
        // vs. testing against -1 (which might or not be an option)

        
        // short-cut 3: oldSelectedItem still in selectedItems
        // happens on all changes except a remove of the selected,
        // need to update index
        if (getSelectedItems().contains(oldSelectedItem)) {
            int itemsIndex = controller.sourceIndexOf(oldSelectedItem);
            syncSingleSelectionState(itemsIndex, sameFocus);
            if (!sameFocus) {
               updateFocus(c); 
            }
            return;
        } 
        
        //-------------- end of short-cuts
        // selectedItem was removed: could have been a "true" remove
        // or a replace
        selectedItemChanged(c);
    }

    /**
     * Called during updating singleSelection state from listener to items.
     * At this point, the old selectedIndex/Item are still valid while
     * the selectedIndices/Items are updated. The pair had been a valid
     * entry in the list before the change, but no longer is.<p>
     * 
     * Need to handle several scenarios:
     * <li> item was really removed, need to update index/item to a new
     *      index/item (RT-??, strategy on either advance/keep/clear
     * <li> item was single-replaced, by items.set(selectedIndex, newItem), need
     *      to update selectedItem to new 
     * <li> item was bulk-replaced, by items.setAll(..), setItems(newItems), ?,
     *      need to clear selection (?)
     * 
     * <p>  
     *    
     * Implementation note: the assumption here is that all change types except
     * removes/replaced which might contain the selectedItem are handled. For testing,
     * we are throwing IllegalStateException for all others. Also note that 
     * the selectedItem can only be in one getRemoved sublist,
     * once we found it we could break out, for now don't though, instead throwing 
     * again on the second time around!            
     * 
     * <p>
     * 
     * @param c
     */
    protected void selectedItemChanged(Change<? extends T> c) {
//        FXUtils.prettyPrint(c);
        T oldSelectedItem = getSelectedItem();
        boolean found = false;
        c.reset();
        while(c.next()) {
            if (c.wasPermutated()) {
                throw new IllegalStateException("expected removed/replaced but was " + c);
            } else if (c.wasUpdated()) {
                throw new IllegalStateException("expected removed/replaced but was " + c);
            } else if (c.wasReplaced()) {
                if (c.getRemoved().contains(oldSelectedItem)) {
                    if (found) 
                        throw new IllegalStateException("old item found in more than one subchange " + c);
                    found = true;
                    selectedItemReplaced(c);
                }
            } else if (c.wasRemoved()) {
                if (c.getRemoved().contains(oldSelectedItem)) {
                    if (found) 
                        throw new IllegalStateException("old item found in more than one subchange " + c);
                    found = true;
                    selectedItemRemoved(c);
                }
            } else if (c.wasAdded()) {
                throw new IllegalStateException("expected removed/replaced but was " + c);
            }
        }
    }

    /**
     * @param c the change that contains the old selectedItem in its removedList, guaranteed
     *   to be of type replaced.
     */
    protected void selectedItemReplaced(Change<? extends T> c) {
        if (c.getRemovedSize() == 1 && c.getAddedSize() == 1) {
            // single replace (not entirely safe, could be a 
            // setAll with a single new element
            select(c.getFrom());
        } else if (c.getAddedSize() == c.getList().size()) {
            // all changed
            clearSelection();
        }
    }
    
    /**
     * Called when selectedItem had been really removed and in the removedList of the 
     * given change. The change is iterated to the subchange of the remove.
     *  
     * Subclasses can implement a strategy (see RT-??) for updating selectedIndex.
     * here.
     * @param c the change that contains the old selectedItem in its removedList, guaranteed
     *    to be of type removed.
     */
    protected void selectedItemRemoved(Change<? extends T> c) {
        select(Math.min(getItemCount() - 1, c.getFrom()));
    }
    
    /**
     * Called if focus wasn't adjusted along with selection. 
     * 
     * Implemented to do nothing on permutated/updated/replaced and
     * adjust focus on added/removed. Doesn't expect mixtures of the
     * former type with the latter types. Revisit if needed.
     * 
     * <p>
     * PENDING JW: 
     * <li> unselected permutation not yet handled.
     * <li> focus on setAll (aka: block replaced)? With auto-focus
     *      enabled, that should be handled ... where?
     * 
     * <p>
     * Note: commented block was  
     * just copied (and fixed nested lookup) from ListViewFocusModel. 
     * which always adds sum of all added/removed! Now replaced by
     * doing so only until index reached.
     * 
     * @param c
     */
    protected void updateFocus(Change<? extends T> c) {
        // no slave, backout
        if (!(getFocusModel() instanceof FocusModelSlave)) return;
        // no focus, nothing to do
        if (getFocusedIndex() < 0) return;
        c.reset();
        c.next();
        // first change after index, return
        if (c.getFrom() > getFocusedIndex()) return;
        
        c.reset();
        int accumulatedIndex = 0;
        int accumulatedDelta = 0;
        boolean hit = false;
        while (c.next()) {
            // PENDING JW: revisit when better understanding
            // possible mixtures of notifications
            // the overall assumption here is:
            // single permutated 
            // replaced with addedSize == removedSize
            // multiple add/removes
            if (c.wasPermutated()) {
                // handled by select? or not?
                hit = true;
            } else if (c.wasUpdated()) {
                // nothing to do: updating the item doesn't 
                // effect the index
            } else if (c.wasReplaced()) {
                hit = true;
                // PENDING JW: don't really know which modifications
                // may fire a replaced (except single/bulk sets)
                // do nothing for now ... might have to
            } else if (c.wasRemoved()) {
                if (hit) 
                    throw new IllegalStateException("expected removed/added only but was: " + c);
                accumulatedIndex += c.getFrom();
                if (getFocusedIndex() < accumulatedIndex) break;
                accumulatedDelta -= c.getRemovedSize();
            } else if (c.wasAdded()) {
                if (hit) 
                    throw new IllegalStateException("expected removed/added only but was: " + c);
                accumulatedIndex += c.getFrom();
                if (getFocusedIndex() < accumulatedIndex) break;
                accumulatedDelta += c.getAddedSize();
                
            }
        }
        
        if (accumulatedDelta == 0) return;
        focus(getFocusedIndex() + accumulatedDelta);
        
        // JW: below is compy from original (removed nested while, though)
//        while (c.next()) {
//            // looking at the first change
//            int from = c.getFrom();
//            if (getFocusedIndex() == -1 || from > getFocusedIndex()) {
//                return;
//            }
//        }
//        boolean added = false;
//        boolean removed = false;
//        int addedSize = 0;
//        int removedSize = 0;
//        // PENDING JW: checking against from doesn't help
//        // because it's the coordinate of the _current_ list state
//        // while focus is _old_ list state
//        while (c.next()) { // && c.getFrom() <= getFocusedIndex()) {
//            added |= c.wasAdded();
//            removed |= c.wasRemoved();
//            addedSize += c.getAddedSize();
//            removedSize += c.getRemovedSize();
//        }
//    
//        if (added && !removed) {
//            focus(getFocusedIndex() + addedSize);
//        } else if (!added && removed) {
//            // fix of navigation issue on remove focus at 0
//            focus(Math.max(0, getFocusedIndex() - removedSize));
//        }
    }

    protected ObservableList<? extends T> getItems() {
        return controller.getSource();
    }

    /**
     * Returns the number of items in the data model that underpins the control.
     * An example would be that a ListView selection model would likely return
     * <code>listView.getItems().size()</code>. The valid range of selectable
     * indices is between 0 and whatever is returned by this method.
     */
    protected int getItemCount() {
        return controller.getSourceSize();
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
        return controller.getSource().get(index);
    };
    

    protected abstract FocusModel<T> getFocusModel();
    protected abstract void focus(int index);
    protected abstract int getFocusedIndex();
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractSelectionModelBase.class.getName());
}
