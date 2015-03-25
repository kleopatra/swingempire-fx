/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.property.PropertyIgnores.IgnoreTreeGetRow;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeDeferredIssue;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.FXUtils.ChangeType;
import de.swingempire.fx.util.ListChangeReport;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeIndicesListTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    private TreeIndicesList indicesList;
    private ListChangeReport report;
    // root is expanded
    private TreeItemX root;
    // tree with isShowRoot true
    private TreeView tree;
    private ObservableList rawItems;
    private ObservableList<TreeItem> rootChildren;

    /**
     * TreeIndicesList must cope with hiding a collapsed root: forced to 
     * expanded.
     * Here we test root unselected -> nothing selected after
     */
    @Test
    public void testShowRootHideCollapsedUnselected() {
        root.setExpanded(false);
        tree.setShowRoot(false);
        assertTrue("root must be expanded on hiding", root.isExpanded());
        assertTrue(indicesList.isEmpty());
        assertEquals(0, report.getEventCount());
    }
    
    /**
     * TreeIndicesList must cope with hiding a collapsed root: forced to 
     * expanded.
     * Here we test root selected -> Options:
     * - nothing selected after: would be inconsistent with removing selected
     * - first child of root selected
     * 
     * Note: the above options are for tree selection, here we test the bare
     * TreeIndicesList: 
     */
    @Test
    public void testShowRootHideCollapsedSelected() {
        root.setExpanded(false);
        indicesList.setIndices(0);
        report.clear();
        tree.setShowRoot(false);
        assertTrue("root must be expanded on hiding", root.isExpanded());
        assertEquals(0, indicesList.size());
        assertEquals("eventCount", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleRemoved(report.getLastChange()));
        assertEquals(1, indicesList.oldIndices.size());
        assertEquals(0, indicesList.oldIndices.get(0));
    }
    
    /**
     * Testing internals: marker sent before firing 
     */
    @Test
    public void testShowRootMarker() {
        ChangeReport report = new ChangeReport(indicesList.showRootProperty());
        tree.setShowRoot(false);
        assertEquals("showRootMarker null after processing ", null, indicesList.getShowRoot());
        assertEquals(2, report.getEventCount());
        assertEquals(false, report.getLastOldValue());
        assertEquals(null, report.getLastNewValue());
    }
    
 //---------------------------------- test TreeModifications
    
    @Test
    public void testValueChangedRootChild() {
        int childIndex = 2;
        TreeItem item = rootChildren.get(childIndex);
        int indexInIndices = childIndex + 1;
        indicesList.setIndices(indexInIndices);
        item.setValue("other value");
        assertEquals("index unchanged", indexInIndices, indicesList.get(0).intValue());
    }
    @Test
    public void testGraphicChangedRootChild() {
        int childIndex = 2;
        TreeItem item = rootChildren.get(childIndex);
        int indexInIndices = childIndex + 1;
        indicesList.setIndices(indexInIndices);
        item.setGraphic(new CheckBox("dummy"));
        assertEquals("index unchanged", indexInIndices, indicesList.get(0).intValue());
    }
    /**
     * no change if hidden item is collapsed.
     */
    @Test
    public void testCollapseHiddenChild() {
        TreeItemX childBranch = createBranch("collapsedChild");
        int childExpanded = childBranch.getExpandedDescendantCount();
        TreeItemX grandChildBranch = createBranch("expandedGrandChild");
        grandChildBranch.setExpanded(true);
        childBranch.getChildren().add(0, grandChildBranch);
        assertEquals("sanity: expandedCount unchanged by adding expanded child", 
                childExpanded, childBranch.getExpandedDescendantCount());
        rootChildren.add(0, childBranch);
        int index = 6;
        indicesList.setIndices(index);
        grandChildBranch.setExpanded(false);
        assertEquals("index unchanged on collapse of hidden grandChild", 
                index, indicesList.get(0).intValue());
    }

    @Test
    public void testCollapseChild() {
        TreeItemX childBranch = createBranch("expandedChild");
        childBranch.setExpanded(true);
        int childExpanded = childBranch.getExpandedDescendantCount();
        rootChildren.add(0, childBranch);
        int rootExpanded = root.getExpandedDescendantCount();
        // test branch has same length
        assertEquals("expanded", 2 * childExpanded, rootExpanded);
        // select last of root
        int last = rootExpanded - 1;
        indicesList.setIndices(last);
        childBranch.setExpanded(false);
        // Note: expandedCount includes the item itself, a collapsed
        // item has a count of 1!
        int collapsedLast = last - childExpanded + childBranch.getExpandedDescendantCount();
        assertEquals("index after collapse ", collapsedLast, indicesList.get(0).intValue());
    }
    
    @Test
    public void testCollapseRoot() {
        int index = 2;
        indicesList.setIndices(index);
        assertEquals("sanity: expandedDescendents", rawItems.size() + 1, root.getExpandedDescendantCount());
        root.setExpanded(false);
        assertEquals("indices after collapse must be empty", 0, indicesList.size());
    }

    /**
     * no change if hidden item is collapsed.
     */
    @Test
    public void testExpandHiddenChild() {
        TreeItemX childBranch = createBranch("collapsedChild");
        int childExpanded = childBranch.getExpandedDescendantCount();
        TreeItemX grandChildBranch = createBranch("expandedGrandChild");
        childBranch.getChildren().add(0, grandChildBranch);
        assertEquals("sanity: expandedCount unchanged by adding expanded child", 
                childExpanded, childBranch.getExpandedDescendantCount());
        rootChildren.add(0, childBranch);
        int index = 6;
        indicesList.setIndices(index);
        grandChildBranch.setExpanded(true);
        assertEquals("index unchanged on expand of hidden grandChild", 
                index, indicesList.get(0).intValue());
    }


    @Test
    public void testExpandChild() {
        TreeItemX childBranch = createBranch("expandedChild");
        rootChildren.add(0, childBranch);
        // index > child index
        int index = 3;
        indicesList.setIndices(index);
        childBranch.setExpanded(true);
        int childExpanded = childBranch.getExpandedDescendantCount();
        int expandedIndex = index + childExpanded -1;
        assertEquals("index unchanged", expandedIndex, indicesList.get(0).intValue());
    }
    
    @Test
    public void testExpandRoot() {
        root.setExpanded(false);
        int index = 0;
        indicesList.setIndices(index);
        root.setExpanded(true);
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
    }

