/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

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
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeDeferredIssue;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.ListChangeReport;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

/**
 * 
 * Testing TreeIndexMappedList. 
 * <p>
 * Remember: plain access will work as long as TreeIndicesList does its work correctly.
 * The tricky part (and the whole point of TreeIndexMappedList) is firing the correct
 * notifications.
 *  
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeIndexMappedListTest {
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

    private TreeIndexMappedList indexedItems;


 //---------------------------------- test TreeModifications
    
    @Test
    public void testValueChangedRootChild() {
        int childIndex = 2;
        TreeItem item = rootChildren.get(childIndex);
        int indexInIndices = childIndex + 1;
        indicesList.setIndices(indexInIndices);
        report.clear();
        item.setValue("other value");
        assertEquals("index unchanged", indexInIndices, indicesList.get(0).intValue());
        assertEquals("event count after setValue", 1, report.getEventCount());
    }
    
    @Test
    public void testGraphicChangedRootChild() {
        int childIndex = 2;
        TreeItem item = rootChildren.get(childIndex);
        int indexInIndices = childIndex + 1;
        indicesList.setIndices(indexInIndices);
        report.clear();
        item.setGraphic(new CheckBox("dummy"));
        assertEquals("index unchanged", indexInIndices, indicesList.get(0).intValue());
        assertEquals("event count after setGraphic", 1, report.getEventCount());
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
        report.clear();
        TreeItem indexedItem = indexedItems.get(0);
        grandChildBranch.setExpanded(false);
        assertEquals("index unchanged on collapse of hidden grandChild", 
                index, indicesList.get(0).intValue());
        assertEquals("eventcount " + report.getLastChange(), 0, report.getEventCount());
        assertEquals("indexedItem unchanged", indexedItem, indexedItems.get(0));
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
        TreeItem indexedItem = indexedItems.get(0);
        report.clear();
        root.setExpanded(false);
        assertEquals("indices after collapse must be empty", 0, indicesList.size());
        assertEquals("indexedItems empty", 0, indexedItems.size());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals("old indexed must be removed", indexedItem, c.getRemoved().get(0));
    }
    
    @Test
    public void testExpandChild() {
        TreeItemX childBranch = createBranch("expandedChild");
        rootChildren.add(0, childBranch);
        // index > child index
        int index = 3;
        indicesList.setIndices(index);
        TreeItem indexedItem = indexedItems.get(0);
        report.clear();
        childBranch.setExpanded(true);
        int childExpanded = childBranch.getExpandedDescendantCount();
        int expandedIndex = index + childExpanded -1;
        assertEquals("index increased by child count", expandedIndex, indicesList.get(0).intValue());
        assertEquals("eventcount " + report.getLastChange(), 0, report.getEventCount());
        assertEquals("indexedItem unchanged", indexedItem, indexedItems.get(0));
    }
    
    @Test
    public void testExpandRoot() {
        root.setExpanded(false);
        int index = 0;
        indicesList.setIndices(index);
        report.clear();
        root.setExpanded(true);
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
        assertEquals("eventcount", 0, report.getEventCount());
        assertEquals("indexedItem unchanged", root, indexedItems.get(0));
    }

//------------------------------ modifications to children
//
    
    @Test
    public void testSetAllRootIndexed() {
        indicesList.setIndices(0);
        TreeItem indexedItem = indexedItems.get(0);
        report.clear();
        rootChildren.setAll(createItems(rawItems.subList(3, 6)));
        assertEquals("index unchanged on replacing children of indexed items", 
                0, indicesList.get(0).intValue());
        assertEquals("eventcount", 0, report.getEventCount());
        assertEquals("indexedItem unchanged", indexedItem, indexedItems.get(0));
    }
    @Test
    public void testSetAllRootChildIndexed() {
        int index = 1;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.setAll(createItems(rawItems.subList(3, 6)));
        assertEquals("indices on replaced children must be cleared", 0, indicesList.size());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleRemoved(report.getLastChange()));
        
    }
    /**
     * PENDING JW:
     * Single child replaced ... what should happen?
     * Here we replace with a collapsed child
     */
    @Test
    public void testSetCollapsedChildBefore() {
        TreeItemX child = createBranch("single-replaced-child");
        int index = 3;
        indicesList.setIndices(index);
        TreeItem indexedItem = indexedItems.get(0);
        rootChildren.set(0, child);
        report.clear();
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals("eventcount on indexedItems", 0, report.getEventCount());
        assertEquals("indexedItem unchanged", indexedItem, indexedItems.get(0));
    }
    
    /**
     * PENDING JW:
     * Single child replaced ... what should happen?
     * Here we replace with a expanded child
     */
    @Test
    public void testSetExpandedChildBefore() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int expandedCount = child.getExpandedDescendantCount();
        int index = 3;
        indicesList.setIndices(index);
        TreeItem indexedItem = indexedItems.get(0);
        report.clear();
        rootChildren.set(0, child);
        int expected = index + expandedCount -1;
        assertEquals(expected, indicesList.get(0).intValue());
        assertEquals("eventcount", 0, report.getEventCount());
        assertEquals("indexedItem unchanged", indexedItem, indexedItems.get(0));
    }
    
    /**
     * PENDING JW:
     * Single child replaced ... what should happen?
     * Here we replace with a collapsed child
     * 
     * It's less the _what_ than the actual doing it!
     * @see IgnoreTreeDeferredIssue
     */
    @Test
    @ConditionalIgnore(condition = IgnoreTreeDeferredIssue.class)
    public void testSetCollapsedChildAt() {
        TreeItemX child = createBranch("single-replaced-child");
        int index = 3;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.set(index -1, child);
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleReplaced(report.getLastChange()));
    }
    
    /**
     * PENDING JW:
     * Single child replaced ... what should happen?
     * Here we replace with a expanded child
     * 
     * Hmm ... can't see any difference to set(i, item) in list, or is there?
     * 
     * It's less the _what_ than the actual doing it!
     * @see IgnoreTreeDeferredIssue
     */
    @Test 
    @ConditionalIgnore(condition = IgnoreTreeDeferredIssue.class)
    public void testSetExpandedChildAt() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.set(index -1, child);
        assertFalse("indicesList must not be empty", indicesList.isEmpty());
        if (!indicesList.isEmpty()) {
            int intValue = indicesList.get(0).intValue();
            assertTrue("index must not be negative, was " , intValue >= 0);
            assertEquals("index must be unchanged", index, intValue);
            assertEquals("eventcount", 0, report.getEventCount());
        }
        fail("TBD: need to specify what to do on setChildAt");
    }
    
    @Test
    public void testRemoveFromCollapsedChild() {
        TreeItemX child = createBranch("expandedChild");
        rootChildren.add(0, child);
        int index = 4;
        indicesList.setIndices(index);
        report.clear();
        child.getChildren().remove(0);
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
        assertEquals("eventcount", 0, report.getEventCount());
    }

    @Test
    public void testRemoveFromExpandedChild() {
        TreeItemX child = createBranch("expandedChild");
        child.setExpanded(true);
        rootChildren.add(0, child);
        int childIndex = 4;
        TreeItem childItem = (TreeItem) child.getChildren().get(childIndex);
        int index = tree.getRow(childItem);
        indicesList.setIndices(index);
        report.clear();
        assertEquals("sanity: indexed is childItem", childItem, indexedItems.get(0));
        child.getChildren().remove(childItem);
        assertEquals("indexedItems is empty", 0, indexedItems.size());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testRemoveChildAt() {
        int index = 2;
        indicesList.setIndices(index);
        report.clear();
        TreeItem item = tree.getTreeItem(index);
        rootChildren.remove(item);
        assertEquals("indexed item must cleared", 0, indexedItems.size());
        assertEquals("fired remove", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testRemoveChildAbove() {
        int index = 2;
        indicesList.setIndices(index);
        TreeItem item = tree.getTreeItem(index);
        rootChildren.remove(0);
        assertEquals("item must be unchanged", item, indexedItems.get(0));
    }
    
    @Test
    public void testAddToHiddenChild() {
        TreeItemX child = createBranch("collapsedChild");
        rootChildren.add(0, child);
        int index = 4;
        indicesList.setIndices(index);
        TreeItem item = tree.getTreeItem(index);
        child.getChildren().add(0, createItem("added grandChild"));
        assertEquals("item must be unchanged", item, indexedItems.get(0));
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
        TreeItem item = tree.getTreeItem(index);
        grand.getChildren().add(0, createItem("added grandChild"));
        assertEquals("item must be unchanged", item, indexedItems.get(0));
    }
    
    @Test
    public void testAddExpandedChild() {
        TreeItemX child = createBranch("expandedChild");
        child.setExpanded(true);
        int index = 4;
        indicesList.setIndices(index);
        TreeItem item = tree.getTreeItem(index);
        rootChildren.add(0, child);
        assertEquals("item must be unchanged", item, indexedItems.get(0));
    }
    
    @Test
    public void testAddChild() {
        int index = 2;
        indicesList.setIndices(index);
        report.clear();
        TreeItem item = tree.getTreeItem(index);
        rootChildren.add(0, createItem("newItemAt-0"));
        assertEquals("item must be unchanged", item, indexedItems.get(0));
        assertEquals("nothing fired", 0, report.getEventCount());
    }
    
//--------------------- indicesList api
    
    /**
     * Direct modifications to the indices should work the same way as
     * in IndexMappedList - it is listening to list changes of the indicesList,
     * tree-nature doesn't show.
     */
    @Test
    public void testSetIndicesOnChildBranch() {
        TreeItemX child = createBranch("expanded Child");
        child.setExpanded(true);
        int inserted = 2;
        rootChildren.add(inserted, child);
        int index = inserted + 3;
        TreeItem item = tree.getTreeItem(index);
        indicesList.setIndices(index);
        assertEquals("indexed item", item, indexedItems.get(0));
        assertEquals("event count", 1, report.getEventCount());
    }
    
    @Test
    public void testSetIndices() {
        int index = 2;
        indicesList.setIndices(index);
        TreeItem item = tree.getTreeItem(index);
        assertEquals("size after setting index", 1, indexedItems.size());
        assertEquals("indexed item", item, indexedItems.get(0));
        assertEquals("event count", 1, report.getEventCount());
    }
    
    @Test
    public void testInitial() {
        assertEquals(0, indexedItems.size());
        assertTrue(root.isExpanded());
        assertSame(root, tree.getRoot());
        assertTrue(tree.isShowRoot());
    }
    
    @Before
    public void setup() {
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        root = createBranch("root");
        rootChildren = root.getChildren();
        root.setExpanded(true);
        tree = new TreeView(root);
        indicesList = new TreeIndicesList(tree);
        indexedItems = new TreeIndexMappedList(indicesList);
        report = new ListChangeReport(indexedItems);
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

}
