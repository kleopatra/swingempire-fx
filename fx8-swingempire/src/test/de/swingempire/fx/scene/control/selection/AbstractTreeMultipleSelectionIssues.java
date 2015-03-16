/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeAnchor;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeDeferredIssue;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeFocus;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeUncontained;
import static org.junit.Assert.*;

/**
 * Tree-selection related tests. 
 * <p>
 * To comply with super's test assumption, the tree instantiated in setup has an
 * expanded root with super.items added to its children list and has
 * no subtrees. The root is not visible.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class AbstractTreeMultipleSelectionIssues extends
    MultipleSelectionIssues<TreeView, MultipleSelectionModel<TreeItem>> {

    /**
     * the list of values of the treeItems in a default branch.
     */
    private ObservableList<String> rawItems;
    
    /**
     * Regression testing: 
     * SelectedItems contains null after removing unselected grandParent of 
     * selectedItem
     * https://javafx-jira.kenai.com/browse/RT-38334
     * 
     * This is the exact replication of the bug report: grandParent is last
     * 
     */
    @Test
    public void testSelectedItemsOnRemoveGrandParentOfSelectedItemIfLast() {
        TreeItem grandParent = createBranch("grandParent", true);
        TreeItem childWithSelection = createBranch("selected", true);
        grandParent.getChildren().add(childWithSelection);
        TreeItem selected = (TreeItem) childWithSelection.getChildren().get(0);
        int lastIndex = rawItems.size() - 1;
        TreeItem oldFirst = (TreeItem) getRootChildren().get(lastIndex);
        getRootChildren().add(grandParent);
        getSelectionModel().select(selected);
        getRootChildren().remove(grandParent);
        assertEquals("sibling of removed selected", oldFirst, getSelectedItem());
        assertEquals("selectedItems contain sibling", oldFirst, getSelectedItems().get(0));
    }

    /**
     * Regression testing: 
     * SelectedItems contains null after removing unselected grandParent of 
     * selectedItem
     * https://javafx-jira.kenai.com/browse/RT-38334
     * 
     * different problem in my own implementation: old selected still selected!
     * Actually, no: accidentally, the value is the same, not the item. 
     * The selection is moved to the item following the removed
     * grandParent - which feels natural.
     */
    @Test
    public void testSelectedItemsOnRemoveGrandParentOfSelectedItem() {
        TreeItem grandParent = createBranch("grandParent", true);
        TreeItem childWithSelection = createBranch("selected", true);
        grandParent.getChildren().add(childWithSelection);
        TreeItem selected = (TreeItem) childWithSelection.getChildren().get(0);
        TreeItem oldFirst = (TreeItem) getRootChildren().get(0);
        getRootChildren().add(0, grandParent);
        getSelectionModel().select(selected);
        assertEquals("sanity: oldfirst is next child", oldFirst, getRootChildren().get(1));
        getRootChildren().remove(grandParent);
        assertEquals("sibling of removed selected", oldFirst, getSelectedItem());
        assertEquals("selectedItems contain sibling", oldFirst, getSelectedItems().get(0));
    }
    
    
    @Test
    public void testSelectedOnExpandedGrandBranchCollapsed() {
        TreeItem childBranch = createBranch("expandedChild", true);
        TreeItem grandChildBranch = createBranch("expandedGrandChild", true);
        childBranch.getChildren().add(0, grandChildBranch);
        TreeItem selected = (TreeItem) grandChildBranch.getChildren().get(rawItems.size() - 1);
        getRootChildren().add(0, childBranch);
        getSelectionModel().select(selected);
        TreeItem selectedItem = getSelectedItems().get(0);
        assertEquals(selectedItem, getSelectedItems().get(0));
        assertEquals(selectedItem, getSelectedItem());
        grandChildBranch.setExpanded(false);
        assertEquals("selected items size", 1, getSelectedItems().size());
        assertEquals(grandChildBranch, getSelectedItem());
//        assertEquals(index, getSelectionModel().getSelectedIndex());
//        assertEquals(index, getSelectionModel().getSelectedIndices().get(0).intValue());
    }
    
    @Test
    public void testCollapsedBranch() {
        TreeItem childBranch = createBranch("collapsedChild");
        TreeItem grandChildBranch = createBranch("expandedGrandChild");
        grandChildBranch.setExpanded(true);
        childBranch.getChildren().add(0, grandChildBranch);
        getRootChildren().add(0, childBranch);
        int index = 6;
        getSelectionModel().select(index);
        TreeItem selectedItem = getSelectedItems().get(0);
        grandChildBranch.setExpanded(false);
        assertEquals(index, getSelectedIndex());
        assertEquals(index, getSelectedIndices().get(0).intValue());
        assertEquals(selectedItem, getSelectedItems().get(0));
        assertEquals(selectedItem, getSelectedItem());
    }

   
    /**
     * Sanity? Removing last child throws IOOB if selected
     */
    @Test
    public void testSelectedRemoveLast() {
        int lastIndex = rawItems.size() - 1;
        TreeItem lastChild = (TreeItem) getRootChildren().get(lastIndex);
        getSelectionModel().select(lastChild);
        getRootChildren().remove(lastChild);
    }
