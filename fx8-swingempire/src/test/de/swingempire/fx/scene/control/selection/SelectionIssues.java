/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SelectionModel;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreAnchor;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreCorrelated;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreDocErrors;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.StageLoader;

import static org.junit.Assert.*;

/**
 * Testing SelectionModel api.
 * 
 * Note: as of 8u40b9, table/list autofocus to 0 for not-empty items. This
 * test reverts to -1!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class SelectionIssues<V extends Control, T extends SelectionModel> {
    
    public final static String ANCHOR_KEY = "anchor";
  
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    

    /**
     * The model set to the views. It contains 5 string items, originally
     * in descending order. Invoking sort will revert the order.
     */
    protected ObservableList items;
    protected V view;

//--------------- changes in list
    
    /**
     * Testing notification on disjoint removes above selected. Here: item
     * 
     * Included in bug report: https://javafx-jira.kenai.com/browse/RT-40012
     */
    @Test
    public void testSelectedItemNotificationOnDisjointRemovesAbove() {
        int last = items.size() - 2;
        Object lastItem = items.get(last);
        getSelectionModel().select(last);
        assertEquals(lastItem, getSelectedItem());
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
        removeAll(0, 2);
//        items.removeAll(items.get(1), items.get(3));
        assertEquals("sanity: selectedItem unchanged", lastItem, getSelectedItem());
        assertEquals("must not fire on removes above", 0, report.getEventCount());
    }

    /**
     * Testing notification on disjoint removes above selected. Here: index
     * 
     * Note that we don't use the corner case of having the last index selected
     * (which fails already on updating the index)
     * 
     * Included in bug report: https://javafx-jira.kenai.com/browse/RT-40012
     */
    @Test
    public void testSelectedIndexNotificationOnDisjointRemovesAbove() {
        int last = items.size() - 2;
        getSelectionModel().select(last);
        assertEquals(last, getSelectedIndex());
        ChangeReport report = new ChangeReport(getSelectionModel().selectedIndexProperty());
        removeAll(0, 2);
//        items.removeAll(items.get(1), items.get(3));
        assertEquals("sanity: selectedIndex must be shifted by -2", last - 2, 
                getSelectedIndex());
        assertEquals("must fire single event on removes above", 1, 
                report.getEventCount());
    }
    /**
     * This fails because it it not updated at all, that is old selectedIndex ==
     * newSelectedIndex
     * 
     * not included in bug report
     */
    @Test
    public void testSelectedIndexAtLastNotificationOnDisjointRemovesAbove() {
        int last = items.size() - 1;
        getSelectionModel().select(last);
        assertEquals(last, getSelectedIndex());
        ChangeReport report = new ChangeReport(getSelectionModel().selectedIndexProperty());
        removeAll(0, 2);
//        assertEquals("sanity: selectedIndex must be shifted by -2", last - 2, 
//                getSelectionModel().getSelectedIndex());
        assertEquals("must fire single event on removes above", 1, 
                report.getEventCount());
    }
    
    /**
     * Testing notification on disjoint removes above selected. Here: index
     * Here test the (bad!) effect of incorrect handling of disjoint 
     * changes - index in change notification is illegal.
     * 
     * This fails to fail because there is no change fired on selectedIndex
     * - which in itself is the-correct-thingy-to-do, because it is
     * not updated (which is incorrect)
     * 
     */
    @Test
    public void testAccessSelectedIndexNotificationOnDisjointRemovesAbove() {
        int last = items.size() - 1;
        getSelectionModel().select(last);
        ChangeListener l = (o, old, value) -> {
            // hmm ... don't reach here? 
            // nothing fired, as the index isn't updated ...
            int newSelected = (int) value;
//            LOG.info("new value/size: " + newSelected + "/" + items.size());
            if (newSelected > -1) {
                assertTrue("index " + newSelected + "must be less than size " + items.size(),
                        newSelected < items.size());
            }
        } ;
        getSelectionModel().selectedIndexProperty().addListener(l);
        removeAll(0, 2);
    }
    

