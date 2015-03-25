/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Comparator;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.property.PropertyIgnores.IgnoreNotYetImplemented;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreNotificationIndicesOnRemove;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeAnchor;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeFocus;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeUncontained;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.ListChangeReport;
import de.swingempire.fx.util.TreeModificationReport;
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

    
//--------------------
    
//-------------------    
    @Test
    public void testNullRoot() {
        TreeView view = createEmptyView();
    }
    
    /**
     * IndicesList is throwing IllegalState: on hiding a collapsed root, 
     * the root is forced to expanded (in the showRoot property invalidation)
     * Need to cope.
     */
    @Test
    public void testHideCollapsedRoot() {
        getView().setShowRoot(true);
        TreeItem root = getView().getRoot();
        assertTrue("snaity: root expanded", root.isExpanded());
        root.setExpanded(false);
        getView().setShowRoot(false);
    }
    
    /**
     * Regression testing: 37366 - fire single event on moving selectedItem
     * to parent.
     */
    @Test
    public void testNotificationIndicesOnCollapseItemWithSelectedChild_37366() {
        getView().setShowRoot(true);
        // select child of root
        int index = 3;
        getSelectionModel().select(index);
        ListChangeReport report = new ListChangeReport(getSelectedIndices());
        getRoot().setExpanded(false);
        assertEquals(1, report.getEventCount());
    }
    
    /**
     * Regression testing: rt_37632
     * 
     */
    @Test
    public void testReplaceRootMustClearSelectionState_37632() {
        int index = 3;
        getSelectionModel().select(index);
        TreeItem replace = createItem("replaced root");
        getView().setRoot(replace);
        assertEquals("selectedIndex must be cleared", -1, getSelectedIndex());
        assertEquals("selectedItem must be cleared", null, getSelectedItem());
        assertEquals("selectedIndices must be empty ", 0 , getSelectedIndices().size());
        assertEquals("selectedItems must be empty ", 0 , getSelectedItems().size());
    }
    
    /**
     * Regression tesing: rt_38341
     * <p>
     * It's basically removeAt (open to spec/config support) with a tweak
     * for tree specifics: how to change the selection when the last child
     * of a branch is selected and removed?
     * <p>
     * For core, it doesn't make a difference: in all views implemented to decrease
     * the selectedIndex. Simple (accidentally?) has the same behaviour as core for the
     * last child of a treeItem. Need to track why/where and spec the expected behaviour.
     * <p>
     * seems to be something else (2 events fired vs. 1 expected) - need to dig
     * 
     */
    @Test
    public void testSelectionStateOnRemoveLastChildOfBranch_38341() {
        TreeItem branch = createBranch("branch", true);
        int insert = 1;
        getRoot().getChildren().add(insert , branch);
        int lastBranchIndex = branch.getChildren().size() -1;
        Object item = branch.getChildren().get(lastBranchIndex);
        // index in model is insert in root children + child index in branch + the inserted 
        // node itself
        int select = insert + lastBranchIndex + 1;
        getSelectionModel().select(select);
        assertEquals("sanity: selected the expected child ", item, getSelectedItem());
        ListChangeReport indicesReport = new ListChangeReport(getSelectedIndices());
        ListChangeReport itemsReport = new ListChangeReport(getSelectedItems());
        branch.getChildren().remove(lastBranchIndex);
        if (getSelectedIndex() == select) {
            // this would be the default implementation for all SimpleXX: keep 
            // selectedIndex unchanged, effectly selecting the next item
            // which here would be the next sibling of the removed child's parent
            assertEquals("selectedIndex unchanged", select, getSelectedIndex());
            assertEquals(getView().getTreeItem(select), getSelectedItem());
            assertEquals(0, indicesReport.getEventCount());
            assertEquals(1, itemsReport.getEventCount());
            
        } else { // index was changed
            assertEquals("selected decremented", select -1, getSelectedIndex());
            assertEquals("selectedItem", getView().getTreeItem(select - 1), getSelectedItem());
            // fails .. we fire more than one event, reselect issue again?
            assertEquals("items changes fired", 1, itemsReport.getEventCount());
            assertEquals("indices changes fired ", 1, indicesReport.getEventCount());
        }
        fail("TBD: spec and implement removal of last child in a branch");
    }
    
    /**
     * Regression testing: Rt_39966 - core test with simple failed on setting
     * null root (didn't clean up selection state)
     * 
     * This here tests the null
     */
    @Test
    public void testRootNullMustClearSelection_39966() {
        int index = 3;
        getSelectionModel().select(index);
        getView().setRoot(null);
        assertEmptySelection();
    }

    @Test
    public void testSelectedOnReplaceItemWithSelectedItemInSubtree() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("child-with-selected-child", true);
        int index = 3;
        setItem(index - 1, child);
        // first grandchild
        int selected = index + 1;
        assertEquals("sanity", child.getChildren().get(0), getView().getTreeItem(selected));
        getSelectionModel().select(selected);
        TreeItem other = createBranch("replacing-child");
        setItem(index - 1, other);
        assertEquals(-1, getSelectedIndex());
    }