//---------------- super adjusted to tree-specifics: 
// root not shown, expanded, no subtrees    
    /**
     * Yet another test on clearing the items: test selection state for multiple
     * selections. This assumptions is (? maybe) wrong for trees: clearing out
     * the children might result in selecting the parent if any of the children
     * had been selected.
     * 
     * Note: base test has the root not shown (to align with list tests)
     * so this should work as base test! Overridden anyway to comment focus
     * test (still no use, as we use core focusModel)
     */
    @Override
    @Test
    public void testSelectedOnClearItemsRange() {
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        TreeItem parent = getSelectedItem().getParent();
        clearItems();
        if (getSelectionModel().isEmpty()) {
            assertEquals(null, getSelectedItem());
            assertEquals(-1, getSelectedIndex());
            assertEquals("selectedItems must be empty", 0, getSelectedItems().size());
            assertTrue("", getSelectedIndices().isEmpty());
//            assertEquals("focus must be cleared", -1, getFocusModel()
//                    .getFocusedIndex());
        } else {
            assertEquals(0, getSelectedItems().indexOf(parent));
            assertEquals("parent selected after clearing children", parent,
                    getSelectedItem());
        }
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreTreeFocus.class)
    public void testFocusOnClearItemsRange() {
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        TreeItem parent = getSelectedItem().getParent();
        clearItems();
        if (getSelectionModel().isEmpty()) {
            assertEquals("focus must be cleared", -1, getFocusModel()
                    .getFocusedIndex());
        } else {
            assertEquals("focus is where?", 0, getFocusIndex());
            assertEquals("parent focused after clearing children", parent,
                getFocusModel().getFocusedItem());
        }
    }

    /**
     * Yet another test on clearing the items: test selection state for single
     * selected item
     */
    @Override
    @Test
    public void testSelectedOnClearItemsSingle() {
        int index = 2;
        getSelectionModel().select(index);
        TreeItem parent = getSelectedItem().getParent();
        int parentIndex = getView().getRow(parent);
        clearItems();
        if (getSelectionModel().isEmpty()) {
            assertTrue("selection must be empty", getSelectionModel().isEmpty());
            assertEquals(-1, getSelectedIndex());
            assertEquals(null, getSelectedItem());
            assertTrue("", getSelectedIndices().isEmpty());
            assertTrue("", getSelectedItems().isEmpty());
//            assertEquals("focus must be cleared", -1, getFocusModel()
//                    .getFocusedIndex());
        } else {
            assertEquals("selectedIndex at parentIndex", parentIndex, getSelectedIndex());
            assertEquals("parent must be selected after clearing ", parent, 
                    getSelectedItem());
        }
    }

    @Test
    @Override
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusNotSelectedIndexOnInsertAbove() {
        super.testFocusNotSelectedIndexOnInsertAbove();
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusUnselectedUpdateOnInsertAbove() {
        super.testFocusUnselectedUpdateOnInsertAbove();
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusUnselectedUpdateOnRemoveAbove() {
        super.testFocusUnselectedUpdateOnRemoveAbove();
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusMultipleRemoves() {
        super.testFocusMultipleRemoves();
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeDeferredIssue.class)
    public void testSelectedIndicesAfterSort() {
        int first = 0;
        int last = items.size() -1;
        getSelectionModel().select(first);
        FXCollections.sort(items);
        assertEquals(1, getSelectedIndices().size());
        assertEquals(last, getSelectedIndices().get(0).intValue());
    }

    @Override
    public void testFocusFirstRemovedItem() {
        super.testFocusFirstRemovedItem();
    }

    /**
     * What happens with uncontained when replacing items with list that contains it? 
     * selectedIndex is updated: is consistent with typical handling - a selectedItem
     * that had not been in the list is treated like being independent and left alone.
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeUncontained.class)
    public void testSelectedOnSetItemsWithUncontained() {
        TreeItem uncontained = createItem("uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, uncontained);
        setAllItems(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectedIndex());
    }

    /**
     * What happens if uncontained not in new list? 
     * selectedIndex -1, uncontained still selectedItem
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeUncontained.class)
    public void testSelectedOnSetItemsWithoutUncontained() {
        TreeItem uncontained = createItem("uncontained");
        // prepare state
        getSelectionModel().select(uncontained);
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, createItem("anything"));
        setAllItems(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectedItem());
        assertEquals("selectedIndex unchanged", -1, getSelectedIndex());
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeUncontained.class)
    public void testSelectedOnInsertUncontainedMultiple() {
        super.testSelectedOnInsertUncontainedMultiple();
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeUncontained.class)
    public void testSelectedOnInsertUncontainedSingle() {
        super.testSelectedOnInsertUncontainedSingle();
    }
    
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeAnchor.class)
    public void testAlsoSelectNextAscending() {
        super.testAlsoSelectNextAscending();
    } 
    /**
     * Spurious exceptions when uncommenting ...
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeAnchor.class)
    public void testAlsoSelectPreviousAscending() {
        super.testAlsoSelectPreviousAscending();
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeAnchor.class)
    public void testAnchorOnClearSelectionAt() {
        super.testAnchorOnClearSelectionAt();
    }    

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeAnchor.class)
    public void testAnchorOnClearSelectionOfAnchorInRangeWithNext() {
        super.testAnchorOnClearSelectionOfAnchorInRangeWithNext();
    }
    
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeAnchor.class)
    public void testAnchorOnSelectRangeAscending() {
        super.testAnchorOnSelectRangeAscending();
    }
    
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeAnchor.class)
    public void testAnchorOnClearSelectionAtAfterRange() {
        super.testAnchorOnClearSelectionAtAfterRange();
    }
    
    protected TreeItem getRoot() {
        return getView().getRoot();
    }
    protected ObservableList getRootChildren() {
        return getRoot().getChildren();
    }
    @Override
    protected MultipleSelectionModel<TreeItem> getSelectionModel() {
        return getView().getSelectionModel();
    }

    /**
     * Overridden to get a list of treeItems
     */
    @Override
    protected ObservableList<TreeItem> getSelectedItems() {
        return getSelectionModel().getSelectedItems();
    }

    /**
     * Overridden to return a TreeItem.
     */
    @Override
    protected TreeItem getSelectedItem() {
        return getSelectionModel().getSelectedItem();
    }

    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    @Override
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        items = createTreeItems(rawItems);
        view = createView(items);
        // complete override, need to handle focus here as well
        if (getFocusModel() != null) {
            getFocusModel().focus(-1);
        }
    }
    
    /**
     * TreeItem has no setItems, so this is implemneted to delegate
     * to setAll.
     * 
     * Here we expect the elements to be treeItems.
     */
    @Override
    protected void setItems(ObservableList otherTreeItems) {
        setAllItems(otherTreeItems);
    }


    /**
     * Here we expect the elements to be treeItems!.
     */
    @Override
    protected void setAllItems(ObservableList treeItems) {
        getRootChildren().setAll(treeItems);
    }

    /**
     * Here we expect the new elements to be TreeItems.
     */
    @Override
    protected void setAllItems(Object... treeItems) {
        getRootChildren().setAll(treeItems);
    }

    @Override
    protected void removeAllItems(Object... treeItem) {
        getRootChildren().removeAll(treeItem);
        items.removeAll(treeItem);
    }

    @Override
    protected void removeItem(int pos) {
        getRootChildren().remove(pos);
        items.remove(pos);
    }

    @Override
    protected void addItem(int pos, Object treeItem) {
        getRootChildren().add(pos, treeItem);
    }

    @Override
    protected void setItem(int pos, Object treeItem) {
        getRootChildren().set(pos, treeItem);
    }

    @Override
    protected TreeItem modifyItem(Object treeItem, String mod) {
        TreeItem old = (TreeItem) treeItem;
        return createItem(old.getValue() + mod);
    }

    @Override
    protected void clearItems() {
        getRootChildren().clear();
    }

    
    @Override
    protected TreeItem createItem(Object item) {
        return new TreeItem(item);
    }

    /**
     * Creates and returns a list of items acceptable by the treeItem.
     * For each item in other we create a TreeItem with that item as value
     * and add the treeItem to the returned list.
     *  
     * @param other a list of elements (of arbitrary type) 
     * @return a list of TreeItems with their value set to the raw item.
     */
    protected ObservableList<TreeItem> createTreeItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(value -> items.add(createItem(value)));
        return items;
    }

    /**
     * Created a collapsed default branch with given value.
     * 
     * @param value
     * @return
     */
    protected TreeItem createBranch(Object value) {
        return createBranch(value, false);
    }
    
    /**
     * Creates a default branch with given value, expanded controlled by flag.
     * @param value
     * @param expanded
     * @return
     */
    protected TreeItem createBranch(Object value, boolean expanded) {
        TreeItem item = createItem(value);
        item.getChildren().setAll(createTreeItems(rawItems));
        item.setExpanded(expanded);
        return item;
    }
    /**
     * @param multiple
     */
    public AbstractTreeMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractTreeMultipleSelectionIssues.class.getName());
}
