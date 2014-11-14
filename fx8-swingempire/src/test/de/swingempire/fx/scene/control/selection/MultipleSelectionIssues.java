/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreDocErrors;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.ListChangeReport;
import de.swingempire.fx.util.StageLoader;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;
/**
 * Tests behaviour of MultipleSelection api.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class MultipleSelectionIssues<V extends Control, T extends MultipleSelectionModel> {
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();


    /**
     * The model set to the views. It contains 9 string items, originally
     * in descending order. Invoking sort will revert the order.
     */
    protected ObservableList items;
    protected V view;
    protected boolean multipleMode;

    /**
     * The stageLoader used to force skin creation. It's an artefact of fx
     * instantiation process, not meant to be really used.
     * Note that it's the responsibility of the test method itself (not the setup)
     * to init if needed.
     */
    protected StageLoader loader;

//---------- notification from selectedItems/Indices
    
    /**
     * Any sort on selectedIndices? Or correlation of position of
     * selectedIndices/items?
     * 
     * Undocumented, but seems to be
     */
    @Test
    public void testSelectedIndicesItemsSynced() {
        if (!multipleMode) return;
        int first = 7;
        int[] indices = new int[] {1, 8, 3};
        getSelectionModel().selectIndices(first, indices);
        ObservableList<Integer> selectedIndices = getSelectionModel().getSelectedIndices();
        ObservableList selectedItems = getSelectionModel().getSelectedItems();
        for (int i = 0; i < selectedIndices.size(); i++) {
            int index = selectedIndices.get(i);
            assertEquals(items.get(index), selectedItems.get(i));
        }
    }
    /**
     * Any sort on selectedIndices? Or correlation of position of
     * selectedIndices/items?
     * 
     * Undocumented, but seems to be
     */
    @Test
    public void testSelectedIndicesSorted() {
        if (!multipleMode) return;
        int first = 7;
        int[] indices = new int[] {1, 8, 3};
        getSelectionModel().selectIndices(first, indices);
        ObservableList<Integer> selectedIndices = getSelectionModel().getSelectedIndices();
        int previous = selectedIndices.get(0);
        for (int i = 1; i < selectedIndices.size(); i++) {
            int current = selectedIndices.get(i);
            assertTrue("sorted but was (prev/ current) " + previous + " / " + current, current > previous);
            previous = current;
        }
    }
    /**
     * Why do we get a permutated? How are we supposed to use it?
     */
    @Test
    public void testSelectedIndicesEventsOnAddedItem() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        ObservableList<Integer> indices = getSelectionModel().getSelectedIndices();
        ObservableList<Integer> copy = FXCollections.observableArrayList(indices);
        LOG.info("before add: " + copy);
        ListChangeReport report = new ListChangeReport(indices);
        items.add(0, "newItem");
        LOG.info("after add: " + indices);
        Change c = report.getLastChange();
        prettyPrint(c);
        c.reset();
        c.next();
        for (int i = c.getFrom(); i < c.getTo(); i++) {
            int newIndex = c.getPermutation(i);
            assertEquals("item at oldIndex " + i, copy.get(i), indices.get(newIndex));
        }
    }
    
    @Test
    public void testSelectedItemsEventsOnAddedItem() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        items.add(0, "newItem");
        assertEquals("selected items unchanged on adding above", 0, report.getEventCount());
    }
    
    /**
     * Issue: selectionModel must not fire on reselect selected index
     * 
     * Here: test selectedIndex.
     * 
     * spurned by:
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     */
    @Test
    public void testSelectedItemEventsMultipleReselectSingle() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        assertEquals("received single event", 1, report.getEventCount());
        report.clear();
        getSelectionModel().select(selected);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    /**
     * Issue: selectionModel must not fire on reselect selected index
     * 
     * Here: test selectedIndex.
     * 
     * spurned by:
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     */
    @Test
    public void testSelectedIndexEventsMultipleReselectSingle() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        ChangeReport report = new ChangeReport(getSelectionModel().selectedIndexProperty());
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        assertEquals("received single event", 1, report.getEventCount());
        report.clear();
        getSelectionModel().select(selected);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    /**
     * Issue: selectionModel must not fire on reselect selected index
     * 
     * Here: test selectedIndices.
     * 
     * spurned by:
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     */
    @Test
    public void testSelectedIndicesEventsMultipleReselectSingle() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        getSelectionModel().selectRange(start, end);
        assertEquals("received single event", 1, report.getEventCount());
        assertTrue(wasSingleAdded(report.getLastChange()));
        report.clear();
        getSelectionModel().select(end - 1);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    /**
     * Issue: selectionModel must not fire on reselect selected index
     * 
     * Here: test selectedItems.
     * 
     * spurned by:
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     */
    @Test
    public void testSelectedItemsEventsMultipleReselectSingle() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        getSelectionModel().selectRange(start, end);
        assertEquals("received single event", 1, report.getEventCount());
        assertTrue(wasSingleAdded(report.getLastChange()));
        report.clear();
        getSelectionModel().select(end - 1);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    /**
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     * 
     * This is the test for 37360: clearAndSelect an already selected
     */
    @Test
    public void testSelectedItemsEventsMultipleToSingle() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        getSelectionModel().selectRange(start, end);
        assertEquals("received single event", 1, report.getEventCount());
        assertTrue(wasSingleAdded(report.getLastChange()));
        report.clear();
        getSelectionModel().clearAndSelect(end - 1);
        assertEquals("event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        assertTrue("must be single remove but was " + c, wasSingleRemoved(c));
    }
    