//-------------------------    
    /**
     * @see #testSelectedItemUncontainedNotificationSingle
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncItemToIndexSingle() {
        Object uncontained = "uncontained";
        // prepare state, single select
        int start = 3;
        getSelectionModel().select(start);
        ChangeListener l = (p, old, value) -> assertEquals(uncontained, getSelectedItem());
        getSelectionModel().selectedIndexProperty().addListener(l);
        getSelectionModel().select(uncontained);
    }
    
    /**
     * @see #testSelectedItemUncontainedNotificationSingle
     */
    @Test
    @ConditionalIgnore(condition = IgnoreCorrelated.class)
    public void testSyncIndexToItemSingle() {
        Object uncontained = "uncontained";
        // prepare state, single select
        int start = 3;
        getSelectionModel().select(start);
        ChangeListener l = (p, old, value) -> assertEquals(-1, getSelectedIndex());
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
     * 2 events fired on selecting uncontained item: one is due to the internal
     * sync (InvalidationListener), the other to real setting.
     * Done in MultipleSelectionModelBase.
     * <p>
     * 
     * accessing the item during listening to index changes returns intermediate
     * value (vs. the finally set)
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
    
//------------  test interplay of selection/focus/anchor 

    @Test
    public void testSelectedOnClearItems() {
        int index = 2;
        getSelectionModel().select(index);
//        items.clear();
        clearItems();
        assertEquals("selectedIndex must be cleared on removeAll", -1, getSelectedIndex());
        assertEquals("selectedItem must be cleared on removeAll", null, getSelectedItem());
        assertTrue("selection must be empty", getSelectionModel().isEmpty());
    }
    
    /**
     * Test that uncontained selectedItem isn't changed when clearing 
     * out items.
     */
    @Test
    public void testSelectedUncontainedOnClearItems() {
        Object uncontained = createItem("uncontained");
        getSelectionModel().select(1);
        getSelectionModel().select(uncontained);
        clearItems();
        assertTrue("selection must be empty", getSelectionModel().isEmpty());
        assertEquals(-1, getSelectedIndex());
        assertEquals(uncontained, getSelectedItem());
    }
    
    
    /**
     * https://javafx-jira.kenai.com/browse/RT-30931
     * What's happening if the item at the focused/selected index is 
     * removed?
     * 
     * The issue mentions 3 options
     * 1. "shift down both", that is the next item is focused and selected
     * 2. "shift down focus, unselect", that is selected item is cleared, focus one down
     * 3. "clear all", that is selected and focus cleared
     * 
     * UX preferred 1, here we seem to have a shift up?
     * The issue is still open.
     */
    @Test
    public void testFocusOnRemoveItemAtSelectedFocused() {
        int index = 2;
        getSelectionModel().select(index);
        items.remove(index);
        assertEquals("open 30931 - focus after remove focused", index, getFocusedIndex(index));
    }
    
    @Test
    public void testSelectedOnRemoveItemAtSelectedFocused() {
        int index = 2;
        getSelectionModel().select(index);
        items.remove(index);
        assertEquals("open 30931 - selection after remove focused", 
                index, getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreAnchor.class)
    public void testAnchorOnRemoveItemAtSelectedFocused() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.remove(index);
        assertEquals("open 30931 - anchor after remove focused", 
                index, getAnchorIndex(index));
    }
    
    /**
     * Testing effect of items modification.
     * 
     * Here: test selectedIndex/item on setItem(getSelectedIndex(), newItem); 
     */
    @Test
    public void testSelectedOnSetItemAtSelectedFocused() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = items.get(index);
        Object modified = modifyItem(selected, "xx");
//        items.set(index, modified);
        setItem(index, modified);
        assertEquals("selected index must be unchanged on setItem", 
                index, getSelectedIndex());
        assertEquals(modified, getSelectedItem());
    }
    
    /**
     * Testing effect of items modification.
     * 
     * Here: test focusedIndex on setItem(getSelectedIndex(), newItem); 
     */
    @Test
    public void testFocusOnSetItemAtSelectedFocused() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = items.get(index);
        assertEquals("sanity: focus at selected", index, getFocusedIndex(index));
        Object modified = selected + "xx";
        items.set(index, modified);
        assertEquals(index, getFocusedIndex(index));
    }
    
    /**
     * Testing effect of items modification.
     * 
     * Here: test anchor on setItem(getSelectedIndex(), newItem); 
     */
    @Test
    @ConditionalIgnore (condition = IgnoreAnchor.class)
    public void testAnchorOnSetItemAtSelectedFocused() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = items.get(index);
        assertEquals("sanity: anchor at selected", index, getAnchorIndex(index));
        Object modified = selected + "xx";
        items.set(index, modified);
        assertEquals(index, getAnchorIndex(index));
    }
    
    /**
     * Remove above: doesn't sound like RT-30931, no ambiguity if selected/focused
     * not involved.
     */
    @Test
    public void testFocusOnRemoveItemAbove() {
        int index = 2;
        getSelectionModel().select(index);
        assertEquals("sanity: focus set along with selected index", index, getFocusedIndex(index));
        removeItem(1);
        int expected = index -1;
        assertEquals("open 30931 - focus after remove above focused", expected, getFocusedIndex(expected));
    }
    
    /**
     * Corner case: last selected.
     */
    @Test
    public void testSelectedAtLastOnRemoveItemAbove() {
        int last = items.size() - 1;
        getSelectionModel().select(last);
        removeItem(1);
        int expected = last - 1;
        assertEquals("selected index after remove single item above", 
                expected, getSelectedIndex());
    }
    
    /**
     * Test selectedIndex on disjoint remove above.
     * Corner case: last selected. Fails for core
     */
    @Test
    public void testAccessSelectedAtLastOnDisjointRemoveItemsAbove() {
        int last = items.size() - 1;
        getSelectionModel().select(last);
        // disjoint remove of 2 elements above the last selected
        removeAll(1, 3);
//        items.removeAll(items.get(1), items.get(3));
        int selected = getSelectedIndex();
        if (selected > -1)
            items.get(selected);
    }
    
    /**
     * Test selectedIndex on disjoint remove above.
     * Corner case: last selected. Fails for core
     * Included in bug report: https://javafx-jira.kenai.com/browse/RT-40012
    */
    @Test
    public void testSelectedAtLastOnDisjointRemoveItemsAbove() {
        int last = items.size() - 1;
        getSelectionModel().select(last);
        // disjoint remove of 2 elements above the last selected
        removeAll(1, 3);
//        items.removeAll(items.get(1), items.get(3));
        int expected = last - 2;
        assertEquals("selected index after disjoint removes above", 
                expected, getSelectedIndex());
    }
    
    /**
     * Test selectedIndex on disjoint remove above.
     */
    @Test
    public void testSelectedOnDisjointRemoveItemsAbove() {
        int last = items.size() - 2;
        getSelectionModel().select(last);
        // disjoint remove of 2 elements above the last selected
        removeAll(1, 3);
//        items.removeAll(1, 3);
        int expected = last - 2;
        assertEquals("selected index on disjoint removes above", 
                expected, getSelectedIndex());
    }
    
    /**
     * Test selectedItem on disjoint remove above.
     * 
     * Passes because not using the last index, that is list access is
     * valid during internal update.
     */
    @Test
    public void testSelectedItemOnDisjointRemoveItemsAbove() {
        int last = items.size() - 2;
        Object lastItem = items.get(last);
        getSelectionModel().select(last);
        // disjoint remove of 2 elements above the last selected
        removeAll(1, 3);
        assertEquals("selected item unchanged on disjoint remove above", 
                lastItem, getSelectedItem());
    }
    
    /**
     * Test selectedItem on disjoint remove above.
     * corner case: last selected
     */
    @Test
    public void testSelectedItemAtLastOnDisjointRemoveItemsAbove() {
        int last = items.size() - 1;
        Object lastItem = items.get(last);
        getSelectionModel().select(last);
        // disjoint remove of 2 elements above the last selected
        removeAll(1, 3);
        assertEquals("selected item unchanged on disjoint remove above", 
                lastItem, getSelectedItem());
    }
    
    
    @Test
    public void testSelectedOnRemoveItemAbove() {
        int index = 2;
        getSelectionModel().select(index);
        removeItem(1);
        int expected = index - 1;
        assertEquals("open 30931 - selected after remove above selected", 
                expected, getSelectedIndex());
    }
    
    @Test
    public void testSelectedOnInsertItemAbove() {
        int index = 2;
        getSelectionModel().select(index);
//        items.add(0, "6-item");
        addItem(0, createItem("6-item"));
        int expected = index +1;
        assertEquals("selection moved by one after inserting item", 
                expected, getSelectedIndex());
    }
    
    @Test
    public void testFocusOnInsertItemAbove() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