//------------------------------ modifications to children

    
    @Test
    public void testSetAllRootIndexed() {
        indicesList.setIndices(0);
        rootChildren.setAll(createItems(rawItems.subList(3, 6)));
        assertEquals("index unchanged on replacing children of indexed items", 
                0, indicesList.get(0).intValue());
    }
    @Test
    public void testSetAllRootChildIndexed() {
        int index = 1;
        indicesList.setIndices(index);
        rootChildren.setAll(createItems(rawItems.subList(3, 6)));
        assertEquals("indices on replaced children must be cleared", 0, indicesList.size());
    }
    
//----------------- test single replace
    
    /**
     * Replace a single child before selected
     * Here we replace with a collapsed child
     */
    @Test
    public void testSetCollapsedChildBefore() {
        TreeItemX child = createBranch("single-replaced-child");
        int index = 3;
        indicesList.setIndices(index);
        rootChildren.set(0, child);
        assertEquals(index, indicesList.get(0).intValue());
    }
    
    /**
     * Replace a single child before selected
     * Here we replace with a expanded child
     */
    @Test
    public void testSetExpandedChildBefore() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int expandedCount = child.getExpandedDescendantCount();
        int index = 3;
        indicesList.setIndices(index);
        rootChildren.set(0, child);
        int expected = index + expandedCount -1;
        assertEquals(expected, indicesList.get(0).intValue());
    }
    