//-------------------- items modification    
    
    /**
     * Here's one branch of listening to items' change:
     * 
     * <code><pre>
     * if selectedIndex == -1 and selectedItem != null 
     *    updateIndex if selectedItem is contained
     * </pre></code>
     * 
     * missing spec, though   
     */
    @Test
    public void testSelectedOnInsertUncontainedSingle() {
        Object uncontained = "uncontained";
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items
        int insertIndex = 3;
        items.add(insertIndex, uncontained);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Reported: 
     * <p>
     * Rinse with multiple indices selected.<p>
     * 
     * Nevertheless, fails due to competing handling of Change: 
     * <li> in itemsContentListener falls into block where the index is updated
     *   to the index position of the item
     * <li> in selectionChanged the wasAdded-block shifts the selectedIndex, thus
     *   shifting away from the already correct index
     * <p>
     * best would be to not separate out both - just let selectionChanged handle all
     * then analyse each block whether or not it requires further action
     * 
     * <hr> ----------- older isssue, fixed as of 8u40b12
     * multiple selection might break super's (tentative) invariant:
     * 
     * <pre><code>
     * if (selectedIndex > -1)
     *   selectedItem == getItems().get(selectedIndex);
     *   </code></pre>
     * 
     * Some time between 8u20 and 8u40b12, MultipleSelectionModelBase was
     * changed to enforce class invariant above. Added setSelectedIndex(-1)
     * if item uncontained.<p>
     * 
     * @see #testSelectedOnInsertUncontainedSingle()
     */
    @Test
    public void testSelectedOnInsertUncontainedMultiple() {
        if (!multipleMode)
            return;
        Object uncontained = "uncontained";
        // prepare state, select a range
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertEquals("sanity: selectedItem on last of range",
                items.get(end - 1), getSelectionModel().getSelectedItem());
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained,
                getSelectionModel().getSelectedItem());
        assertEquals("sanity: selected index removed ", -1, getSelectionModel()
                .getSelectedIndex());
        // make uncontained part of the items
        int insertIndex = 3;
        items.add(insertIndex, uncontained);
        assertEquals("selectedItem unchanged", uncontained,
                getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel()
                .getSelectedIndex());
    }

    /**
     * Is an uncontained selectedItem in selectedItems? 
     * Looks like no: should update doc to clarify that selectedItems are those that are
     * backed by the model.
     */
    @Test
    public void testSelectedItemUncontainedInSelectedItemsSingle() {
        Object uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        assertFalse("uncontained selectedItem part of selectedItems?", getSelectionModel().getSelectedItems().contains(uncontained));
    }
    
    /**
     * Rinse for multiple selections.
     * @see #testSelectedItemUncontainedInSelectedItemsSingle()
     */
    @Test
    public void testSelectedItemUncontainedInSelectedItemsMultiple() {
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        Object uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        assertFalse("uncontained selectedItem part of selectedItems?", getSelectionModel().getSelectedItems().contains(uncontained));
    }
    /**
     * What happens with uncontained when replacing items with list that contains it? 
     * selectedIndex is updated: is consistent with typical handling - a selectedItem
     * that had not been in the list is treated like being independent and left alone.
     */
    @Test
    public void testSelectedOnSetItemsWithUncontained() {
        Object uncontained = "uncontained";
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, uncontained);
        items.setAll(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * What happens if uncontained not in new list? 
     * selectedIndex -1, uncontained still selectedItem
     */
    @Test
    public void testSelectedOnSetItemsWithoutUncontained() {
        Object uncontained = "uncontained";
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, "anything");
        items.setAll(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex unchanged", -1, getSelectionModel().getSelectedIndex());
    }
    
    
//------------ focus on modifying list    
    @Test
    public void testFocusUnselectedUpdateOnInsertAbove() {
        int index = 2;
        getFocusModel().focus(index);
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity: focus taken", index, getFocusModel().getFocusedIndex());
        items.add(0, "new item");
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals(index + 1, getFocusModel().getFocusedIndex());
    }
    
    @Test
    public void testFocusUnselectedUpdateOnRemoveAbove() {
        int index = 2;
        getFocusModel().focus(index);
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity: focus taken", index, getFocusModel().getFocusedIndex());
        items.remove(0);
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals(index - 1, getFocusModel().getFocusedIndex());
    }
    
    /**
     * Navigation disabled if first is selected/focused and removed
     * https://javafx-jira.kenai.com/browse/RT-38785
     * 
     * (fixed for TableView, not for ListView 8u40b12)
     */
    @Test
    public void testFocusFirstRemovedItem() {
        getSelectionModel().select(0);
        assertEquals("sanity: focus in sync with selection", 0, getFocusModel().getFocusedIndex());
        items.remove(0);
        assertEquals(0, getSelectionModel().getSelectedIndex());
        assertEquals(0, getFocusModel().getFocusedIndex());
    }
    /**
     * Conflicting doc:
     * - focusedProperty: only -1 if empty
     * - focus(index): -1 if given index off range
     */
    @Test
    public void testFocusClearedOffRange() {
        getFocusModel().focus(0);
        assertEquals(0, getFocusModel().getFocusedIndex());
        getFocusModel().focus(items.size());
        assertEquals("focus must be cleared on off range", -1, getFocusModel().getFocusedIndex());
    }
    /**
     * Conflicting doc:
     * - focusedProperty: only -1 if empty
     * - focus(index): -1 if given index off range
     */
    @Test
    public void testFocusClearedMinusOne() {
        getFocusModel().focus(0);
        assertEquals("sanity", 0, getFocusModel().getFocusedIndex());
        getFocusModel().focus(-1);
        assertEquals("focus must be cleared on setting -1", -1, getFocusModel().getFocusedIndex());
    }
    
    
    /**
     * Regression testing: removing first selected item doesn't update the item.
     * https://javafx-jira.kenai.com/browse/RT-28637
     */
    @Test
    public void testRemoveSelectedItem_28637() {
        getSelectionModel().select(0);
        Object selectedItem = getSelectionModel().getSelectedItem();
        items.remove(selectedItem);
        assertEquals(getSelectionModel().getSelectedItems().get(0), getSelectionModel().getSelectedItem());
    }
    
    /**
     * Regression testing 
     * https://javafx-jira.kenai.com/browse/RT-38884
     * 
     * One-off error in boundary condition
     * 
     * Error slightly different for TableView/ListView:
     * - listView simply doesn't fire the correct event (removed item is null)
     * - tableView throws NoSuchElement
     */
    @Test
    public void testNoSuchElementOnClear_38884() {
        getSelectionModel().select(0);
        Object item = getSelectionModel().getSelectedItem();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        int size = items.size();
        items.clear();
        assertEquals("sanity: single event", 1, report.getEventCount());
        Change c = report.getLastChange();
        assertNotNull("sanity: the change is not null", c);
        // here we get a NSEE in tableView- wondering where exactly?
        // commenting leads to IndexOutOfbounds below
        // note: if we add the c for printing here, we get an NSEE here
//        assertTrue("last must be single removed but was " + c, FXUtils.wasSingleRemoved(c));
        // without printing we get an IndexOutofBounds when accessing the removed list below
        assertTrue("last must be single removed", wasSingleRemoved(c));
        c.reset();
        c.next();
        assertEquals("removed size", size, c.getRemovedSize());
        // this is throwing before fix of 38884 with an NoSuchElementException 
        // actually, it's not throwing an NSEE, but an IOOBE
        // NSEE only if iterating over the list as done f.i. when printing
        // isn't here, get simple assertionError
        assertEquals("must be removed item without NSEE", item, c.getRemoved().get(0));
    }
    /**
     * Trying to dig into unexpected failure of alsoSelect.
     * Plain model testing: here use selectPrevious
     */
    @Test
    public void testAnchorAlsoSelectPreviousSingleMode() {
        if (multipleMode) return;
        // general case: anchor kept in behavior for core
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectPrevious();
        assertEquals("anchor must be updated to previous in single mode", 
                index - 1, getAnchorIndex());
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
    }
    
    /**
     * Trying to dig into unexpected failure of alsoSelect.
     * Plain model testing: here use selectRange
     */
    @Test
    public void testAnchorAlsoSelectPreviousByRangeSingleMode() {
        if (multipleMode) return;
        // general case: anchor kept in behavior for core
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        int newFocus = getFocusIndex() - 1;
        getSelectionModel().selectRange(index, newFocus -1);
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be updated to previous in single mode", 
                newFocus, getAnchorIndex());
    }

    /**
     * Loads the view into a StageLoader to enforce skin creation.
     * asserts empty selection.
     */
    protected void initSkin() {
        loader = new StageLoader(getView());
        assertSelectionStateAfterSkin();
    }

    protected void assertSelectionStateAfterSkin() {
        assertEquals("sanity: initially unselected", -1, getSelectionModel().getSelectedIndex());
        // following fails as of 8u40b9 - why exactly?
        // focus forced into 0 - how to test cleanly? force back into -1?
//        assertEquals("sanity: initially unfocused", -1, getFocusIndex());
    }
    

    /**
     * Test how the anchor behaves when clearing individual selected items.
     * 
     * Here we select a range by repeated selectNext.
     */
    @Test
    public void testAnchorOnSelectRangeWithNext() {
        if (!multipleMode) return;
        initSkin();
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept on first of range", first, getAnchorIndex());
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     */
    @Test
    public void testAnchorOnClearSelectionInRangeWithNext() {
        if (!multipleMode) return;
        initSkin();
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection();
        assertEquals(0, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be cleared", -1, getAnchorIndex());
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     */
    @Test
    public void testAnchorOnClearSelectionAtInRangeWithNext() {
        if (!multipleMode) return;
        initSkin();
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept when clearing index in range", first, getAnchorIndex());
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     * 
     * Test what happens if selection at anchor is cleared. 
     */
    @Test
    public void testAnchorOnClearSelectionOfAnchorInRangeWithNext() {
        if (!multipleMode) return;
        initSkin();
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection(first);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be unchanged on clearing its selection", first, getAnchorIndex());
    }
    
    /**
     * Test how the anchor behaves when clearing individual selected items.
     * Here we select a range with the range method.
     * Fails because anchorOnSelectRangeAscending fails?
     */
    @Test
    public void testAnchorOnClearSelectionAtAfterRange() {
        if (!multipleMode) return;
        initSkin();
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept when clearing index in range", start, getAnchorIndex());
    }
    
    @Test
    public void testAnchorOnSelectRangeAscending() {
        if (!multipleMode) return;
        initSkin();
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept on first of range", start, getAnchorIndex());
    }
    
    @Test
    public void testAnchorOnSelectRangeDescending() {
        if (!multipleMode) return;
        initSkin();
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(start, getAnchorIndex());
    }
    
    /**
     * focus == anchor == 0
     */
    @Test
    public void testAlsoSelectNextSameAtFirst() {
        if (!multipleMode) return;
        initSkin();
        getSelectionModel().select(0);
        int anchor = getAnchorIndex();
        int oldFocus = getFocusIndex();
        int newFocus = oldFocus + 1;
        getSelectionModel().selectRange(anchor, newFocus + 1);
        assertEquals(newFocus - anchor + 1, getSelectionModel().getSelectedIndices().size());
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals(newFocus, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testAnchorOnClearSelectionAt() {
        initSkin();
        int index = 2;
        // initial
        getSelectionModel().select(0);
        // something else
        getSelectionModel().clearAndSelect(index);
        // clear at
        getSelectionModel().clearSelection(index);
        assertEquals("anchor must be same as focus", getFocusIndex(), getAnchorIndex());
        assertEquals("anchor must be cleared", -1, getAnchorIndex());
        // wrong assumption: contract changed to make behaviour consistent between
        // anchor and focus: if nothing else selected after clearAt, anchor is
        // cleared just the same as focus
//        assertEquals("anchor must be unchanged on clearAt", index, getAnchorIndex());
    }
    /**
     * focus == anchor == 0
     * PENDING JW: who's responsible for range checks?
     */
    @Test
    public void testAlsoSelectPreviousSameAtFirst() {
        if (!multipleMode) return;
        initSkin();
        getSelectionModel().select(0);
        int anchor = getAnchorIndex();
        int oldFocus = getFocusIndex();
        int newFocus = oldFocus - 1;
        getSelectionModel().selectRange(anchor, newFocus - 1);
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
        assertEquals(oldFocus, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * How to define the range at corner case: 
     * anchor == 1, selectRange up to include index 0
     */
    @Test
    public void testSelectRangeUpFromOne() {
        if (!multipleMode) return;
        initSkin();
        getSelectionModel().select(1);
        getSelectionModel().selectRange(1, -1);
        assertEquals("anchor unchanged", 1, getAnchorIndex());
        assertEquals("focus on 0", 0, getFocusIndex());
    }
    
    /**
     * How to define the range at corner case: 
     * anchor == 1, selectRange up to include index 0
     */
    @Test
    public void testSelectRangeDownFromSecondLast() {
        if (!multipleMode) return;
        initSkin();
        int last = items.size() - 1;
        getSelectionModel().select(last - 1);
        int anchor = last - 1;
        int oldFocus = anchor;
        int newFocus = oldFocus + 1;
        getSelectionModel().selectRange(anchor, newFocus + 1);
        assertEquals("anchor unchanged", anchor, getAnchorIndex());
        assertEquals("focus on last", newFocus, getFocusIndex());
    }
    
    /**
     * How to define the range at corner case: 
     * anchor == last, selectRange down -> nothing changed
     */
    @Test
    public void testSelectRangeDownFromLast() {
        if (!multipleMode) return;
        initSkin();
        int last = items.size() - 1;
        getSelectionModel().select(last);
        int anchor = last;
        int oldFocus = anchor;
        int newFocus = oldFocus + 1;
        getSelectionModel().selectRange(anchor, newFocus + 1);
        assertEquals("anchor unchanged", anchor, getAnchorIndex());
        assertEquals("focus unchanged", oldFocus, getFocusIndex());
    }

    /**
     * Descending == anchor > focus
     * Next == newFocus = focus + 1
     */
    @Test
    public void testAlsoSelectNextDescending() {
        if (!multipleMode) return;
        initSkin();
        prepareAlsoSelectDescending();
        
        int anchor = getAnchorIndex();
        int oldFocus = getFocusIndex();
        int newFocus = oldFocus + 1;
        getSelectionModel().clearSelection();
        // not included boundary is new - 1 for descending
        getSelectionModel().selectRange(anchor, newFocus - 1);
        assertEquals("focus must be last of range", newFocus, getFocusIndex());
        assertEquals("selected must be focus", newFocus, getSelectionModel().getSelectedIndex());
        assertEquals("anchor must be unchanged", anchor, getAnchorIndex());
        assertEquals("size must be old selection till focus", anchor - newFocus + 1, 
                getSelectionModel().getSelectedIndices().size());
        
    }
    
    /**
     * Descending == anchor > focus
     * Previous == newFocus = focus - 1
     */
    @Test
    public void testAlsoSelectPreviousDescending() {
        if (!multipleMode) return;
        initSkin();
        prepareAlsoSelectDescending();
        
        int anchor = getAnchorIndex();
        int oldFocus = getFocusIndex();
        int newFocus = oldFocus - 1;
        getSelectionModel().clearSelection();
        // not included boundary is new - 1 for descending
        getSelectionModel().selectRange(anchor, newFocus - 1);
        assertEquals("focus must be last of range", newFocus, getFocusIndex());
        assertEquals("selected must be focus", newFocus, getSelectionModel().getSelectedIndex());
        assertEquals("anchor must be unchanged", anchor, getAnchorIndex());
        assertEquals("size must be old selection till focus", anchor - newFocus + 1, 
                getSelectionModel().getSelectedIndices().size());
    }
    
    
    /**
     * Prepare model state for alsoSelectPrevious
     * 
     * select 6 .. 4
     * anchor = 6
     * move focus back twice, unselected -> focus == 2
     * 
     * anchor > focus
     */
    protected void prepareAlsoSelectDescending() {
        // initial state
        getSelectionModel().select(0);
        // select 6
        getSelectionModel().clearAndSelect(6);
        // extend selection up twice
        getSelectionModel().selectPrevious();
        getSelectionModel().selectPrevious();
        //move focus
        getFocusModel().focusPrevious();
        getFocusModel().focusPrevious();
        
        assertEquals(6, getAnchorIndex());
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(2, getFocusIndex());
        
    }

    /**
     * Simulate alsoSelectNext to dig down a bug: selectRange doesn't select
     * to new focus
     * 
     * Here: continous range selected via selectNext, then focus moved some rows below,
     * then select range to next after focus
     * 
     * Ascending == anchor < focus
     * Next == newFocus = focus + 1
     */
    @Test
    public void testAlsoSelectNextAscending() {
        if (!multipleMode) return;
        initSkin();
        prepareAlsoSelectAscending();
        
        int anchor = getAnchorIndex();
        int oldFocus = getFocusIndex();
        int newFocus = oldFocus + 1;
        getSelectionModel().clearSelection();
        // not included boundary is new + 1 for ascending
        getSelectionModel().selectRange(anchor, newFocus + 1);
        assertEquals("focus must be last of range", newFocus, getFocusIndex());
        assertEquals("selected must be focus", newFocus, getSelectionModel().getSelectedIndex());
        assertEquals("anchor must be unchanged", anchor, getAnchorIndex());
        assertEquals("size must be old selection till focus", newFocus - anchor + 1, 
                getSelectionModel().getSelectedIndices().size());
    }
    
    /**
     * Simulate alsoSelectNext to dig down a bug: selectRange doesn't select
     * to new focus
     * 
     * Here: continous range selected via selectNext, then focus moved some rows below,
     * then select range to previous before focus
     * 
     * Ascending == anchor < focus
     * Previous == newFocus = focus - 1
     */
    @Test
    public void testAlsoSelectPreviousAscending() {
        if (!multipleMode) return;
        initSkin();
        prepareAlsoSelectAscending();
        
        int anchor = getAnchorIndex();
        int oldFocus = getFocusIndex();
        int newFocus = oldFocus - 1;
        getSelectionModel().clearSelection();
        // not included boundary is new + 1 for ascending
        getSelectionModel().selectRange(anchor, newFocus + 1);
        assertEquals("focus must be last of range", newFocus, getFocusIndex());
        assertEquals("selected must be focus", newFocus, getSelectionModel().getSelectedIndex());
        assertEquals("anchor must be unchanged", anchor, getAnchorIndex());
        assertEquals("size must be old selection till focus", newFocus - anchor +1 , 
                getSelectionModel().getSelectedIndices().size());
    }

    /**
     * Prepare models to simulate problem in alsoSelectNext/Previous
     * index 1 to 3 selected, inclusive
     * anchor == 1
     * focus = 5 unselected
     * 
     * anchor < focus
     */
    protected void prepareAlsoSelectAscending() {
        // prepare: initial select 0
        getSelectionModel().select(0);
        // move selection to next
        getSelectionModel().clearAndSelect(1);
        // extend
        getSelectionModel().selectNext();
        getSelectionModel().selectNext();
        // move focus
        getFocusModel().focusNext();
        getFocusModel().focusNext();
        assertEquals(1, getAnchorIndex());
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(5, getFocusIndex());
    }
    
    
    /**
     * Test: extend selection - move focus - extend selection
     */
    @Test
    public void testFocusOnRangeAscendingMoveFocusSelectRange() {
        if (!multipleMode) return;
        initSkin();
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        assertEquals("sanity anchor", start, getAnchorIndex());
        getFocusModel().focusNext();
        
        assertEquals("sanity ..", end, getFocusIndex());
//        assertEquals("focus must be unchanged on clearSelection at focus", last, getFocusIndex());
    }
    
    @Test
    public void testFocusOnClearSelectionAtFocusRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        getSelectionModel().clearSelection(last);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be unchanged on clearSelection at focus", last, getFocusIndex());
        assertEquals("selectedIndex must be unchanged on clearAt", last, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testFocusOnClearSelectionAtRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be unchanged on clearSelection in range", last, getFocusIndex());
    }
    
    @Test
    public void testFocusOnClearSelectionRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        getSelectionModel().clearSelection();
        assertEquals(0, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be cleared", -1, getFocusIndex());
    }
    
    @Test
    public void testFocusOnSelectRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getFocusIndex());
    }
    
    @Test
    public void testFocusOnSelectRangeDescending() {
        if (!multipleMode) return;
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        int last = end + 1;
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getFocusIndex());
    }
    
    @Test
    public void testSelectedStartEndRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertTrue("start index must be selected" + start, getSelectionModel().isSelected(start));
        assertFalse("end index must not be selected" + end, getSelectionModel().isSelected(end));
    }
    
    @Test
    public void testSelectedStartEndRangeDescending() {
        if (!multipleMode) return;
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        assertTrue("start index must be selected" + start, getSelectionModel().isSelected(start));
        assertFalse("end index must not be selected" + end, getSelectionModel().isSelected(end));
    }
    
    @Test
    public void testSelectedIndicesAfterSort() {
        int first = 0;
        int last = items.size() -1;
        getSelectionModel().select(first);
        FXCollections.sort(items);
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getSelectionModel().getSelectedIndices().get(0));
    }


    @Test
    public void testSelectedIndexIsLastSelected() {
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
            assertEquals("selectedIndex is last selected", 
                    i, getSelectionModel().getSelectedIndex());
        }
    }

    /**
     * Test api doc: reselect an already selected index makes it
     * the selectedIndex.
     */
    @Test
    public void testIndicesSelectedIndexIsUpdatedAfterSelectAgain() {
        int[] indices = new int[] {2,3};
        int lastSelected = indices[0];
        // select all
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        // re-select first
        getSelectionModel().select(lastSelected);
        assertEquals(lastSelected, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Issue: selectedIndex not updated on unselect if there are
     * other indices selected. 
     * 
     * Might be intended behaviour, by the doc of selectedIndexProperty:
     * in multiple selectionMode
     * "the selected index will always represent the last selection made"
     * 
     * Leads to the unintuitive (I would say: illegal) 
     * state where the selectedIndex is unselected.
     * 
     * The (my) confusion might stem from selectionModel mixing "lead" and 
     * "selectedIndex" - a lead might be unselected. Problem is, that
     * unselecting the "selectedIndex" (aka: lead) doesn't send a 
     * notification to listeners, so they can't update their visuals.
     * 
     */
    @Test
    public void testIndicesSelectedIndexIsUpdatedAfterUnselect() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        int lastSelected = indices[indices.length - 1];
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        // clear the last selected
        getSelectionModel().clearSelection(lastSelected);
        int selectedIndex = getSelectionModel().getSelectedIndex();
        assertEquals("selected index must be .. selected", selectedIndex >= 0, 
                getSelectionModel().isSelected(selectedIndex));
        // JW: what exactly happens is unspecified, my expectation would be to
        // move it to one of the still selected 
        assertEquals("selected index must be updated to another selected", 
                indices[0], selectedIndex);
    }

    

    /**
     * Sanity test: select several indices, unselect them one-by-one -
     * selection must be empty and unselected removed from selected
     * indices
     */
    @Test
    public void testIndicesUnselectAll() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        int size = indices.length;
        for (int index : indices) {
            getSelectionModel().select(index);
        }
        assertEquals(size, getSelectionModel().getSelectedIndices().size());
        for (int index : indices) {
            getSelectionModel().clearSelection(index);
            assertFalse("cleared index must be unselected", getSelectionModel().isSelected(index));
            assertFalse("cleared index must not be contained in indices", 
                    getSelectionModel().getSelectedIndices().contains(index));
            assertEquals("size of indices must be decreased by one", 
                    --size, getSelectionModel().getSelectedIndices().size());
        }
        assertTrue(getSelectionModel().isEmpty());
    }
    
    @Test
    public void testSelectedIndices() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        assertEquals("sanity: same size", indices.length, 
                getSelectionModel().getSelectedIndices().size());;
        for (int i : indices) {
            assertTrue("index must be selected", 
                    getSelectionModel().isSelected(i));
            assertTrue("index must be contained in selectedIndices", 
                    getSelectionModel().getSelectedIndices().contains(i));
        }
        
    }
    
        
    /** 
     * Test select(-1) must do nothing - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    @ConditionalIgnore(condition = IgnoreDocErrors.class)
    public void testSelectMinusOneIndex() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(-1);
        assertEquals(indices[1], getSelectionModel().getSelectedIndex());
    }
    
    /** 
     * Test select(-1) must do nothing - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test 
    @ConditionalIgnore(condition = IgnoreDocErrors.class)
    public void testSelectMinusOneItem() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(-1);
        assertEquals(items.get(indices[1]), getSelectionModel().getSelectedItem());
    }
    

    /** 
     * Test select(null) is unspecified - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    @ConditionalIgnore(condition = IgnoreDocErrors.class)
    public void testSelectNullIndex() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(null);
        assertEquals(indices[1], getSelectionModel().getSelectedIndex());
    }
    
    /** 
     * Test select(null) is unspecified - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    @ConditionalIgnore(condition = IgnoreDocErrors.class)
    public void testSelectNullItem() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        Object item = getSelectionModel().getSelectedItem();
        getSelectionModel().select(null);
        assertEquals(item, getSelectionModel().getSelectedItem());
    }

     public MultipleSelectionIssues(boolean multiple) {
        this.multipleMode = multiple;
    }

    /**
     * PENDING: why not parameterize directly on the mode?
     * @return
     */
    @Parameterized.Parameters
    public static Collection selectionModes() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    protected void checkMode(T model) {
       if (multipleMode && model.getSelectionMode() != SelectionMode.MULTIPLE) {
           model.setSelectionMode(SelectionMode.MULTIPLE);
       }
    }

    @Before
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        view = createView(items);
    }
    
    protected abstract V createView(ObservableList items);
    
    protected abstract T getSelectionModel();

    protected V getView() {
        return view;
    }

    /**
     * Returns the index of the anchor value. Note that subclasses which store a
     * compound value need to override and extract the index.
     * 
     * @return
     */
    protected int getAnchorIndex() {
        Object anchor = getView().getProperties().get(SelectionIssues.ANCHOR_KEY);
        return anchor != null ? (int) anchor : -1;
    }


    /**
     * We expect views with MultipleSelectionModel to have a FocusModel as well.
     * 
     * @param index the default value for views that don't have a focusModel
     * @return 
     */
    protected int getFocusIndex() {
        return getFocusModel().getFocusedIndex();
    }
    
    protected abstract FocusModel getFocusModel();
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MultipleSelectionIssues.class.getName());
}