//        items.add(0, "6-item");
        addItem(0, createItem("6-item"));
        int expected = index +1;
        assertEquals("selection moved by one after inserting item", 
                expected, getFocusedIndex(expected));
    }
    
    /**
     * Core installs listener twice of items reset, introduces problems with insert
     */
    @Test
    public void testFocusOnInsertItemAtSelected39042() {
        ObservableList other = FXCollections.observableArrayList(items.subList(0, 5));
        resetItems(other);
        int index = 2;
        getSelectionModel().select(index);
        other.add(index, "6-item");
        int expected = index +1;
        assertEquals("focused moved by one after inserting item", 
                expected, getFocusedIndex(expected));
    }
    
    /**
     * Core installs listener twice of items reset, introduces problems with insert
     */
    @Test
    public void testSelectedOnInsertItemAtSelected39042() {
        ObservableList other = FXCollections.observableArrayList(items.subList(0, 5));
        resetItems(other);
        int index = 2;
        getSelectionModel().select(index);
        other.add(index, "6-item");
        int expected = index +1;
        assertEquals("selected moved by one after inserting item", 
                expected, getSelectedIndex());
    }
    
    /**
     * Core installs listener twice of items reset, introduces problems with insert
     */
    @Test
    @ConditionalIgnore (condition = IgnoreAnchor.class)
    public void testAnchorOnInsertItemAtSelected39042() {
        initSkin();
        ObservableList other = FXCollections.observableArrayList(items.subList(0, 5));
        resetItems(other);
        int index = 2;
        getSelectionModel().select(index);
        other.add(index, "6-item");
        int expected = index +1;
        assertEquals("selected moved by one after inserting item", 
                expected, getAnchorIndex(expected));
    }
    
    /**
     * Undoc'ed but nearly always implemented:
     * Have a selectedItem that is not part of the underlying list (implies
     * selectedIndex < 0), then insert that item: selectedIndex must
     * then be updated to the index of the selectedItem in the list.
     * 
     */
    @Test
    public void testSelectedUncontainedAfterInsertUncontained() {
        Object uncontained = createItem("uncontained");
        getSelectionModel().select(uncontained);
        int insert = 3;
//        items.add(insert, uncontained);
        addItem(insert, uncontained);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectedItem());
        assertEquals("selectedIndex must be updated to position of selectedItem", 
                insert, getSelectedIndex());
    }

    @Test
    public void testFocusOnInsertItemAtSelected() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.add(index, "6-item");
        int expected = index +1;
        assertEquals("focused moved by one after inserting item", 
                expected, getFocusedIndex(expected));
    }
    
    @Test
    public void testSelectedOnInsertItemAtSelected() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selectedItem = getSelectedItem();