//-------------------------- tests for replacing selected treeItem    
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
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        rootChildren.set(index -1, child);
        indicesList.setIndices(index);
        TreeItem collapsedChild = createBranch("another-single-replaced");
        rootChildren.set(index -1, collapsedChild);
        assertFalse(indicesList.isEmpty());
        if (!indicesList.isEmpty())
            assertEquals(index, indicesList.get(0).intValue());
    }
    
    /**
     */
    @Test 
    public void testSetExpandedChildAtExpandedFewer() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        rootChildren.set(index -1, child);
        indicesList.setIndices(index);
        // replacing child has fewer children 
        TreeItemX expandedChild = createBranch("another-single-replaced");
        expandedChild.getChildren().remove(0);
        expandedChild.setExpanded(true);
        assertEquals("sanity: replacing child has fewer children and both expanded", 
                child.getExpandedDescendantCount() - 1, expandedChild.getExpandedDescendantCount());
        rootChildren.set(index -1, expandedChild);
        
        assertFalse(indicesList.isEmpty());
        if (!indicesList.isEmpty()) {
            int intValue = indicesList.get(0).intValue();
            assertTrue("index must not be negative, was " , intValue >= 0);
            assertEquals("index must be unchanged", index, intValue);
        }
    }
    
    /**
     * Replace selected expanded child with an expanded child with 
     * greater # of items as the old.
     */
    @Test 
    public void testSetExpandedChildAtExpandedMore() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        rootChildren.set(index -1, child);
        indicesList.setIndices(index);
        TreeItemX expandedChild = createBranch("another-single-replaced");
        // replacingChild has more children
        expandedChild.getChildren().add(createItem("excess grandChild"));
        expandedChild.setExpanded(true);
        assertEquals("sanity: replacing child has more children and both expanded", 
                child.getExpandedDescendantCount() + 1, expandedChild.getExpandedDescendantCount());
        rootChildren.set(index -1, expandedChild);
        
        assertFalse(indicesList.isEmpty());
        if (!indicesList.isEmpty()) {
            int intValue = indicesList.get(0).intValue();
            assertTrue("index must not be negative, was " , intValue >= 0);
            assertEquals("index must be unchanged", index, intValue);
        }
    }
    /**
     * Replace selected expanded child with an expanded child with 
     * more items than the old.
     */
    @Test 
    public void testSetExpandedChildAtExpandedSame() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        rootChildren.set(index -1, child);
        indicesList.setIndices(index);
        // replacingChild has same # of children
        TreeItemX expandedChild = createBranch("another-single-replaced");
        expandedChild.setExpanded(true);
        assertEquals("sanity: same # of children and both expanded", 
                child.getExpandedDescendantCount(), expandedChild.getExpandedDescendantCount());
        rootChildren.set(index -1, expandedChild);

        assertFalse(indicesList.isEmpty());
        if (!indicesList.isEmpty()) {
            int intValue = indicesList.get(0).intValue();
            assertTrue("index must not be negative, was " , intValue >= 0);
            assertEquals("index must be unchanged", index, intValue);
        }
    }
    
    /**
     * replace collapsed with collapsed item.
     */
    @Test
    public void testSetCollapsedChildAtCollapsed() {
        TreeItemX child = createBranch("single-replaced-child");
        int index = 3;
        indicesList.setIndices(index);
        rootChildren.set(index -1, child);
        assertFalse(indicesList.isEmpty());
        if (!indicesList.isEmpty())
            assertEquals(index, indicesList.get(0).intValue());
    }
    
    /**
     * replace expanded with collapsed item.
     */
    @Test 
    public void testSetExpandedChildAtCollapsed() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        indicesList.setIndices(index);
        rootChildren.set(index -1, child);
        assertFalse(indicesList.isEmpty());
        if (!indicesList.isEmpty()) {
            int intValue = indicesList.get(0).intValue();
            assertTrue("index must not be negative, was " , intValue >= 0);
            assertEquals("index must be unchanged", index, intValue);
        }
    }
    
