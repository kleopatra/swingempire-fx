/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import static org.junit.Assert.*;
import static org.junit.Assert.*;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.property.PropertyIgnores;
import de.swingempire.fx.property.PropertyIgnores.IgnoreReported;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreCorrelated;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreDocErrors;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreFocus;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.ListChangeReport;
import de.swingempire.fx.util.StageLoader;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;
/**
 * Tests behaviour of MultipleSelection api.
 * 
 * Note: as of 8u40b9, table/list autofocus to 0 for not-empty items. This
 * test reverts to -1!
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
     * 
     * PENDING JW: tree tests - this contains the originally
     * created treeItems, modifications happens on the root's
     * children, though! That is, after modifications on the treeItem
     * this is out of sync? Maybe instantiate with the real children?
     * Then need to be sure to _never_ modify directly!
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
    @Test
    public void testSelectedIndicesSize() {
        if (!multipleMode) return;
        int first = 7;
        int[] indices = new int[] {1, 8, 3};
        getSelectionModel().selectIndices(first, indices);
        ObservableList<Integer> selectedIndices = getSelectionModel().getSelectedIndices();
        assertEquals("all indices selected", indices.length + 1, selectedIndices.size());
        assertTrue("must be contained: " + first, selectedIndices.contains(first));
        for (int i = 0; i < indices.length; i++) {
            assertTrue("must be contained: " + indices[i], selectedIndices.contains(indices[i]));
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
    
    @Test
    public void testSelectedIndicesDuplicate() {
        if (!multipleMode) return;
        int first = 7;
        int[] indices = new int[] {3, 8, 3};
        // here we select 3 unique indices (the 4th is a duplicate)
        getSelectionModel().selectIndices(first, indices);
        assertEquals("selected indices must not have any duplicates", 
                indices.length, getSelectionModel().getSelectedIndices().size());
        assertEquals(indices[indices.length-1] , getSelectionModel().getSelectedIndex());
    }
  
    @Test
    public void testSelectedIndicesOffRange() {
        if (!multipleMode) return;
        int first = 7;
        int[] indices = new int[] {3, 8, 100};
        getSelectionModel().selectIndices(first, indices);
        assertEquals(indices.length, getSelectionModel().getSelectedIndices().size());
        assertEquals(8 , getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSelectedIndicesOffNegative() {
        if (!multipleMode) return;
        int first = 7;
        int[] indices = new int[] {3, 8, -100};
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        getSelectionModel().selectIndices(first, indices);
        assertEquals(indices.length, getSelectionModel().getSelectedIndices().size());
    }
    /**
     * Test selection state and notification of selectedIndices on continous remove items
     * https://javafx-jira.kenai.com/browse/RT-39636
     * 
     * sanity testing: all fine for a single remove
     */
    @Test
    public void testSelectedIndicesOnContinuousRemovedItems() {
//        if (!multipleMode) return;
        int last = items.size() - 1;
        getSelectionModel().select(last);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        removeAllItems(items.get(2), items.get(3));
        int expected = last - 2;
        assertEquals("sanity: items size after remove", expected + 1, items.size());
        assertEquals("selectedIndex", expected, getSelectionModel().getSelectedIndex());
        assertEquals("selected in indices after removing", expected, getSelectionModel().getSelectedIndices().get(0));
        assertEquals("single event on discontinousremove " + report.getLastChange(), 1, report.getEventCount());
    }
    
    /**
     * Test selection state and notification of selectedIndices for discontinous remove items
     * https://javafx-jira.kenai.com/browse/RT-39636
     * Reported for tableView, not for others.
     */
    @Test
//    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedItemsOnDiscontinousRemovedItems() {
//        if (!multipleMode) return;
        int last = items.size() - 1;
        getSelectionModel().select(last);
        Object selectedItem = getSelectionModel().getSelectedItem();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        removeAllItems(items.get(2), items.get(5));
        int expected = last - 2;
        assertEquals("sanity: items size after remove", expected + 1, items.size());
        assertEquals("selectedItem", selectedItem, getSelectionModel().getSelectedItem());
        assertEquals("selected in items after removing", selectedItem, getSelectionModel().getSelectedItems().get(0));
        assertEquals("no event of selectedItems on discontinousremove " + report.getLastChange(), 0, report.getEventCount());
    }
    
    /**
     * Test selection state and notification of selectedIndices for discontinous remove items
     * https://javafx-jira.kenai.com/browse/RT-39636
     * Reported for tableView, not for others.
     */
    @Test