//        items.add(index, "6-item");
        addItem(index, createItem("6-item"));
        int expected = index +1;
        assertEquals("selection moved by one after inserting item", 
                expected, getSelectedIndex());
        assertEquals("selectedItem must be unchanged", selectedItem, getSelectedItem());
    }
    
    /**
     * Insert item at selected index
     * Anchor must be at selected.
     */
    @Test
    @ConditionalIgnore (condition = IgnoreAnchor.class)
    public void testAnchorOnInsertItemAtSelected() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.add(index, "6-item");
        int selected = getSelectedIndex();
        assertEquals("anchor must be same as selected index", 
                selected, getAnchorIndex(selected));
    }


    @Test
    public void testFocusOnClearSelection() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection();
        assertEquals("focus must be cleared clearSelection", -1, getFocusedIndex(-1));
        
    }
    @Test
    public void testFocusOnClearAndSelect() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearAndSelect(index + 1);
        assertEquals("focus must same as selected", index + 1, getFocusedIndex(index + 1));
        
    }
    @Test
    public void testFocus() {
        int index = 2;
        getSelectionModel().select(index);
        assertEquals("focus must be same as selected index", index, getFocusedIndex(index));
    }
    
    @Test
    public void testFocusOnClearSelectionAt() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index);
        assertEquals("focus must be cleared", -1, getFocusedIndex(-1));
    }
    
    @Test
    public void testFocusOnClearSelectionAtUnselected() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index + 1);
        assertEquals("focus must be unchanged on clear of unselected", index, getFocusedIndex(index));
    }
    @Test
    public void testFocusOnSelectNext() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectNext();
        int next = index + 1;
        assertEquals("focus must be same as next index", next, 
                getFocusedIndex(next));
    }

    @Test
    public void testFocusOnSelectPrevious() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectPrevious();
        int previous = index - 1;
        assertEquals("focus must be same as previous index", previous, 
                getFocusedIndex(previous));
    }

    @Test
    public void testFocusOnSelectFirst() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectFirst();
        int first = 0;
        assertEquals("focus must be same as first index", first, 
                getFocusedIndex(first));
    }

    @Test
    public void testFocusOnSelectLast() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectLast();
        int last = items.size() - 1;
        assertEquals("focus must be same as first index", last, 
                getFocusedIndex(last));
    }
    
    @Test
    public void testFocusWithoutSelection() {
        if (getFocusModel() == null) return;
        int index = 2;
        getSelectionModel().select(index);
        getFocusModel().focusNext();
        int next = getFocusModel().getFocusedIndex();
        assertEquals(index +1, next);
        assertFalse(getSelectionModel().isSelected(next));
    }
    
    @Test
    public void testAnchorOnFocusNextWithoutSelection() {
        if (getFocusModel() == null) return;
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getFocusModel().focusNext();
        int next = getFocusModel().getFocusedIndex();
        assertEquals("anchor must be unchanged when moving focus", 
                index, getAnchorIndex(index));
    }
    /**
     * Anchor must be set on select in empty selection.
     * 
     * Note: plain anchor testing doesn't make sense here - it's controlled by behaviour which is 
     * part of skin which is not yet installed after instantiation ... so 
     * need to force the creation by adding it to a life stage.
     */
    @Test
    public void testAnchor() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        assertEquals("anchor must be same as selected index", index, 
                getAnchorIndex(index));
    }
    
    /**
     * Anchor testing: must be reset on clearAndSelect.
     */
    @Test
    public void testAnchorClearAndSelect() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearAndSelect(index + 1);
        int selected = getSelectedIndex();
        assertEquals("anchor must be same as selected index", 
                selected, getAnchorIndex(selected));
    }
    
    @Test
    public void testAnchorClearSelectionAt() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index);
        assertEquals("anchor must be cleared", 
                -1, getAnchorIndex(-1));
    }
    
    @Test
    public void testAnchorClearSelectionAtUnselected() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index + 1);
        assertEquals("anchor must be unchanged on clear unselected", 
                index, getAnchorIndex(index));
    }
    
    @Test
    public void testAnchorClearSelection() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection();
        assertEquals("anchor must be cleared", 
                -1, getAnchorIndex(-1));
    }
    /**
     * Anchor must not be moved after adding/removing items below.
     */
    @Test
    public void testAnchorOnRemoveItemBelow() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.remove(index + 1);
        int selected = getSelectedIndex();
        assertEquals("anchor must be same as selected index", 
                selected, getAnchorIndex(selected));
    }
    
    /**
     * Anchor must not be moved after adding/removing items below.
     */
    @Test
    public void testAnchorOnInsertItemBelow() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.add(index + 1, "6-item");
        int selected = getSelectedIndex();
        assertEquals("anchor must be same as selected index", 
                selected, getAnchorIndex(selected));
    }

    /**
     * Anchor must be move after adding/removing items above.
     */
    @Test
    @ConditionalIgnore (condition = IgnoreAnchor.class)
    public void testAnchorOnInsertItemAbove() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.add(0, "6-item");
        int selected = getSelectedIndex();
        assertEquals("anchor must be same as selected index", 
                selected, getAnchorIndex(selected));
    }
    
    /**
     * Anchor must be move after adding/removing items above.
     */
    @Test
    @ConditionalIgnore (condition = IgnoreAnchor.class)
    public void testAnchorOnRemoveItemAbove() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.remove(0);
        int selected = getSelectedIndex();
        assertEquals("anchor must be same as selected index", 
                selected, getAnchorIndex(selected));
    }
    
