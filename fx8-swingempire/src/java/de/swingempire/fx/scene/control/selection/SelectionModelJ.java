/*
 * Created on 19.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.control.SelectionModel;

/**
 * Going back (compared to SelectionModelX): let implementations handle 
 * - allowsEmpty
 * 
 * In this experiment, there's a notion of selectable based
 * on index being in-range of items, that is 
 * 
 * <code><pre>
 * 0 <= selectedIndex < getItemCount();
 * </pre></code>
 * 
 * Without a formal itemCount, there's no way of enforcing, so we added
 * it here. CHANGED JW: added getItemCount() and backing list.
 * 
 * We have a notion of set/add selection, names slightly unusual following
 * rules in SelectionModel:
 * 
 * select(...) -> addSelection
 * clearAndSelect(..) -> setSelection
 * 
 * As this base-implementation is biased for single-selection, all default methods
 * ultimately delegate to clearAndSelect - it's up to
 * subclasses to override for multiple. All the add/set navigational methods
 * delegate to their base add/set.
 * 
 * PENDING JW: add select(T) and selectedItem.
 * 
 * The basic idea is the same as SelectionModel: the selection is backed
 * by a list of items and it is the responsibility of the model to 
 * update itself if the backing model changes. The exact manner of selection
 * change can be re-specified by subclasses.
 * 
 * The base behaviour tries to keep as much of the selection state constant,
 * in particular (assuming selectedIndex >= 0) 
 * - insert/remove items < selectedIndex and insert == selectedIndex
 *   will move it such that the selected item will be constant
 * - insert/remove items > selectedIndex will not change selectionState  
 * - remove item at selectedIndex will keep the selectedIndex constant, that
 *   is change the selectedItem to next (there are other options, see RT-3??)
 *   Will clear selection if the removed was the single item in the list.
 * - replace/update item at selectedIndex will keep selectedIndex constant, thus
 *   changing selected item to new/changed item
 * - complete change of all items (setAll or new itemsList) will clear selection
 * 
 * Note that for a list with a single item, there's no way to differentiate between
 * replace and setAll. This ambiguity has to be solved, how?   
 * 
 * SelectionModel which allows for richer semantic value of
 * the sugar methods.
 * 
 * Issues:
 * 
 * ListView et al should use semantic methods selectNext/First 
 *     https://javafx-jira.kenai.com/browse/RT-30537
 * SelectionModel remove or keep semantic methods    
 *     https://javafx-jira.kenai.com/browse/RT-15128
 * SelectionModel incomplete api    
 *     https://javafx-jira.kenai.com/browse/RT-38510
 * 
 * Enhancements
 * 
 * - CHANGED JW - removed, compared to SelectionModelX
 *   add notion of selectable and specify method behavior against it
 * - compplete api to be fully symetric in selectXX vs clearAndSelectXX
 *   (aka: add vs. setSelection)
 * - future: play with adding selectToXX() - that's a major requirement
 *   in behavior     
 *   
 * Technically, this should be regarded as a decorating interface to
 * SelectionModel, ideally implemented there
 *   
 * @author Jeanette Winzenburg, Berlin
 * @see SelectionModel
 */
public interface SelectionModelJ<T> {

    /**
     * Checks and returns whether the index is selectable. 
     * 
     * This implementations accepts all positive indices < itemCount.
     * 
     * @param index
     * @return
     */
    default boolean isSelectable(int index) {
        return index >= 0 && index < getItemCount();
    }
    
    /**
     * Returns the number of backing items.
     * @return
     */
    int getItemCount();
    
    /**
     * Returns the selected index or -1 if none selected.
     * 
     * @return
     * @see javafx.scene.control.SelectModel#getSelectedIndex()
     */
    default int getSelectedIndex() {
        return selectedIndexProperty().get();
    }
    
    /**
     * Returns the property holding the selectedIndex.
     */
    ReadOnlyIntegerProperty selectedIndexProperty();
 
    /**
     * Sets selection to the index if selectable, does nothing otherwise. 
     * 
     * Note: we delegate from select to here - that's because we want
     * to keep the notification minimal: 
     * 
     * @param index
     * @see javafx.scene.control.SelectModel#clearAndSelect(int)
     */
    void clearAndSelect(int index);
    