//    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedIndicesOnDiscontinousRemovedItems() {
//        if (!multipleMode) return;
        int last = items.size() - 1;
        getSelectionModel().select(last);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        removeAllItems(items.get(2), items.get(5));
        int expected = last - 2;
        assertEquals("sanity: items size after remove", expected + 1, items.size());
        assertEquals("selectedIndex", expected, getSelectionModel().getSelectedIndex());
        assertEquals("selected in indices after removing", expected, getSelectionModel().getSelectedIndices().get(0));
        assertEquals("single event on discontinousremove " + report.getLastChange(), 1, report.getEventCount());
    }
    
    
    /**
     * Why do we get a permutated? How are we supposed to use it?
     * don't expect a permutation change, incorrect in core!
     */
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedIndicesEventsOnAddedItem() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        ObservableList<Integer> indices = getSelectionModel().getSelectedIndices();
        ObservableList<Integer> copy = FXCollections.observableArrayList(indices);
        ListChangeReport report = new ListChangeReport(indices);
        items.add(0, "newItem");
        Change c = report.getLastChange();
//        prettyPrint(c);
        c.reset();
        c.next();
        if (!c.wasPermutated()) {
            LOG.info("no permutation as expected:" + c);
            return;
        }
        for (int i = c.getFrom(); i < c.getTo(); i++) {
            int newIndex = c.getPermutation(i);
            assertEquals("item at oldIndex " + i, copy.get(i), indices.get(newIndex));
        }
    }
    
    /**
     * Hmm ... 
     * The assumption might be not quite correct? The items are unchanged,
     * but their position in the list is changed ... Think if that might count as
     * a change
     */
    @Test
    public void testSelectedItemsEventsOnAddedItem() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        addItem(0, createItem("newItem"));
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
    public void testEventsMultipleReselectSingleSelectedItem() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
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
    public void testEventsMultipleReselectSingleSelectedIndex() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ChangeReport report = new ChangeReport(getSelectionModel().selectedIndexProperty());
        getSelectionModel().select(selected);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    /**
     * Issue: selectionModel must not fire on reselect selected index
     * 
     * Here: test selectedIndex, clearAndSelect with index already selected.
     * This is the doc'ed context in selectionModel: listeners to 
     * selectedIndex must not receive an intermediate -1
     * 
     * spurned by:
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     */
    @Test
    public void testEventsMultipleToSingleClearAndSelectSelectedIndex() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ChangeReport report = new ChangeReport(getSelectionModel().selectedIndexProperty());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    @Test
    public void testEventsMultipleToSingleClearAndSelectSelectedItem() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
        getSelectionModel().clearAndSelect(selected);
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
    public void testEventsMultipleReselectSingleSelectedIndices() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        getSelectionModel().select(selected);
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
    public void testEventsMultipleReselectSingleSelectedItems() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        getSelectionModel().select(selected);
        assertEquals("no event on adding already selected", 0, report.getEventCount());
    }
    
    /**
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     * 
     * This is the test for 37360: clearAndSelect an already selected
     */
    @Test
    public void testEventsMultipleToSingleClearAndSelectSelectedItems() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        // incorrect assumption: we had 2 selected, with clearAndSelect the last
        // we should get a single removed
//        report.prettyPrint();
        assertTrue("must be single removed but was " + c, wasSingleRemoved(c));
    }
    
    /**
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     * 
     * PENDING JW: still issues
     * - core: incorrect change event
     * - simple: clearAndSelect not yet implemented (to not fire if selected)
     * 
     * This is the test for 37360: clearAndSelect an already selected
     */
    @Test
    public void testEventsMultipleToSingleClearAndSelectSelectedIndices() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        // hadbeen wasSingleReplaced - 
        // that's an incorrect assumption: we had 2 selected, with clearAndSelect the last
        // we should get a single removed
        // fails in core due an invalid change event
        // fails in simple due to clearAndSelect not yet really implemented
        assertTrue("must be single removed but was " + c, wasSingleRemoved(c));
    }
    
    /**
     * ClearAndSelect fires invalid event if selectedIndex is unchanged.
     * Reported https://javafx-jira.kenai.com/browse/RT-40212
     * 
     * closed as fixed afte u60b5
     */
    @Test
    @ConditionalIgnore(condition=PropertyIgnores.IgnoreReported.class)
    public void testChangeEventSelectedItemsOnClearAndSelect() {
        if (!multipleMode) return;
        // init with multiple selection
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("sanity: selectedIndex unchanged", selected, getSelectionModel().getSelectedIndex());
        assertEquals("single event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        while(c.next()) {
            boolean type = c.wasAdded() || c.wasRemoved() || c.wasPermutated() || c.wasUpdated();
            assertTrue("at least one of the change types must be true", type);
        }
    }
    
    /**
     * ClearAndSelect fires invalid change event if selectedIndex is unchanged.
     * REported: https://javafx-jira.kenai.com/browse/RT-40212
     * closed as fixed afte u60b5
     */
    @Test
    @ConditionalIgnore(condition=PropertyIgnores.IgnoreReported.class)
    public void testChangeEventSelectedIndicesOnClearAndSelect() {
        if (!multipleMode) return;
        // init with multiple selection
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectionModel().getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("sanity: selectedIndex unchanged", selected, getSelectionModel().getSelectedIndex());
        assertEquals("single event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        while(c.next()) {
            boolean type = c.wasAdded() || c.wasRemoved() || c.wasPermutated() || c.wasUpdated();
            assertTrue("at least one of the change types must be true", type);
        }
    }
    