//    * Reported: https://javafx-jira.kenai.com/browse/RT-38494
//    * mismatch between spec and implementation
    /**
     * Bug or feature? Can select the item in empty selection
     * which then still reports to be empty.
     * 
     * Violates doc of isEmpty:
     * "test whether there are any selected indices/items.
     * It will return true if there are no selected items."
     * 
     * Reported: https://javafx-jira.kenai.com/browse/RT-38494
     * mismatch between spec and implementation
     * 
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectUncontainedIfEmptySelection() {
        Object item = "uncontained";
        getSelectionModel().select(item);
        assertEquals("sanity: the item is selected", item, getSelectedItem());
        assertFalse("probably DOC ERROR: selection must not be empty", 
                getSelectionModel().isEmpty());
    }
    
    /**
     * Issue: broken invariant: if selectedIndex >= 0 --> selectItem = get(selectedIndex)
     * 
     * Bug or feature? select(T) explicitly states that it _attempts_ to select the item
     * (and stops at the first find), but doesn't specify what happens if T isn't found.
     * 
     * This is done in getSelectedItem it specifies the item being the one
     *  "which resides in the selectedIndex position". Also in selectedItemProperty: 
     * "The selected item is either null, 
     * to represent that there is no selection, or an Object that is retrieved 
     * from the underlying data model"
     *  
     * ... so looks like a bug to me.
     *  
     * Actually, doesn't make sense to allow the index/item correlation being unsynched in the 
     * _general_ case - it's a special need if we have selections that are not necessarily 
     * backed in the model (like in swing comboboxModel)  
     * 
     * Reported: https://javafx-jira.kenai.com/browse/RT-38494
     * mismatch between spec and implementation
     * 
     */
    @Test
    public void testSelectUncontainedIfNotEmptySelection() {
        int index = 2;
        getSelectionModel().select(index);
        Object item = createItem("uncontained");
        getSelectionModel().select(item);
        if (getSelectedIndex() >= 0) {
            assertEquals("selectedItem must be model item at selectedIndex", 
                    items.get(index), getSelectedItem());
        } else {
            assertEquals("uncontained item must be selected item", item, getSelectedItem());
        }
    }
    
    /**
     * Issue: selectedIndex gets out off synch on select null item.
     * Strictly speaking, there are two issues: 
     * a) the doc doesn't specify of what happens if the item is null (it does of index being
     *   off range)
     * b) whatever happens, it must keep the invariant selectedItem == get(selectedIndex)
     * 
     * The invariant isn't explicitly documented, just ... 
     * Note: in 8u113 the behaviour is changed - selecting null clears the selection
     * in SingleSelectionModel and TableViewSelectionModel in singleSelection 
     * 
     * Reported: https://javafx-jira.kenai.com/browse/RT-38494
     * mismatch between spec and implementation
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectNullItem() {
        int index = 2;
        getSelectionModel().select(index);
        Object item = getSelectedItem();
        getSelectionModel().select(null);
        assertEquals("unspecified behaviour on select(null): what to expect?", item, getSelectedItem());
        fail("unspecified behaviour for selecting null item");
    }
    
    /**
     * Reported: https://javafx-jira.kenai.com/browse/RT-38494
     * mismatch between spec and implementation
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectNullIndex() {
        int index = 2;
        getSelectionModel().select(index);
        Object item = getSelectedItem();
        getSelectionModel().select(null);
        assertEquals("unspecified behaviour on select(null) ", index, getSelectedIndex());
        fail("unspecified behaviour for selecting null item");
    }

    /**
     * SelectionModel is documented to do nothing if the index is out of range.
     * Test that selectedItem is unchanged.
     * 
     * Reported: https://javafx-jira.kenai.com/browse/RT-38494
     * mismatch between spec and implementation
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectMinusOneItem() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().select(-1);
        assertEquals("selecting off-range (here: - 1) must have no effect on selectedItem", items.get(index), getSelectedItem());
    }

    /**
     * SelectionModel is documented to do nothing if the index is out of range.
     * Test that the selectedIndex is unchanged.
     * 
     * Reported: https://javafx-jira.kenai.com/browse/RT-38494
     * mismatch between spec and implementation
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectMinusOneIndex() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().select(-1);
        assertEquals("selecting off-range (here: -1) must have no effect on selectedIndex", index, getSelectedIndex());
     }
    /**
     * Test clearSelection(index).
     * 
     * Mostly passes, fails for TableViewSelectionModel.
     */
    @Test
    public void testClearSelectionAtIndex() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index);
        assertTrue("selection must be empty after unselect the selected", 
                getSelectionModel().isEmpty());
        assertFalse("index must be unselected", getSelectionModel().isSelected(index));
        assertEquals("index must be cleared", 
                -1, getSelectedIndex());
    }

    @Test
    public void testClearSelectionAtUnselected() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(index + 1);
        assertEquals("selectedIndex must be unchanged after clearing unselected index",
                index, getSelectedIndex());
    }
    
    @Test
    public void testClearSelectAtInvalidIndex() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().clearSelection(items.size());
        assertTrue("index must still be selected " + index, getSelectionModel().isSelected(index));
        assertEquals("index must still be selected", 
                index, getSelectedIndex());
        
    }
    /**
     * Strictly speaking the api doc of select(index) is incorrect: "will not 
     * clear the selection of other indices" - should specify that it certainly
     * will if selection constraints apply (like in singleSelection mode/implementations)
     */
    @Test
    public void testSelectOther() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().select(index + 1);
        assertEquals(index +1, getSelectedIndex());
    }
    
