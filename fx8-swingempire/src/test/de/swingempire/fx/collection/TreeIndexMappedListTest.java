/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.ListChangeReport;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

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
        assertEquals("sanity: root selected", root, indexedItems.get(0));
        assertEquals("sanity: indexedItems fired", 1, report.getEventCount());
        report.clear();
        tree.setShowRoot(false);
        assertTrue("root must be expanded on hiding", root.isExpanded());
        assertEquals(0, indexedItems.size());
        assertEquals("eventCount", 1, report.getEventCount());
        assertTrue("singleRemoved ", wasSingleRemoved(report.getLastChange()));
    }
    

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

//------------------ test replacing child
    
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

//------------------ test single replaced of selected node
    
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
        report.clear();
        TreeItem collapsedChild = createBranch("another-single-replaced");
        rootChildren.set(index -1, collapsedChild);
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
    }
    
    @Test 
    public void testSetExpandedChildAtExpandedFewer() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        rootChildren.set(index -1, child);
        indicesList.setIndices(index);
        report.clear();
        // replacing child has fewer children 
        TreeItemX expandedChild = createBranch("another-single-replaced");
        expandedChild.getChildren().remove(0);
        expandedChild.setExpanded(true);
        assertEquals("sanity: replacing child has fewer children and both expanded", 
                child.getExpandedDescendantCount() - 1, expandedChild.getExpandedDescendantCount());
        rootChildren.set(index -1, expandedChild);
        
        assertFalse(indicesList.isEmpty());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
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
        report.clear();
        TreeItemX expandedChild = createBranch("another-single-replaced");
        // replacingChild has more children
        expandedChild.getChildren().add(createItem("excess grandChild"));
        expandedChild.setExpanded(true);
        assertEquals("sanity: replacing child has more children and both expanded", 
                child.getExpandedDescendantCount() + 1, expandedChild.getExpandedDescendantCount());
        rootChildren.set(index -1, expandedChild);
        
        assertFalse(indicesList.isEmpty());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
        
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
        report.clear();
        // replacingChild has same # of children
        TreeItemX expandedChild = createBranch("another-single-replaced");
        expandedChild.setExpanded(true);
        assertEquals("sanity: same # of children and both expanded", 
                child.getExpandedDescendantCount(), expandedChild.getExpandedDescendantCount());
        rootChildren.set(index -1, expandedChild);

        assertFalse(indicesList.isEmpty());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testSetCollapsedChildAtCollapsed() {
        TreeItemX child = createBranch("single-replaced-child");
        int index = 3;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.set(index -1, child);
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
    }
    
    /**
     */
    @Test 
    public void testSetExpandedChildAtCollapsed() {
        TreeItemX child = createBranch("single-replaced-child");
        child.setExpanded(true);
        int index = 3;
        indicesList.setIndices(index);
        report.clear();
        rootChildren.set(index -1, child);
        assertFalse("indicesList must not be empty", indicesList.isEmpty());
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals("eventcount", 1, report.getEventCount());
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
    }
    
 // ----------- end test replace selected item
    
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
    public void testClearsIndicesOnSettingNullRoot() {
        indicesList.setIndices(3);
        tree.setRoot(null);
        assertEquals("empty selection when setting null root", 0, indexedItems.size());
    }

    /**
     * copes with null root
     */
    @Test
    public void testNullRoot() {
        TreeView tree = new TreeView();
        assertNull(tree.getRoot());
        TreeIndicesList indicesList = new TreeIndicesList(tree);
        TreeIndexMappedList indexMapped = new TreeIndexMappedList<>(indicesList);
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
