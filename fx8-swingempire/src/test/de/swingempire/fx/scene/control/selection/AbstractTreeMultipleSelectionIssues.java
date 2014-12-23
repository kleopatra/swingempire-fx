/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

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
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class AbstractTreeMultipleSelectionIssues extends
    MultipleSelectionIssues<TreeView, MultipleSelectionModel<TreeItem>> {


    private ObservableList<String> rawItems;
    
    @Test
    public void testSelectedOnExpandedGrandBranchCollapsed() {
        TreeItem childBranch = createBranch("expandedChild", true);
        TreeItem grandChildBranch = createBranch("expandedGrandChild", true);
        childBranch.getChildren().add(0, grandChildBranch);
        TreeItem selected = (TreeItem) grandChildBranch.getChildren().get(rawItems.size() - 1);
        getView().getRoot().getChildren().add(0, childBranch);
        getSelectionModel().select(selected);
        TreeItem selectedItem = getSelectionModel().getSelectedItems().get(0);
        assertEquals(selectedItem, getSelectionModel().getSelectedItems().get(0));
        assertEquals(selectedItem, getSelectionModel().getSelectedItem());
        grandChildBranch.setExpanded(false);
        assertEquals("selected items size", 1, getSelectionModel().getSelectedItems().size());
        assertEquals(grandChildBranch, getSelectionModel().getSelectedItem());
//        assertEquals(index, getSelectionModel().getSelectedIndex());
//        assertEquals(index, getSelectionModel().getSelectedIndices().get(0).intValue());
    }
    
    @Test
    public void testCollapsedBranch() {
        TreeItem childBranch = createBranch("collapsedChild");
        TreeItem grandChildBranch = createBranch("expandedGrandChild");
        grandChildBranch.setExpanded(true);
        childBranch.getChildren().add(0, grandChildBranch);
        getView().getRoot().getChildren().add(0, childBranch);
        int index = 6;
        getSelectionModel().select(index);
        TreeItem selectedItem = getSelectionModel().getSelectedItems().get(0);
        grandChildBranch.setExpanded(false);
        assertEquals(index, getSelectionModel().getSelectedIndex());
        assertEquals(index, getSelectionModel().getSelectedIndices().get(0).intValue());
        assertEquals(selectedItem, getSelectionModel().getSelectedItems().get(0));
        assertEquals(selectedItem, getSelectionModel().getSelectedItem());
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
        TreeItem parent = getSelectionModel().getSelectedItem().getParent();
        clearItems();
        if (getSelectionModel().isEmpty()) {
            assertEquals(null, getSelectionModel().getSelectedItem());
            assertEquals(-1, getSelectionModel().getSelectedIndex());
            assertEquals("selectedItems must be empty", 0, getSelectionModel()
                    .getSelectedItems().size());
            assertTrue("", getSelectionModel().getSelectedIndices().isEmpty());
//            assertEquals("focus must be cleared", -1, getFocusModel()
//                    .getFocusedIndex());
        } else {
            assertEquals(0, getSelectionModel().getSelectedItems().indexOf(parent));
            assertEquals("parent selected after clearing children", parent,
                    getSelectionModel().getSelectedItem());
        }
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreTreeFocus.class)
    public void testFocusOnClearItemsRange() {
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        TreeItem parent = getSelectionModel().getSelectedItem().getParent();
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
        TreeItem parent = getSelectionModel().getSelectedItem().getParent();
        int parentIndex = getView().getRow(parent);
        clearItems();
        if (getSelectionModel().isEmpty()) {
            assertTrue("selection must be empty", getSelectionModel().isEmpty());
            assertEquals(-1, getSelectionModel().getSelectedIndex());
            assertEquals(null, getSelectionModel().getSelectedItem());
            assertTrue("", getSelectionModel().getSelectedIndices().isEmpty());
            assertTrue("", getSelectionModel().getSelectedItems().isEmpty());
//            assertEquals("focus must be cleared", -1, getFocusModel()
//                    .getFocusedIndex());
        } else {
            assertEquals("selectedIndex at parentIndex", parentIndex, getSelectionModel().getSelectedIndex());
            assertEquals("parent must be selected after clearing ", parent, 
                    getSelectionModel().getSelectedItem());
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
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getSelectionModel().getSelectedIndices().get(0).intValue());
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
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, uncontained);
        setAllItems(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex updated", insertIndex, getSelectionModel().getSelectedIndex());
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
        assertEquals("sanity: having uncontained selectedItem", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("sanity: no selected index", -1, getSelectionModel().getSelectedIndex());
        // make uncontained part of the items by replacing old items
        ObservableList copy = FXCollections.observableArrayList(items);
        int insertIndex = 3;
        copy.add(insertIndex, createItem("anything"));
        setAllItems(copy);
        assertEquals("sanity: selectedItem unchanged", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex unchanged", -1, getSelectionModel().getSelectedIndex());
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
    
    @Override
    protected MultipleSelectionModel<TreeItem> getSelectionModel() {
        return getView().getSelectionModel();
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
        items = createItems(rawItems);
        view = createView(items);
        // complete override, need to handle focus here as well
        if (getFocusModel() != null) {
            getFocusModel().focus(-1);
        }
    }

    /**
     * Here we expect the elements to be treeItems!.
     */
    @Override
    protected void setAllItems(ObservableList treeItems) {
        TreeItem root = getView().getRoot();
        root.getChildren().setAll(treeItems);
    }

    /**
     * Here we expect the new elements to be TreeItems.
     */
    @Override
    protected void setAllItems(Object... treeItems) {
        getView().getRoot().getChildren().setAll(treeItems);
    }

    @Override
    protected void removeAllItems(Object... treeItem) {
        getView().getRoot().getChildren().removeAll(treeItem);
        items.removeAll(treeItem);
    }

    @Override
    protected void removeItem(int pos) {
        getView().getRoot().getChildren().remove(pos);
        items.remove(pos);
    }

    @Override
    protected void addItem(int pos, Object treeItem) {
        getView().getRoot().getChildren().add(pos, treeItem);
    }

    @Override
    protected void setItem(int pos, Object treeItem) {
        getView().getRoot().getChildren().set(pos, treeItem);
    }

    @Override
    protected TreeItem modifyItem(Object treeItem, String mod) {
        TreeItem old = (TreeItem) treeItem;
        return createItem(old.getValue() + mod);
    }

    @Override
    protected void clearItems() {
        getView().getRoot().getChildren().clear();
    }

    
    @Override
    protected TreeItem createItem(Object item) {
        return new TreeItem(item);
    }

    protected ObservableList<TreeItem> createItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(value -> items.add(createItem(value)));
        return items;
    }

    protected TreeItem createBranch(Object value) {
        return createBranch(value, false);
    }
    protected TreeItem createBranch(Object value, boolean expanded) {
        TreeItem item = createItem(value);
        item.getChildren().setAll(createItems(rawItems));
        item.setExpanded(expanded);
        return item;
    }
    /**
     * @param multiple
     */
    public AbstractTreeMultipleSelectionIssues(boolean multiple) {
        super(multiple);
        // TODO Auto-generated constructor stub
    }

}