//---------- navigational methods on empty selection
    
    /**
     * Intuitively expected behaviour in most implementations, but doc violation
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectNextOnEmpty() {
        getSelectionModel().clearSelection();
        getSelectionModel().selectNext();
        assertEquals("intuitve: next on empty is first", 0, 
                getSelectedIndex());
        fail("doc clash: _will only succeed if there is currently a lead/focused index_");
    }
    
    /**
     * Intuitively expected behaviour in most implementations, but doc violation
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDocErrors.class)
    public void testSelectPreviousOnEmpty() {
        getSelectionModel().clearSelection();
        getSelectionModel().selectPrevious();
        assertEquals("intuitve: previous on empty is last", items.size() - 1, 
                getSelectedIndex());
        fail("doc clash: _will only succeed if there is currently a lead/focused index_");
    }
    
//---------------------------- passing tests    
    
    /**
     * Incorrect selection behaviour after sorting the model
     * 
     * a) selectedIndex must be updated to new position (instead of being cleared)
     * b) in the unusual case that the model decides to clear the selected index, 
     *    it  must report having done so in isEmpty
     * 
     * Passes in fx8(u113)
     */
    @Test
    public void testSelectedIndexAfterSort() {
        int first = 0;
        int last = items.size() -1;
        getSelectionModel().select(first);
        FXCollections.sort(items);
        assertFalse("selection must not be empty after sorting", getSelectionModel().isEmpty());
        assertTrue("last index must be selected", getSelectionModel().isSelected(last));
        assertEquals(last, getSelectedIndex());        
    }
    /**
     * SelectionModel is documented to do nothing if the index is out of range.
     */
    @Test
    public void testSelectOffRange() {
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().select(items.size() + 3);
        assertEquals("selecting off-range (here: size + 3) must have no effect on selectedIndex", index, getSelectedIndex());
        assertEquals("selecting off-range (here: size + 3) must have no effect on selectedItem", items.get(index), getSelectedItem());
    }

    @Test
    public void testSelectItemSyncIndex() {
        int index = 2;
        Object item = items.get(index);
        getSelectionModel().select(item);
        assertEquals(index, getSelectedIndex());
    }
    
    /**
     * Natural (though not explicitly specified) synch of selectedIndex and 
     * selectedItem.
     */
    @Test
    public void testSelectIndexSyncItem() {
        int index = 2;
        getSelectionModel().select(index);
        assertEquals(items.get(index), getSelectedItem());
    }

    @Test
    public void testIsSelectedNegativeIndex() {
        assertFalse("-1 must not be selected", getSelectionModel().isSelected(-1));
        assertFalse("-11 must not be selected", getSelectionModel().isSelected(-11));
    }