//-------------- test selected on replacing selected item
    
    /**
     * PENDING JW:
     * Single child replaced ... what should happen?
     * What's wrong with doing the same as with list? that is do nothing
     * for a single replace?
     * 
     * It was less the _what_ than the actual doing it!
     * 
     * below is a bunch of tests that replace the selected item:
     * - collapsed child with collapsed child
     * - expanded child with collapsed child
     * - collapsed child with expanded child
     * - expanded child with expanded child: # of children on replacing child 
     *      same/fewer/more
     * 
     */
    @Test
    public void testSetCollapsedChildAtExpanded() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("single-replaced-child", true);
        int index = 3;
        setItem(index -1, child);
        getSelectionModel().select(index);
        TreeItem collapsedChild = createBranch("another-single-replaced");
        setItem(index -1, collapsedChild);
        assertEquals(index, getSelectedIndex());
        assertEquals(collapsedChild, getSelectedItem());
    }
    
    @Test 
    public void testSetExpandedChildAtExpandedFewer() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        setItem(index -1, child);
        getSelectionModel().select(index);
        // replacing child has fewer children 
        TreeItem expandedChild = createBranch("another-single-replaced");
        expandedChild.getChildren().remove(0);
        expandedChild.setExpanded(true);
        assertEquals("sanity: replacing child has fewer children and both expanded", 
                child.getChildren().size() - 1, expandedChild.getChildren().size());
        setItem(index -1, expandedChild);
        
        assertEquals(index, getSelectedIndex());
        assertEquals(expandedChild, getSelectedItem());
    }   
    
    /**
     * Replace selected expanded child with an expanded child with 
     * greater # of items as the old.
     */
    @Test 
    public void testSetExpandedChildAtExpandedMore() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        setItem(index -1, child);
        getSelectionModel().select(index);
        TreeItem expandedChild = createBranch("another-single-replaced");
        // replacingChild has more children
        expandedChild.getChildren().add(createItem("excess grandChild"));
        expandedChild.setExpanded(true);
        assertEquals("sanity: replacing child has more children and both expanded", 
                child.getChildren().size() + 1, expandedChild.getChildren().size());
        setItem(index -1, expandedChild);
        
        assertEquals(index, getSelectedIndex());
        assertEquals(expandedChild, getSelectedItem());
    }

    /**
     * Replace selected expanded child with an expanded child with 
     * more items than the old.
     */
    @Test 
    public void testSetExpandedChildAtExpandedSame() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        setItem(index -1, child);
        getSelectionModel().select(index);
        // replacingChild has same # of children
        TreeItem expandedChild = createBranch("another-single-replaced");
        expandedChild.setExpanded(true);
        assertEquals("sanity: same # of children and both expanded", 
                child.getChildren().size(), expandedChild.getChildren().size());
        setItem(index -1, expandedChild);

        assertEquals(index, getSelectedIndex());
        assertEquals(expandedChild, getSelectedItem());
    }
    
    @Test
    public void testSetCollapsedChildAtCollapsed() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("single-replaced-child");
        int index = 3;
        getSelectionModel().select(index);
        setItem(index - 1, child);
        assertEquals(index, getSelectedIndex());
        assertEquals(child, getSelectedItem());
    }
    
    /**
     */
    @Test 
    public void testSetExpandedChildAtCollapsed() {
        getView().setShowRoot(true);
        TreeItem child = createBranch("single-replaced-child", true);
        int index = 3;
        getSelectionModel().select(index);
        setItem(index -1, child);
        assertEquals(index, getSelectedIndex());
        assertEquals(child, getSelectedItem());
    }
    