    /**
     * Adds the index to the selection if selectable, does nothing otherwise.
     * 
     * @param index
     * @see javafx.scene.control.SelectModel#select(int)
     */
    default void select(int index) {
        if (!isSelectable(index)) return;
        clearAndSelect(index);
    }
    
    /**
     * Clears the selection at the given index if it is selected,
     * does nothing otherwise.
     * 
     * @param index the index to clear
     * @see javafx.scene.control.SelectModel#clearSelection(int)
     */
    void clearSelection(int index);
    
    /**
     * Returns true if the index is selected, false otherwise.
     * 
     * This default implementation returns true if index selectable
     * and same as selectedIndex.
     * 
     * @param index
     * @return
     */
    default boolean isSelected(int index) {
        if (!isSelectable(index)) return false;
        return getSelectedIndex() == index;
    }
    
    /**
     * Returns true if nothing selected, false otherwise.
     * This implmentation returns true if selectedIndex < 0;
     * @return
     */
    default boolean isEmpty() {
        return getSelectedIndex() < 0;
    }
    /**
     * Clears all selections.
     * 
     * @see javafx.scene.control.SelectModel#clearSelection();
     * 
     */
    void clearSelection();
    
//-------------------------- semantic navigation
    
    /**
     * Sets the selection to the first selectable index, does nothing if 
     * there is none. 
     * 
     * This default implemenation calls clearAndSelect(0).
     */
    default void clearAndSelectFirst() {
        int next = 0;
        while (!isSelectable(next) && next < getItemCount()) {
            next++;
        }
        clearAndSelect(next);
    };
    
    /**
     * Adds the first selectable index to the selection, does nothing if
     * there is none.
     * 
     * This default implementation calls select(0).
     */
    default void selectFirst() {
        int next = 0;
        while (!isSelectable(next) && next < getItemCount()) {
            next++;
        }
        select(next);
    }
    
    /**
     * Sets the selection to the last selectable index, does nothing if 
     * there is none. 
     * 
     * Note: can't have a default without notion of itemCount?
     */
    default void clearAndSelectLast() {
        int last = getItemCount() - 1;
        while (!isSelectable(last) && last >= 0) {
            last--;
        }
        clearAndSelect(last);
    }
    
    /**
     * Adds the last selectable index to the selection, does nothing if
     * there is none.
     * 
     */
    default void selectLast() {
        int last = getItemCount() - 1;
        while (!isSelectable(last) && last >= 0) {
            last--;
        }
        select(last);
    }
    
    /**
     * Sets the selection to the next selectable index, does nothing if
     * there is none.
     * 
     * This implemenations navigates relative to getSelectedIndex(). Note
     * that it is safe to over-shoot the index as clearAndSelect must
     * protect itself against off-range indices.
     */
    default void clearAndSelectNext() {
        int next = getSelectedIndex() + 1;
        while (!isSelectable(next) && next < getItemCount()) {
            next++;
        }
        clearAndSelect(next);
    }
    
    /**
     * Adds next selectable index to the selection, does nothing
     * if there is none.
     */
    default void selectNext() {
        int next = getSelectedIndex() + 1;
        while (!isSelectable(next) && next < getItemCount()) {
            next++;
        }
        select(next);
    }
    
    /**
     * Sets the selection to the previous selectable index, does nothing if
     * there is none.
     * 
     * This implementation navigates one back relative to getSelectedIndex, 
     * selects last if empty.
     * Note that it relies on select to do nothing if index < 0. 
     * 
     */
    default void clearAndSelectPrevious() {
        int current = isEmpty() ? getItemCount() : getSelectedIndex();
        int previous = current - 1;
        while (!isSelectable(previous) && previous >= 0) {
            previous--;
        }
        clearAndSelect(previous);
    }
    
    /**
     * Adds the selection to the previous selectable index, does nothing
     * if there is none.
     * 
     * This default implementation navigates back relative to getSelectedIndex until 
     * it finds a selectable index. If selection is empty, it starts with trying
     * to select last.
     * 
     * Note that it relies on select to do nothing if index < 0. 
     */
    default void selectPrevious() {
        int current = isEmpty() ? getItemCount() : getSelectedIndex();
        int previous = current - 1;
        while (!isSelectable(previous) && previous >= 0) {
            previous--;
        }
        select(previous);
    }
    
}
