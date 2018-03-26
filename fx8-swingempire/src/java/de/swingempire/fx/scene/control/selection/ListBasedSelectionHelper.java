/*
 * Created on 07.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

/**
 * This class helps a (Abstract)MultipleSelectionModel to keep its single selection state 
 * sync'ed to its multiple selection state when the source list changes. It assumes 
 * that multiple selection state is completely update before seeing the change.
 * 
 * <p>
 * 
 * This helper can handle a list as source.
 * 
 * PENDING JW:
 * - should take a property of items (vs. the raw items)
 * - should wrap into a ListProperty itself (if not yet done), currently simply
 *   assumes that the caller handles the wrap
 * - need to support configuration of handling of changes (removeAt, setItems/All, 
 *   uncontained, set(..))
 * - bug: replacing a single element list by a single element list doesn't clear
 *   out selection state (who's responsible? IndicesList/IndexMappedList or this?)     
 * 
 * @see AbstractSelectionModelBase
 * @see TreeBasedSelectionHelper
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListBasedSelectionHelper<T> {

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
    private AbstractSelectionModelBase<T> selectionModel;
    
    /**
     * 
     * @param selectionModel
     * @param items
     */
    public ListBasedSelectionHelper(AbstractSelectionModelBase<T> selectionModel, ObservableList<T> items) {
        this.selectionModel = selectionModel;
        items.addListener(weakItemsContentListener);
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
        int oldSelectedIndex = selectionModel.getSelectedIndex();
        T oldSelectedItem = selectionModel.getSelectedItem();
        int oldFocus = selectionModel.getFocusedIndex();
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
            if (oldSelectedItem != null && c.getList().contains(oldSelectedItem)) {
                int selectedIndex = c.getList().indexOf(oldSelectedItem);
                // need to select vs. sync because can't yet be in selectedIndices
                selectionModel.select(selectedIndex);
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
        if (c.getList().isEmpty()) {
            selectionModel.clearSelection();
            return;
        }
        
        // Note JW: isSelected tests whether the given index is part of selectedIndices
        // (even doc'ed as such - invalidly in SelectionModel)
        // vs. testing against -1 (which might or not be an option)

        
        // short-cut 3: oldSelectedItem still in selectedItems
        // happens on all changes except a remove of the selected,
        // need to update index
        if (selectionModel.getSelectedItems().contains(oldSelectedItem)) {
            int indexInIndices = selectionModel.getSelectedItems().indexOf(oldSelectedItem);
            int sourceIndex = selectionModel.getSelectedIndices().get(indexInIndices);
            // PENDING JW: here's the only place where the listener
            // accesses private api - can do anything to remove?
            // wouldn't matter to use select, except for focus
            selectionModel.syncSingleSelectionState(sourceIndex, sameFocus);
            if (!sameFocus) {
               updateFocus(c); 
            }
//            select(sourceIndex);
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
        T oldSelectedItem = selectionModel.getSelectedItem();
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
            selectionModel.select(c.getFrom());
        } else if (c.getAddedSize() == c.getList().size()) {
            // all changed
            selectionModel.clearSelection();
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
        // PENDING JW: think about discontinous removes - c.getFrom
        // really the position that we need?
        selectionModel.select(Math.min(selectionModel.getItemCount() - 1, c.getFrom()));
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
        if (!(selectionModel.getFocusModel() instanceof FocusModelSlave)) return;
        // no focus, nothing to do
        if (selectionModel.getFocusedIndex() < 0) return;
        c.reset();
        c.next();
        // first change after index, return
        if (c.getFrom() > selectionModel.getFocusedIndex()) return;
        
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
                if (selectionModel.getFocusedIndex() < accumulatedIndex) break;
                accumulatedDelta -= c.getRemovedSize();
            } else if (c.wasAdded()) {
                if (hit) 
                    throw new IllegalStateException("expected removed/added only but was: " + c);
                accumulatedIndex += c.getFrom();
                if (selectionModel.getFocusedIndex() < accumulatedIndex) break;
                accumulatedDelta += c.getAddedSize();
                
            }
        }
        
        if (accumulatedDelta == 0) return;
        selectionModel.focus(selectionModel.getFocusedIndex() + accumulatedDelta);
        
    }
    

}