//-------------- end replacing selected
    @Test
    public void testRemoveFromCollapsedChild() {
        TreeItemX child = createBranch("expandedChild");
        rootChildren.add(0, child);
        int index = 4;
        indicesList.setIndices(index);
        child.getChildren().remove(0);
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
    }

    
    @Test
    public void testRemoveExpandedChild() {
        TreeItemX child = createBranch("expandedChild");
        child.setExpanded(true);
        int expandedCount = child.getExpandedDescendantCount();
        rootChildren.add(0, child);
        int index = expandedCount + 4;
        indicesList.setIndices(index);
        report.clear();
        int expected = index - expandedCount;
        rootChildren.remove(child);
        assertEquals("index decreased by child's expanded size", expected, indicesList.get(0).intValue());
        assertEquals("eventCount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testRemoveChildAbove() {
        int index = 2;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.remove(0);
        assertEquals("index decreased by one", index -1, indicesList.get(0).intValue());
        assertEquals("eventCount", 1, report.getEventCount());
    }
    
    @Test
    public void testRemoveChildAt() {
        int index = 2;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.remove(tree.getTreeItem(index));
        assertEquals(0, indicesList.size());
        assertEquals("eventCount", 1, report.getEventCount());
    }
    
    @Test
    public void testAddToHiddenChild() {
        TreeItemX child = createBranch("collapsedChild");
        rootChildren.add(0, child);
        int index = 4;
        indicesList.setIndices(index);
        report.clear();
        child.getChildren().add(0, createItem("added grandChild"));
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
        assertEquals("eventCount", 0, report.getEventCount());
    }
    
    @Test
    public void testAddToHiddenExpandedGrandChild() {
        TreeItemX child = createBranch("collapsedChild");
        rootChildren.add(0, child);
        TreeItemX grand = createBranch("expandedGrandChild");
        child.getChildren().add(0, grand);
        grand.setExpanded(true);
        int index = 4;
        indicesList.setIndices(index);
        report.clear();
        grand.getChildren().add(0, createItem("added grandChild"));
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
        assertEquals("eventCount", 0, report.getEventCount());
    }
    
    @Test
    public void testAddExpandedChild() {
        TreeItemX child = createBranch("expandedChild");
        child.setExpanded(true);
        int expandedCount = child.getExpandedDescendantCount();
        int index = 4;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.add(0, child);
        int expected = index + expandedCount;
        assertEquals("index increased by child's expanded size", expected, indicesList.get(0).intValue());
        assertEquals("eventCount", 1, report.getEventCount());
    }
    
    @Test
    public void testAddChild() {
        int index = 2;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.add(0, createItem("newItemAt-0"));
        assertEquals("index increased by one", index + 1, indicesList.get(0).intValue());
        assertEquals("eventCount", 1, report.getEventCount());
    }
    
//--------------------- indicesList api (copied from IndicesListTest)
    @Test
    public void testSetAllIndices() {
        indicesList.setAllIndices();
        assertEquals(tree.getExpandedItemCount() , indicesList.size());
    }
    
    @Test
    public void testSetIndices() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] setIndices = new int[] {2, 4, 6, 7};
        indicesList.setIndices(setIndices);
        assertEquals(setIndices.length, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleReplaced(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(indices[i]);
        }
        assertEquals(base, c.getRemoved());
    }
    
    @Test
    public void testClearAll() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearAllIndices();
        assertEquals(0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
//        report.prettyPrint();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(indices[i]);
        }
        assertEquals(base, c.getRemoved());
    }
    @Test
    public void testClearSomeIndicesNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] clear = new int[] {5, 1};
//        new PrintingListChangeListener("clearSomeNotification", indicesList);
        indicesList.clearIndices(clear);
        assertEquals(1, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertEquals("must be 2 disjoint removes", 2, getChangeCount(report.getLastChange(), ChangeType.REMOVED));
    }
    @Test
    public void testClearIndicesNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearIndices(indices);
        assertEquals(0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("must be single remove", wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(indices[i]);
        }
        assertEquals(base, c.getRemoved());
    }
    
    @Test
    public void testClearUnsetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearIndices(2);
        assertEquals(0, report.getEventCount());
    }
    
    
    @Test
    public void testAddAlreadySetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.addIndices(indices[0]);
        assertEquals(0, report.getEventCount());
    }

    @Test
    public void testSetAlreadySetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.setIndices(indices);
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testAddMoreNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] more = new int[] { 2, 7, 4};
        indicesList.addIndices(more);
        
        assertEquals(1, report.getEventCount());
        assertEquals(3, getChangeCount(report.getLastChange()));
    }
    
    @Test
    public void testAddMultiple() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("got a single added", wasSingleAdded(report.getLastChange()));
    }
    
    @Test
    public void testAddSingle() {
        int index = 3;
        indicesList.addIndices(index);
        assertEquals(1, indicesList.size());
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals(1, report.getEventCount());
    }

    /**
     * Source index is same as get, kind of.
     * SourceIndex not supported?
     */
