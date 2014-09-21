/*
 * Created on 19.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

/**
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
 * Use-case for semantically richer selectNext/Prev: 
 *   select the next selectable, internal profiteers  
 *   
 * - ChoiceBoxSelectionModel: disregard separators - incorrectly
 *   handled in selectIndex (which shouldn't select arbitrary
 *   values if the index refers to a separator but do nothing)
 * - TabPaneSelectionModel: behaviour manually calls selectNext
 *   until a not disabled tab is found  
 *   
 * Enhancements
 * 
 * - add notion of selectable and specify method behavior against it
 * - compplete api to be fully symetric in selectXX vs clearAndSelectXX
 *   (aka: add vs. setSelection)
 * - future: play with adding selectToXX() - that's a major requirement
 *   in behavior     
 *   
 * Technically, this should be regarded as a decorating interface to
 * SelectionModel, ideally implemented there
 *   
 * @author Jeanette Winzenburg, Berlin
 */
public interface SelectionModelX<T> {

    /**
     * Checks and returns whether the index is selectable.
     * Default implementation accepts all positive integers.
     * 
     * @param index
     * @return
     */
    default  boolean isSelectable(int index) {
        return index >= 0;
    }
    
    /**
     * Checks and returns whether the item is selectable.
     * Default implementation accepts all items != null;
     * 
     * @param item
     * @return
     */
    default boolean isSelectable(T item) {
        return item != null;
    }
    
    /**
     * Returns whether or not empty selection is allowed.
     * @return
     */
    default boolean allowsEmpty() {
        return true;
    }
    
    /**
     * Returns the selected index.
     * @return
     * 
     * @see javafx.scene.control.SelectModel#clearAndSelect(int)
     */
    int getSelectedIndex();
    /**
     * Sets selection to the index if selectable, does nothing
     * if not. 
     * 
     * POST: 
     * 
     * <code><pre>
     * int old = getSelectedIndex();
     * clearAndSelect(index); // index != old
     * if (isSelectable(index)) {
     *    assertEquals(index, getSelectedIndex());
     *    forEach selectableIndices != index:
     *       assertFalse(isSelected(other));
     * } else {
     *    assertEquals(old, getSelectedIndex());
     * }     
     * </pre></code>
     * 
     * Note: this default implementation assumes allowsEmpty. Subclasses
     * with !allowsEmpty must re-implement with some internal
     * clearing of the selection to guarantee the at least one selected
     * at the end.
     * 
     * @param index
     * @see javafx.scene.control.SelectModel#clearAndSelect(int)
     */
    default void clearAndSelect(int index) {
        if (isSelectable(index)) {
            // PENDING JW: allowsEmpty might object?
            clearSelection();
            select(index);
        }
    };
    
    /**
     * Adds the index to the selection if selectable, does nothing
     * otherwise. Subclasses may constrain the number of selections,
     * such that formerly selected indices are cleared.
     * <p>
     * 
     * POST:
     * <code><pre>
     * int old = getSelectedIndex();
     * select(index); // index != old
     * if (isSelectable(index)) {
     *     assertEquals(index, getSelectedIndex());
     *     // no constraint for old selections
     * } else {
     *     assertEquals(old, getSelectedIndex());
     * }
     * </pre></code>
     * @param index
     * 
     * @see javafx.scene.control.SelectModel#select(int)
     */
    void select(int index);
    
    /**
     * Clears the selection of the given index if it is selected and 
     * if it doesn't 
     * violate the allowsEmpty constraint, does nothing otherwise.
     * <p>
     * 
     * POST
     * 
     * <code><pre>
     * if (!isSelected(index)) //do nothing;
     * if (allowsEmpty() || 
     *    (!allowsEmpty() && !isEmpty() after)
     *    assertFalse(isSelected(index));
     * } else {
     *    // don't change anything
     *    assertTrue(isSelected(index)); 
     * }   
     * </pre></code>
     * @param index the index to clear
     */
    void clearSelection(int index);
    
    /**
     * Clears all selections if emptySelectionAllowed, does nothing 
     * otherwise.
     * <p>
     * 
     * PENDING JW: allowsEmpty might be an over-complication at this stage ..
     * Check if subclasses are allowed to enforce at least one selected
     * without having it specified here.
     * <p>
     * POST:
     * 
     * <code><pre>
     * if (allowsEmpty) {
     *    assertTrue(isEmpty()); 
     * } else {
     *    assertFalse(isEmpty());
     * }
     * </code></pre>
     * 
     * @see javafx.scene.control.SelectModel#clearSelection();
     * 
     */
    void clearSelection();
    
//-------------------------- semantic navigation
    
    /**
     * Sets the selection to the first selectable index, does nothing if 
     * there is none. 
     */
    void clearAndSelectFirst();
    
    /**
     * Adds the first selectable index to the selection, does nothing if
     * there is none.
     */
    void selectFirst();
    
    /**
     * Sets the selection to the last selectable index, does nothing if 
     * there is none. 
     */
    void clearAndSelectLast();
    
    /**
     * Adds the last selectable index to the selection, does nothing if
     * there is none.
     */
    void selectLast();
    
    /**
     * Sets the selection to the next selectable index, does nothing if
     * there is none.
     */
    void clearAndSelectNext();
    
    /**
     * Adds next selectable index to the selection, does nothing
     * if there is none.
     */
    void selectNext();
    
    /**
     * Sets the selection to the next selectable index, does nothing if
     * there is none.
     * 
     * PENDING JW: thought that this could be handled in the default - 
     * but couldn't. Semantically, should delegate to selectPrevious but
     * can't clear is we don't know whether there _is_ a selectable 
     * previous.
     * 
     */
    void clearAndSelectPrevious();
    
    /**
     * Adds the selection to the next selectable index, does nothing
     * if there is none.
     */
    void selectPrevious();
    
}