//-------------------- items modification    
    
    /**
     * Bunch of tests that verify no change of uncontained selectedItem if it isn't
     * "inserted" by the modification.
     */
    @Test
    public void testUncontainedOnClearSingle() {
        Object uncontained = createItem("permanently-uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        setAllItems();
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnClearMultiple() {
        Object uncontained = createItem("permanently-uncontained");
        getSelectionModel().selectRange(2, 6);
        // prepare state
        getSelectionModel().select(uncontained);
        setAllItems();
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnSetAllSingle() {
        Object uncontained = createItem("permanently-uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        setAllItems(createItem("newItem"), createItem("other"));
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnSetAllMultiple() {
        Object uncontained = createItem("permanently-uncontained");
        getSelectionModel().selectRange(2, 6);
        // prepare state
        getSelectionModel().select(uncontained);
        setAllItems(createItem("newItem"), createItem("other"));
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnSetItemSingle() {
        Object uncontained = createItem("permanently-uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        setItem(0, createItem("newItem"));
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnSetItemMultiple() {
        Object uncontained = createItem("permanently-uncontained");
        getSelectionModel().selectRange(2, 6);
        // prepare state
        getSelectionModel().select(uncontained);
        setItem(4, createItem("newItem"));
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnRemoveItemSingle() {
        Object uncontained = createItem("permanently-uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        removeItem(0);
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
    }
    
    @Test
    public void testUncontainedOnRemoveItemMultiple() {
        Object uncontained = createItem("permanently-uncontained");
        getSelectionModel().selectRange(2, 6);
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: ", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: ", -1, getSelectionModel().getSelectedIndex());
        removeItem(4);
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testUncontainedOnInsertItemSingle() {
        Object uncontained = createItem("permanently-uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        addItem(4, createItem("newItem"));
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
    }
    
    @Test
    public void testUncontainedOnInsertItemMultiple() {
        Object uncontained = createItem("permanently-uncontained");
        getSelectionModel().selectRange(2, 6);
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: ", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: ", -1, getSelectionModel().getSelectedIndex());
        addItem(4, createItem("newItem"));
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }

    /**
     * Multiple selection,
     * listen to selectedIndexProperty and access selectedItem.
     * 
     * @see #testSelectedItemUncontainedNotificationSingle
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncItemToIndexMultiple() {
        Object uncontained = "uncontained";
        // prepare state, single Multiple
        int start = 3;
        int end = 6;
        getSelectionModel().selectRange(start, end);
        ChangeListener l = (p, old, value) -> assertEquals(uncontained, getSelectionModel().getSelectedItem());
        getSelectionModel().selectedIndexProperty().addListener(l);
        getSelectionModel().select(uncontained);
    }

    /**
     * Single selection,
     * listen to selectedIndexProperty and access selectedItem.
     * @see #testSelectedItemUncontainedNotificationSingle
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncItemToIndexSingle() {
        Object uncontained = "uncontained";
        // prepare state, single select
        int start = 3;
        getSelectionModel().select(start);
        ChangeListener l = (p, old, value) -> assertEquals(uncontained, 
                getSelectionModel().getSelectedItem());
        getSelectionModel().selectedIndexProperty().addListener(l);
        getSelectionModel().select(uncontained);
    }
    
    /**
     * Single selection,
     * listen to selectedItemProperty and access selectedIndex. Passes because index
     * is updated before item (implementation detail, of course!)
     * @see #testSelectedItemUncontainedNotificationSingle()
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncIndexToItemSingle() {
        Object uncontained = "uncontained";
        // prepare state, single select
        int start = 3;
        getSelectionModel().select(start);
        ChangeListener l = (p, old, value) -> assertEquals(-1, getSelectionModel().getSelectedIndex());
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().select(uncontained);
    }
    
    /**
     * Principal problem with correlated properties: they are hard to sync.
     * While they should fire only after all internal state of the bean is
     * updated, they do fire individually. This implies that the other is
     * not yet update when the first fires.
     * <p>
     * 
     * Core tries to sync them via a InvalidationListener on selectedIndex:
     * the item is updated on a change to the index. This leads to two
     * misbehaviours:<p>
     * 
     * 2 events fired on selecting uncontained item: first is due to the internal
     * sync (InvalidationListener), second to real setting. 
     * Done in MultipleSelectionModelBase.
     * <p>
     * 
     * accessing the item during listening to index-changes returns intermediate
     * value (vs. the finally set)
     * 
     * Multiple selection,
     * listen to selectedItemProperty and access selectedItem.
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncItemNotificationSingle() {
        Object uncontained = "uncontained";
        // prepare state, single select
        int start = 3;
        getSelectionModel().select(start);
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
        getSelectionModel().select(uncontained);
        assertEquals("expected single event", 1, report.getEventCount());
    }

    /**
     * Stand-alone version for bug report
     * https://javafx-jira.kenai.com/browse/RT-39552
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncItemNotificationCount() {
        Object uncontained = "uncontained";
        // prepare state, single select
        int start = 3;
        getSelectionModel().select(start);
        List values = new ArrayList();
        ChangeListener l = (p, old, value) -> values.add(value);
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().select(uncontained);
        assertEquals("expected single event", 1, values.size());
        assertEquals("expected newvalue is uncontained", uncontained, values.get(0));
        
    }
    /**
     * @see #testSelectedItemUncontainedNotificationSingle
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncItemNotificationMultiple() {
        Object uncontained = "uncontained";
        // prepare state, select a range
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
        getSelectionModel().select(uncontained);
        assertEquals("expected single event", 1, report.getEventCount());
    }
    
    
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
        Object uncontained = createItem("inserted-formerly-uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items
        int insertIndex = 3;
        addItem(insertIndex, uncontained);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertTrue("selectedItem must be in selectedItems " + uncontained, getSelectionModel().getSelectedItems().contains(uncontained));
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel().getSelectedIndex());
        assertTrue("selectedIndex must be in selectedIndices " + insertIndex, getSelectionModel().getSelectedIndices().contains(insertIndex));    
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
     * In contrast to the xxSingle method, here we select a range: must work as 
     * expected for both modes.
     * 
     * @see #testSelectedOnInsertUncontainedSingle()
     */
    @Test
    public void testSelectedOnInsertUncontainedMultiple() {
//        if (!multipleMode)
//            return;
        Object uncontained = createItem("uncontained");
        // prepare state, select a range
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertEquals("sanity: selectedItem on last of range",
                items.get(end - 1), getSelectionModel().getSelectedItem());
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained,
                getSelectionModel().getSelectedItem());
        assertEquals("sanity: selected index removed ", -1, 
                getSelectionModel().getSelectedIndex());
        // make uncontained part of the items
        int insertIndex = 3;
        addItem(insertIndex, uncontained);
        assertEquals("selectedItem unchanged", uncontained,
                getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel()
                .getSelectedIndex());
        assertTrue("selectedItem must be in selectedItems " + uncontained, 
                getSelectionModel().getSelectedItems().contains(uncontained));
        assertTrue("selectedIndex must be in selectedIndices " + insertIndex, 
                getSelectionModel().getSelectedIndices().contains(insertIndex));    
    }

    /**
     * Is an uncontained selectedItem in selectedItems? 
     * Looks like no: should update doc to clarify that selectedItems are those that are
     * backed by the model.
     */
    @Test
    public void testSelectedItemUncontainedInSelectedItemsSingle() {
        Object uncontained = createItem("uncontained");
        getSelectionModel().select(uncontained);
        assertFalse("uncontained selectedItem part of selectedItems?", 
                getSelectionModel().getSelectedItems().contains(uncontained));
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
        Object uncontained = createItem("uncontained");
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
        Object uncontained = createItem("uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, uncontained);
        setItems(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * What happens if uncontained not in new list? 
     * selectedIndex -1, uncontained still selectedItem
     */
    @Test
    public void testSelectedOnSetItemsWithoutUncontained() {
        Object uncontained = createItem("uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, createItem("anything"));
        setItems(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex unchanged", -1, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Specification ambiguity: clear out all selection state an replacing
     * all items or not?
     * Maybe not: see https://javafx-jira.kenai.com/browse/RT-35039
     * 
     */
    @Test
    public void testSelectedOnSetItemsOldContained() {
        getSelectionModel().select(1);
        Object selectedItem = getSelectionModel().getSelectedItem();
        ObservableList other = FXCollections.observableArrayList(createItem("some"), createItem("other"), selectedItem);
        setItems(other);
        assertEquals("setItems: new list contains old selectedItem", selectedItem, getSelectionModel().getSelectedItem());
        fail("spec ambiguity: replace all items and old selectedItem still contained - keep or not");
    }
    
    /**
     * SetAll must clear out selection state: contained or not must not make a difference.
     * Maybe not: see https://javafx-jira.kenai.com/browse/RT-35039
     */
    @Test
    public void testSelectedOnSetAllOldContained() {
        int index = 2;
        getSelectionModel().select(index);
        Object selectedItem = getSelectionModel().getSelectedItem();
        setAllItems(createItem("one"), createItem("two"), createItem("three"), selectedItem);
        assertEquals("setItems: new list contains old selectedItem", selectedItem, getSelectionModel().getSelectedItem());
//        assertEmptySelection();
        fail("spec ambiguity: replace all items and old selectedItem still contained - keep or not");
    }
    
    /**
     * SetAll must clear out selection state.
     */
    @Test
    public void testSelectedOnSetAll() {
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        setAllItems(createItem("one"), createItem("two"), createItem("three"), modifyItem(selected,"XX"));
        assertEmptySelection();
    }
    
    /**
     * SetAll must clear out selection state.
     */
    @Test
    public void testSelectedOnSetItems() {
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        setItems(FXCollections.observableArrayList(
                createItem("one"), createItem("two"), createItem("three"), modifyItem(selected,"XX")));
        assertEmptySelection();
    }
    
    private void assertEmptySelection() {
        assertEquals("selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItem must be null", null, getSelectionModel().getSelectedItem());
        assertEquals("selectedItems must be empty", 0, getSelectionModel().getSelectedItems().size());
        assertEquals("selectedIndices must be empty", 0, getSelectionModel().getSelectedIndices().size());
    }

    // ------------ focus on modifying list
    
    @Test
    public void testFocusMultipleRemoves() {
        int focus = 5;
        getFocusModel().focus(focus);
        // remove one above, one below
        removeAllItems(items.get(3), items.get(6));
        assertEquals("focus must be decreased by one", focus -1, getFocusIndex());
    }

    /**
     * Test having focusedIndex != selectedIndex (?)
     */
    @Test
    public void testFocusNotSelectedIndexOnInsertAbove() {
        int select = 5;
        int focus = 3;
        getSelectionModel().select(select);
        getFocusModel().focus(focus);
//        LOG.info("models: " + getSelectionModel().getClass() + getFocusModel().getClass());
        assertEquals("sanity selectedIndex", select, getSelectionModel().getSelectedIndex());
        assertEquals("sanity focusedIndex ", focus, getFocusModel().getFocusedIndex());
        addItem(0, createItem("new item"));
        assertEquals("selected increased by one", select + 1, getSelectionModel().getSelectedIndex());
        assertEquals("focused increased by one (selected = " + getSelectionModel().getSelectedIndex() + ")", 
                focus + 1, getFocusModel().getFocusedIndex());
    } 
    
    @Test
    public void testFocusUnselectedUpdateOnInsertAbove() {
        int index = 2;
        getFocusModel().focus(index);
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity: focus taken", index, getFocusModel().getFocusedIndex());
        addItem(0, createItem("new item"));
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals(index + 1, getFocusModel().getFocusedIndex());
    }
    
    @Test
    public void testFocusUnselectedUpdateOnRemoveAbove() {
        int index = 2;
        getFocusModel().focus(index);
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity: focus taken", index, getFocusModel().getFocusedIndex());
        removeItem(0);
        assertEquals("sanity: selected index not affected by focus", -1, getSelectionModel().getSelectedIndex());
        assertEquals(index - 1, getFocusModel().getFocusedIndex());
    }
    
    /**
     * Navigation disabled if first is selected/focused and removed
     * https://javafx-jira.kenai.com/browse/RT-38785
     * 
     * (fixed for TableView, not for ListView 8u40b12/b15)
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
     * 
     * This is basically 30931 (or so, the one that should define what happens if
     * the selectedIndex is removed). Most implementations move the index to
     * an adjacent index, so this test my fail for custom implemenations.
     * Unexpected failure for SimpleListSelectionModel.
     * 
     * Not quite: whatever the strategy, _if_ selectedIndex > -1 it must be 
     * part of selectedIndices and selectedItems must be updated along with it.
     */
    @Test
    public void testRemoveSelectedItemThatIsFirst_28637() {
        getSelectionModel().select(0);
        Object selectedItem = getSelectionModel().getSelectedItem();
        items.remove(selectedItem);
        int selected = getSelectionModel().getSelectedIndex();
        assertRemoveItemAtSelected_28637(selected);
    }
    
    protected void assertRemoveItemAtSelected_28637(int selected) {
        if (selected >= 0) {
            Object itemAfterRemove = getSelectionModel().getSelectedItem();
            assertEquals("selectedIndices must not be empty if selected index = " + selected, 
                    1, getSelectionModel().getSelectedIndices().size());
            assertEquals("index contained for " + selected, 
                    selected, getSelectionModel().getSelectedIndices().get(0));
            assertEquals("selectedItems must not be empty if selected index = " + selected, 
                    1, getSelectionModel().getSelectedItems().size());
            assertEquals("item contained for " + selected,
                    itemAfterRemove, 
                    getSelectionModel().getSelectedItems().get(0)); 
        } else {
            fail("selectedIndex must not be -1 was: " + selected);
        }
    }
    
    @Test
    public void testRemoveSelectedItemThatIsLast_28637() {
        getSelectionModel().select(items.size() - 1);
        Object selectedItem = getSelectionModel().getSelectedItem();
        items.remove(selectedItem);
        int selected = getSelectionModel().getSelectedIndex();
        assertRemoveItemAtSelected_28637(selected);
    }
    
    @Test
    public void testRemoveSelectedItemThatIsMiddle_28637() {
        getSelectionModel().select(3);
        Object selectedItem = getSelectionModel().getSelectedItem();
        items.remove(selectedItem);
        int selected = getSelectionModel().getSelectedIndex();
        assertRemoveItemAtSelected_28637(selected);
    }

    /**
     * Yet another test on clearing the items: test selection state for single
     * selected item
     */
    @Test
    public void testSelectedOnClearItemsSingle() {
        int index = 2;
        getSelectionModel().select(index);
        clearItems();
        assertTrue("selection must be empty", getSelectionModel().isEmpty());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertTrue("", getSelectionModel().getSelectedIndices().isEmpty());
        assertTrue("", getSelectionModel().getSelectedItems().isEmpty());
        assertEquals("focus must be cleared", -1, getFocusModel().getFocusedIndex());
    }
    
    /**
     * Yet another test on clearing the items: test selection state for multiple
     * selections
     */
    @Test
    public void testSelectedOnClearItemsRange() {
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        clearItems();
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItems must be empty", 0, getSelectionModel().getSelectedItems().size());
        assertTrue("", getSelectionModel().getSelectedIndices().isEmpty());
        assertTrue("selection must be empty", getSelectionModel().isEmpty());
        assertEquals("focus must be cleared", -1, getFocusModel().getFocusedIndex());
    }
    
    /**
     * Test that uncontained selectedItem isn't changed when clearing 
     * out items.
     */
    @Test
    public void testSelectedUncontainedOnClearItems() {
        Object uncontained = createItem("uncontained");
        getSelectionModel().selectRange(2, 6);
        getSelectionModel().select(uncontained);
        clearItems();
        assertTrue("selection must be empty", getSelectionModel().isEmpty());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
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
     * 
     * PENDING JW:
     * In AbstractSelectionModelBase: no event fired on clear - why not?
     */
    @Test
    public void testNoSuchElementOnClear_38884() {
        getSelectionModel().select(0);
        Object item = getSelectionModel().getSelectedItem();
        assertEquals("sanity: selectedItem is item at 0", items.get(0), item);
        assertTrue("sanity: selectedItem contained in selectedItems", 
                getSelectionModel().getSelectedItems().contains(item));
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        int size = items.size();
//        items.clear();
        clearItems();
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
        // PENDING JW: wrong assumption - size of selectedItems has been 1
        assertEquals("removed size", 1, c.getRemovedSize());
        // this is throwing before fix of 38884 with an NoSuchElementException 
        // actually, it's not throwing an NSEE, but an IOOBE
        // NSEE only if iterating over the list as done f.i. when printing
        // isn't here, get simple assertionError
        assertEquals("must be removed item without NSEE", item, c.getRemoved().get(0));
    }
    
    /**
     * Can't really test before the fix going public - but:
     * now selectedItems are firing too many removes. 
     * 
     * Setup:
     * - select a single item such that selectedItems.size() = 1
     * - clear list
     * - expected: removedSize in selectedItems' change is 1
     * - actual: removedSize in selectedItems' change is size of items
     */
    @Test
    public void testNoSuchElementOnClear_38884Unfixed() {
        getSelectionModel().select(0);
        Object item = getSelectionModel().getSelectedItem();
        int selectedItemsSize = getSelectionModel().getSelectedItems().size();
        assertEquals("sanity: selectedItem is item at 0", items.get(0), item);
        assertTrue("sanity: selectedItem contained in selectedItems", 
                getSelectionModel().getSelectedItems().contains(item));
        assertEquals("sanity: selected items size", 1, selectedItemsSize);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        clearItems();
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
        // PENDING JW: wrong assumption - size of selectedItems has been 1
        assertEquals("removed size", selectedItemsSize, c.getRemovedSize());
        // this is throwing before fix of 38884 with an NoSuchElementException 
        // actually, it's not throwing an NSEE, but an IOOBE
        // NSEE only if iterating over the list as done f.i. when printing
        // isn't here, get simple assertionError
        assertEquals("must be removed item without NSEE", item, c.getRemoved().get(0));
    }
    
    /**
     * Stand-alone for report
     * Change contains all items, should contain only formerly selected items.
     * https://javafx-jira.kenai.com/browse/RT-39553
     */
    @Test
    public void testSelectedItemsInvalidChange_38884() {
        getSelectionModel().select(3);
        int removedSize = getSelectionModel().getSelectedItems().size();
        ListChangeListener l = (Change c) -> {
            c.next();
            assertEquals(removedSize, c.getRemovedSize());
        };
        getSelectionModel().getSelectedItems().addListener(l);
        items.clear();
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
        assertEquals("sanity: anchor on first", first, getAnchorIndex());
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
    @ConditionalIgnore(condition = IgnoreFocus.class)
    public void testFocusOnRangeAscendingMoveFocusSelectRange() {
        if (!multipleMode) return;
        initSkin();
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
//        assertEquals("sanity anchor", start, getAnchorIndex());
        getFocusModel().focusNext();
        
        assertEquals("sanity ..", end, getFocusIndex());
//        assertEquals("focus must be unchanged on clearSelection at focus", last, getFocusIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreFocus.class)
    public void testFocusOnClearSelectionAtFocusRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        assertEquals("sanity: focus after selectRange", last, getFocusIndex());
        getSelectionModel().clearSelection(last);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be unchanged on clearSelection at focus", last, getFocusIndex());
        assertEquals("selectedIndex must be unchanged on clearAt", last, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreFocus.class)
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
    @ConditionalIgnore(condition = IgnoreFocus.class)
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
    @ConditionalIgnore(condition = IgnoreFocus.class)
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
    @ConditionalIgnore(condition = IgnoreFocus.class)
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
    @ConditionalIgnore(condition = IgnoreDocErrors.class)
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
        assertEquals("selected index must be .. selected " + selectedIndex, selectedIndex >= 0, 
                getSelectionModel().isSelected(selectedIndex));
        // JW: what exactly happens is unspecified, my expectation would be to
        // move it to one of the still selected 
        assertEquals("selected index must be updated to another selected", 
                indices[0], selectedIndex);
    }

    
    /**
     * Same as above, only the other way round: check that selectedIndex/item is 
     * consistently unchanged on clearing the index.
     * 
     * @see #testIndicesSelectedIndexIsUpdatedAfterUnselect()
     */
    @Test
    @ConditionalIgnore(condition = IgnoreDocErrors.class)
    public void testClearSelectionAtSelectedItem() {
        int start = 2;
        int end = 6;
        getSelectionModel().selectRange(start, end);
        int index = end - 1;
        Object selectedItem = items.get(index);
        int selectionSize = getSelectionModel().getSelectedIndices().size();
//        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index);
        assertEquals(index, getSelectionModel().getSelectedIndex());
        assertEquals(selectedItem, getSelectionModel().getSelectedItem());
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

//----------------------- test single selection enforce in singleMode    
    /**
     * 
     */
    @Test
    public void testSingleModeIndices() {
        if (multipleMode) return;
        int first = 2;
        int[] indices = new int[] {3, 7, 4};
        getSelectionModel().selectIndices(first, indices);
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
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

    @Test
    public void testClearSelectionAtInvalidIndex() {
        int start = 2;
        int end = 6;
        getSelectionModel().selectRange(start, end);
        int index = end - 1;
        int selectionSize = getSelectionModel().getSelectedIndices().size();
        getSelectionModel().clearSelection(items.size());
        assertTrue("index must still be selected " + index, getSelectionModel().isSelected(index));
        assertEquals("selectedIndex must be unchanged", 
                index, getSelectionModel().getSelectedIndex());
        assertEquals(selectionSize, getSelectionModel().getSelectedIndices().size());
        assertEquals(selectionSize, getSelectionModel().getSelectedItems().size());
    }
    
    @Test
    public void testClearSelectionAtUnselectedIndex() {
        int start = 2;
        int end = 6;
        getSelectionModel().selectRange(start, end);
        int index = end - 1;
        int selectionSize = getSelectionModel().getSelectedIndices().size();
        getSelectionModel().clearSelection(end);
        assertTrue("index must still be selected " + index, getSelectionModel().isSelected(index));
        assertEquals("selectedIndex must be unchanged", 
                index, getSelectionModel().getSelectedIndex());
        assertEquals(selectionSize, getSelectionModel().getSelectedIndices().size());
        assertEquals(selectionSize, getSelectionModel().getSelectedItems().size());
    }

    /**
     * Test notification of selectedItems on single select different index.
     */
    @Test
    public void testItemsNotificationOnSelect() {
        if (multipleMode) return;
        int old = 3;
        getSelectionModel().select(old);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedItems());
        int index = 4;
        getSelectionModel().select(index);
        assertEquals("one event on select", 1, report.getEventCount());
        assertTrue("expect single replace but was " + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
    }
    
    /**
     * Test notification of selectedIndices on single select different index.
     */
    @Test
    public void testIndicesNotificationOnSelect() {
        if (multipleMode) return;
        int old = 3;
        getSelectionModel().select(old);
        ListChangeReport report = new ListChangeReport(getSelectionModel().getSelectedIndices());
        int index = 4;
        getSelectionModel().select(index);
        assertEquals("one event on select", 1, report.getEventCount());
//        prettyPrint(report.getLastChange());
        assertEquals("expect single replace but was " + report.getLastChange(), 
                1,
                getChangeCount(report.getLastChange(), ChangeType.REPLACED));
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
        // PENDING JW: revert autoFocus!
        getFocusModel().focus(-1);
    }
    /**
     * Trying to open up the test for trees. Might work at least for very basic
     * state testing, but maybe not...
     * 
     * <p>
     * 
     * This implementation returns the given item.
     * 
     * @param item
     * @return
     */
    protected Object createItem(Object item) {
        return item;
    }
    
    protected Object modifyItem(Object old, String mod) {
        return old + mod;
    }

    /**
     * @param i
     */
    protected void removeItem(int i) {
        items.remove(i);
    }

    /**
     * @param object
     * @param object2
     */
    protected void removeAllItems(Object... object) {
        items.removeAll(object);
    }
    /**
     * Replaces all items via setAll. Here we assume that 
     * items is the backing list of a itemsProperty in the 
     * view, so simply doing a items.setAll(other)
     * subclasses may need to adjust as approperiate.
     * @param other
     */
    protected void setAllItems(ObservableList other) {
        items.setAll(other);
    };

    protected void setAllItems(Object... elements) {
        items.setAll(elements);
    }
    
    /**
     * Replace items list in view by other, if available. View
     * without setItems can delegate to setAllItems(list).
     * 
     * @param other a list containing elements acceptable to the view/s
     *   items property
     */
    protected void setItems(ObservableList other) {
        throw new UnsupportedOperationException("must be implemented by sub-classes - or ignore related tests");
    };
    
    protected void addItem(int pos, Object item) {
        items.add(pos, item);
    }

    protected void setItem(int pos, Object item) {
        items.set(pos, item);
    }

    protected void clearItems() {
        items.clear();
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