//--------------end test selected on replacing    
    
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
    public void testSelectedItemsOnRemoveGrandParentOfSelectedItemIfLast_38334() {
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
//----------------------   https://javafx-jira.kenai.com/browse/RT-40278 

    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    @Test
    public void testShowRootIndexOn() {
        int index = 3;
        getSelectionModel().select(index);
        getView().setShowRoot(true);
        assertEquals(index + 1, getSelectedIndex());
    }
    
    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    @Test
    public void testShowRootIndexOff() {
        getView().setShowRoot(true);
        int index = 3;
        getSelectionModel().select(index);
        getView().setShowRoot(false);
        assertEquals(index - 1, getSelectedIndex());
    }
    
    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    @Test
    public void testShowRootItemOn() {
        int index = 3;
        getSelectionModel().select(index);
        Object item = getSelectedItem();
        getView().setShowRoot(true);
        assertEquals("selectedItem unchanged on showing root:", item, getSelectedItem());
    }
    
    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    @Test
    public void testShowRootItemOff() {
        getView().setShowRoot(true);
        int index = 3;
        getSelectionModel().select(index);
        Object item = getSelectedItem();
        getView().setShowRoot(false);
        assertEquals("selectedItem unchanged on showing root:", item, getSelectedItem());
    }

    /**
     * Corner case: first child selected
     */
    @Test
    public void testShowRootIndexAtFirstChildOff() {
        getView().setShowRoot(true);
        getSelectionModel().select(1);
        getView().setShowRoot(false);
        assertEquals("selection moved to first child of hidden root", 0, getSelectedIndex());
    }
    
    /**
     * Corner case: first child selected
     */
    @Test
    public void testShowRootIndexAtRootAndFirstChildOff() {
        if (!multipleMode) return;
        getView().setShowRoot(true);
        getSelectionModel().selectIndices(0, 1);
        getView().setShowRoot(false);
        assertEquals(1, getSelectedIndices().size());
        assertEquals("selection moved to first child of hidden root", 0, getSelectedIndex());
    }
    
    /**
     * Corner case: root selected
     */
    @Test
    public void testShowRootIndexAtRootOff() {
        getView().setShowRoot(true);
        getSelectionModel().select(0);
        assertEquals("sanity: root selected", getRoot(), getSelectedItem());
        getView().setShowRoot(false);
        assertEquals("selection moved to first child of hidden root", 0, getSelectedIndex());
    }

    /**
     * Corner case: root selected
     */
    @Test
    public void testShowRootItemAtRootOff() {
        getView().setShowRoot(true);
        getSelectionModel().select(0);
        assertEquals("sanity: root selected", getRoot(), getSelectedItem());
        getView().setShowRoot(false);
        assertEquals("selection moved to first child of hidden root", 
                getRootChildren().get(0), getSelectedItem());
    }

    /**
     * Corner case: root selected
     * 
     * Here testing that selectedIndices don't fire on re-selecting the next.
     * Fails for simpleTreeSM: might expect a single change (we are re-selecting
     * in selectionHelper/treeIndicesList whoever might be responsible).
     * Might be a implementation limitation in IndicesBase: shiftLeft requires
     * that nothing in the removed range is selected.<p>
     * 
     * Not special to showRoot - it's a variant of notification on removeAt, 
     * @see IgnoreNotificationIndicesOnRemove
     * 
     * @see #testNotificationSelectedIndicesOnRemoveAt()
     */
    @Test
    @ConditionalIgnore(condition = IgnoreNotificationIndicesOnRemove.class)
    public void testShowRootNotificationIndicesAtRootOff() {
        getView().setShowRoot(true);
        getSelectionModel().select(0);
        ListChangeReport report = new ListChangeReport(getSelectedIndices());
        getView().setShowRoot(false);
//        report.prettyPrint();
        assertEquals("selectedIndices must not have fired", 0, report.getEventCount());
    }
    
    /**
     * Corner case: root selected
     * Here we expect the selecteItems to fire because the root selection is
     * effectively replaced by th selection of its first child
     */
    @Test
    public void testShowRootNotificationItemsAtRootOff() {
        getView().setShowRoot(true);
        getSelectionModel().select(0);
        ListChangeReport report = new ListChangeReport(getSelectedItems());
        getView().setShowRoot(false);
        assertTrue(getSelectedItems().contains(getSelectedItem()));
        assertEquals("selectedItems must have fired", 1, report.getEventCount());
    }
    
    /**
     * Corner case: root selected
     */
    @Test
    public void testShowRootNotificationItemAtRootOff() {
        getView().setShowRoot(true);
        getSelectionModel().select(0);
        ChangeReport report = new ChangeReport(getSelectionModel().selectedItemProperty());
        getView().setShowRoot(false);
        assertEquals("selectedItem must have fired", 1, report.getEventCount());
    }
    
    
    /**
     * corner case: root selected
     */
    @Test
    public void testShowRootItemAtIndexAtRootOff() {
        getView().setShowRoot(true);
        int index = 0;
        getSelectionModel().select(index);
        assertEquals(getSelectedItem(), getView().getTreeItem(getSelectedIndex()));
        getView().setShowRoot(false);
        assertEquals(getSelectedItem(), getView().getTreeItem(getSelectedIndex()));
    }
    
    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    @Test
    public void testShowRootItemAtIndexOff() {
        getView().setShowRoot(true);
        int index = 3;
        getSelectionModel().select(index);
        assertEquals(getSelectedItem(), getView().getTreeItem(getSelectedIndex()));
        getView().setShowRoot(false);
        assertEquals(getSelectedItem(), getView().getTreeItem(getSelectedIndex()));
    }
    
    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    @Test
    public void testShowRootItemAtIndexOn() {
        int index = 3;
        getSelectionModel().select(index);
        assertEquals(getSelectedItem(), getView().getTreeItem(getSelectedIndex()));
        getView().setShowRoot(true);
        assertEquals(getSelectedItem(), getView().getTreeItem(getSelectedIndex()));
    }
    
    /**
     * Items not changed, no event on selectedItems.
     * 
     */
    @Test
    public void testShowRootNotificationIndicesOn() {
        int index = 3;
        getSelectionModel().select(index);
        ListChangeReport report = new ListChangeReport(getSelectedIndices());
        getView().setShowRoot(true);
        assertEquals("selectedIndices must fire", 1, report.getEventCount());
    }
    
    /**
     * Items not changed, no event on selectedItems.
     * 
     */
    @Test
    public void testShowRootNotificationIndicesOff() {
        getView().setShowRoot(true);
        int index = 3;
        getSelectionModel().select(index);
        ListChangeReport report = new ListChangeReport(getSelectedIndices());
        getView().setShowRoot(false);
        assertEquals("selectedIndices must fire", 1, report.getEventCount());
    }
    
    /**
     * Items not changed, no event on selectedItems.
     * 
     */
    @Test
    public void testShowRootNotificationItemsOn() {
        int index = 3;
        getSelectionModel().select(index);
        ListChangeReport report = new ListChangeReport(getSelectedItems());
        getView().setShowRoot(true);
        assertEquals("selectedItems must not fire if nothing changed", 0, report.getEventCount());
    }
    
    /**
     * Items not changed, no event on selectedItems.
     * 
     */
    @Test
    public void testShowRootNotificationItemsOff() {
        getView().setShowRoot(true);
        int index = 3;
        getSelectionModel().select(index);
        ListChangeReport report = new ListChangeReport(getSelectedItems());
        getView().setShowRoot(false);
        assertEquals("selectedItems must not fire if nothing changed", 0, report.getEventCount());
    }

    @Test
    public void testShowRootFocusOn() {
        int index = 3;
        getSelectionModel().select(index);
        TreeItem focusedItem = (TreeItem) getFocusModel().getFocusedItem();
        assertEquals("sanity: focus at selected", index, getFocusedIndex());
        getView().setShowRoot(true);
        assertEquals(index + 1, getFocusedIndex());
        assertEquals(focusedItem, getFocusModel().getFocusedItem());
    }
    
    @Test
    public void testShowRootFocusOff() {
        getView().setShowRoot(true);
        int index = 3;
        getSelectionModel().select(index);
        TreeItem focusedItem = (TreeItem) getFocusModel().getFocusedItem();
        assertEquals("sanity: focus at selected", index, getFocusedIndex());
        getView().setShowRoot(false);
        assertEquals(index - 1, getFocusedIndex());
        assertEquals(focusedItem, getFocusModel().getFocusedItem());
    }
    
    @Test
    public void testShowRootAnchorOn() {
        initSkin();
        int index = 3;
        getSelectionModel().select(index);
        assertEquals("sanity: anchor", index, getAnchorIndex());
        getView().setShowRoot(true);
        assertEquals(index + 1, getAnchorIndex());
    }
    
    @Test
    public void testShowRootAnchorOff() {
        getView().setShowRoot(true);
        initSkin();
        int index = 3;
        getSelectionModel().select(index);
        assertEquals("sanity: anchor", index, getAnchorIndex());
        getView().setShowRoot(false);
        assertEquals(index - 1, getAnchorIndex());
    }
    
    
    /**
     * Sanity testing: getTreeItem(nextRow) returns correctly?
     */
    @Test
    public void testShowRootGetTreeItem() {
        int index = 3;
        TreeItem item = getView().getTreeItem(index);
        getView().setShowRoot(true);
        assertEquals(item, getView().getTreeItem(index + 1));
    }
    
    /**
     * SelectionModel must update indices on toggling showRoot
     * https://javafx-jira.kenai.com/browse/RT-40278
     */
    /** 
     * incorrect assumption: showRoot or not is property of the tree, 
     * the treeItem cannot fire on its behalf, doesn't even know it
     */
    @Test @Ignore
    public void testShowRootNotification() {
        TreeModificationReport report = new TreeModificationReport(getView().getRoot());
        getView().setShowRoot(true);
        assertEquals(1, report.getEventCount());
    }
    
//------------------ end https://javafx-jira.kenai.com/browse/RT-40278
    
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
            assertEquals("focus is where?", 0, getFocusedIndex());
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

    /**
     * Here we have two issues that are not yet implemented:
     * - any tree-specifics for sorting
     * - DONE the test itself, needs a comparator
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreNotYetImplemented.class)
    public void testSelectedIndicesAfterSort() {
        int first = 0;
        int last = items.size() -1;
        getSelectionModel().select(first);
        Comparator comparator = new TreeItemComparator();
        FXCollections.sort(items, comparator);
        assertEquals(1, getSelectedIndices().size());
        assertEquals(last, getSelectedIndices().get(0).intValue());
    }

    private static class TreeItemComparator implements Comparator<TreeItem> {

        @Override
        public int compare(TreeItem o1, TreeItem o2) {
            return ((Comparable) o1.getValue()).compareTo(o2.getValue());
        }
        
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

    @Test
    public void testInitialState() {
        assertFalse("sanity: subclasses must hide the root", getView().isShowRoot());
        assertTrue("sanity: subclasses must expand the root", getRoot().isExpanded());
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

    protected abstract TreeView createEmptyView();
    
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