//    @Test
//    public void testSourceIndexInRange() {
//        int[] indices = new int[] { 3, 5, 1};
//        indicesList.addIndices(indices);
//        assertEquals(indices.length, indicesList.size());
//        Arrays.sort(indices);
//        for (int i = 0; i < indices.length; i++) {
//            assertEquals("sourceIndex " + i, indices[i], indicesList.getSourceIndex(i));
//        }
//        
//    }
    /**
     * Test sourceIndex if index off range.
     * Changed implementation to throw IndexOOB 
     * (off range access is always a programming error)
     */
//    @Test (expected = IndexOutOfBoundsException.class)
//    public void testSourceIndexOffRange() {
//        int[] indices = new int[] { 3, 5, 1};
//        indicesList.addIndices(indices);
//        assertEquals(indices.length, indicesList.size());
//        assertEquals(-1, indicesList.getSourceIndex(-1));
//        assertEquals(-1, indicesList.getSourceIndex(indices.length));
//    }
    
    /**
     * Test get if index off range.
     * Changed implementation to throw IndexOOB 
     * (off range access is always a programming error)
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOffRange() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        assertEquals(-1, indicesList.get(-1).intValue());
        assertEquals(-1, indicesList.get(indices.length).intValue());
    }
    
    
    @Test
    public void testAddEmpty() {
        indicesList.addIndices();
        assertEquals(0, report.getEventCount());
    }

    @Test
    public void testClearsIndicesOnSettingNullRoot() {
        indicesList.setIndices(3);
        tree.setRoot(null);
        assertEquals("empty selection when setting null root", 0, indicesList.size());
    }
    /**
     * copes with null root
     */
    @Test
    public void testNullRoot() {
        TreeView tree = new TreeView();
        assertNull(tree.getRoot());
        TreeIndicesList indicesList = new TreeIndicesList(tree);
    }
    @Test
    public void testInitial() {
        assertEquals(0, indicesList.size());
        assertNull(indicesList.getShowRoot());
        assertNull(indicesList.getTreeModification());
        assertEquals(0, indicesList.oldIndices.size());
    }
    
    @Before
    public void setup() {
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        root = createBranch("root");
        rootChildren = root.getChildren();
//        rootChildren.setAll(createItems(rawItems));
        root.setExpanded(true);
        tree = new TreeView(root);
        indicesList = new TreeIndicesList(tree);
        report = new ListChangeReport(indicesList);
    }
    
    protected TreeItemX createItem(Object item) {
        return new TreeItemX(item);
    }

    protected ObservableList<TreeItemX> createItems(List list) {
        ObservableList items = FXCollections.observableArrayList();
        list.stream().forEach(item -> items.add(createItem(item)));
        return items;
    }
    
    protected TreeItemX createBranch(Object item) {
        TreeItemX child = createItem(item);
        child.getChildren().setAll(createItems(rawItems));
        return child;
    }

  //--------------------------- tree.getRow  
  //-----------  Reported as https://javafx-jira.kenai.com/browse/RT-39661
      
      /**
       * Quick test of tree.getRow(treeItem) semantics: 
       * seems to return the row if all expanded, not the actual?
       */
      @Test
      @ConditionalIgnore(condition = IgnoreTreeGetRow.class)
      public void testTreeRowInvisibleInitialExpanded() {
          TreeItemX child = createBranch("child 1");
          int grandIndex = 3;
          TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
          child.setExpanded(true);
          int childIndex = 2;
          rootChildren.add(childIndex, child);
          int row = tree.getRow(grandChild);
          assertEquals("sanity: row of grandChild", (1 + childIndex) + (1 + grandIndex), row);
          child.setExpanded(false);
          assertEquals("grandChild not visible", -1, tree.getRow(grandChild));
      }
      
      /**
       * Here we insert a collapsed child and query the row of a grandchild 
       * (which isn't visible)
       * Doesn't matter whether parent was initially expanded or
       * collapsed, result always as if it were expanded.
       */
      @Test
      @ConditionalIgnore(condition = IgnoreTreeGetRow.class)
      public void testTreeRowInvisibleInitialCollapsed() {
          TreeItemX child = createBranch("child 1");
          int grandIndex = 3;
          TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
          child.setExpanded(false);
          int childIndex = 2;
          rootChildren.add(childIndex, child);
          int row = tree.getRow(grandChild);
          assertEquals("grandChild not visible", -1, row);
      }
      
      /**
       * Here we insert a several collapsed children, 
       *  and query the row of a grandchild in the last.
       *  
       * WRONG test: inserted at same absolute index, doesn't
       *  matter whether we inserted something above or not.
       *  
       * Keep for not repeating 
       *   
       * (which isn't visible)
       * Doesn't matter whether parent was initially expanded or
       * collapsed, result always as if it were expanded.
       */
      @Test @Ignore
      public void testTreeRowInvisibleInitialCollapsedMultiple() {
          TreeItemX childAbove = createBranch("above-collapsed");
//          childAbove.setExpanded(true);
//          rootChildren.add(1, childAbove);
          TreeItemX child = createBranch("child 1");
          int grandIndex = 3;
          TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
          int childIndex = 5;
          rootChildren.add(childIndex, child);
          int row = tree.getRow(grandChild);
          assertEquals("grandChild not visible", -1, row);
      }
      
      /**
       * Sanity test: use core TreeItem, stand-alone test
       * for report.
       * 
       * same misbehaviour as custom - so go ahead with custom testing
       */
      @Test
      @ConditionalIgnore(condition = IgnoreTreeGetRow.class)
      public void testTreeRowInvisibleInitialCollapsedCore() { 
          // create a tree with expanded root
          TreeItem root = createSubTree("root");
          root.setExpanded(true);
          TreeView tree = new TreeView(root);
          int initialRowCount = tree.getExpandedItemCount();
          assertEquals("sanity: itemcount before addition", 
                  root.getChildren().size() + 1, initialRowCount);
          // create a collapsed new child to insert into the root
          TreeItem newChild = createSubTree("added-child");
          TreeItem grandChild = (TreeItem) newChild.getChildren().get(2);
          root.getChildren().add(6, newChild);
          assertEquals("sanity: row count of tree increased by one", initialRowCount +1, 
                  tree.getExpandedItemCount());
          // query the row of a grand-child
          int row = tree.getRow(grandChild);
          // grandChild not visible, row coordinate in tree is not available
          assertEquals("grandChild not visible", -1, row);
          // the other way round: if we get a row, expect the item at the row be the grandChild
          assertEquals(grandChild, tree.getTreeItem(row));
      }

      protected TreeItem createSubTree(String name) {
          ObservableList rawItems = FXCollections.observableArrayList(
                  "9-item", "8-item", "7-item", "6-item", 
                  "5-item", "4-item", "3-item", "2-item", "1-item");
          TreeItem root = new TreeItem(name);
          rawItems.stream().forEach(item -> root.getChildren().add(new TreeItem(item)));
          return root;
      }
      
      /**
       * Effect of unexpected tree.getRow on selection: behaves as expected.
       */
      @Test
      @ConditionalIgnore(condition = IgnoreTreeGetRow.class)
      public void testTreeSelectRowInvisible() {
          TreeItemX child = createBranch("child 1");
          int grandIndex = 3;
          TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
          child.setExpanded(true);
          int childIndex = 2;
          rootChildren.add(childIndex, child);
          int row = tree.getRow(grandChild);
          assertEquals("sanity: row of grandChild", (1 + childIndex) + (1 + grandIndex), row);
          child.setExpanded(false);
          tree.getSelectionModel().select(row);
          assertEquals("what do we get?", row, tree.getSelectionModel().getSelectedIndex());
          assertEquals(grandChild, tree.getSelectionModel().getSelectedItem());
      }
   

}