//-------------- initial state
    
    // as of 8u20, the ListView, TableView have the first row selected
    // done in a focusListener as fix for rt-25679
    // further changes expected in 8u40 - looks like moved to the model somehow
    // recent changeset has all tests calling clearSelection
    // http://hg.openjdk.java.net/openjfx/8u-dev/rt/rev/258d08a27dc0
    // == fix for F2 having no effect
    @Test
    public void testInitialSelection() {
        assertEquals(-1, getSelectedIndex());
        assertEquals(null, getSelectedItem());
        assertTrue(getSelectionModel().isEmpty());
    }

    @Test
    public void testInitialFocus() {
        assertEquals(-1, getFocusedIndex(-1));
    }

    @Test
    public void testInitialAnchor() {
        initSkin();
        assertEquals(-1, getAnchorIndex(-1));
    }
    
    @Test
    public void testCopeWithNullSelectionModel() {
        setSelectionModel(null);
    }

    /**
     * The stageLoader used to force skin creation. It's an artefact of fx
     * instantiation process, not meant to be really used.
     * Note that it's the responsibility of the test method itself (not the setup)
     * to init if needed.
     */
    protected StageLoader loader;

    protected void initSkin() {
        loader = new StageLoader(getView());
        // doesn't make a difference: still spurious RejectedExecutionException ..
        // triggered by task PaintRenderJob
//        PlatformImpl.runAndWait(() -> loader = new StageLoader(getView()));
    }

    @Before
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        view = createView(items);
        if (getFocusModel() != null) {
            getFocusModel().focus(-1);
        }
    }
    
    protected abstract V createView(ObservableList items);
    
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

    protected void removeAll(int... indices) {
        List result = new ArrayList(); 
        Arrays.stream(indices).forEach(i -> result.add(items.get(i)));
//        List result =  Arrays.stream(indices)
//                .map(i ->  items.get(i)).collect(Collectors.toList());
        items.removeAll(result);
    }
    /**
     * Re-configures the view with new items via view.setItems(other)
     * @param other
     */
    protected abstract void resetItems(ObservableList other);

    protected void addItem(int pos, Object item) {
        items.add(pos, item);
    }

    protected void setItem(int pos, Object item) {
        items.set(pos, item);
    }

    protected void clearItems() {
        items.clear();
    }
    protected V getView() {
        return view;
    }
    
    protected abstract T getSelectionModel();

    protected abstract void setSelectionModel(T model);

    protected Object getSelectedItem() {
        return getSelectionModel().getSelectedItem();
    }

    protected int getSelectedIndex() {
        return getSelectionModel().getSelectedIndex();
    }
    

    /**
     * Returns the index of the anchor value. Note that subclasses which store a
     * compound value need to override and extract the index.
     * 
     * Same trick as with focusIndex: 
     * Subclasses that don't have the notion of anchor shcould override to
     * return the input index.
     * 
     * @return
     */
    protected int getAnchorIndex(int index) {
        Object anchor = getView().getProperties().get(ANCHOR_KEY);
        return anchor != null ? (int) anchor : -1;
    }


    /**
     * The signature is a bit of a hack: simple singleSelectionModels don't have the
     * notion of focus/anchor. The related tests don't make much sense then. Without 
     * having (or me not knowing them) parameterized ignors, the tests would fail. Too
     * lazy to override/ignore in each subclass, so this method will simply
     * return  the input index if focusModel == null.
     *  
     * @param index the default value for views that don't have a focusModel
     * @return 
     */
    protected int getFocusedIndex(int index) {
        return getFocusModel() != null? getFocusModel().getFocusedIndex() : index;
    }
    
    protected abstract FocusModel getFocusModel();
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SelectionIssues.class
            .getName());

}
