/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.property;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import de.swingempire.fx.collection.TreeIndicesList;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.property.PropertyIgnores.IgnoreTreeGetRow;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.TreeModificationReport;

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
    private TreeModificationReport report;
    private TreeItemX root;
    private ObservableList rawItems;
    private ObservableList<TreeItem> rootChildren;

    private TreeView tree;

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
//        childAbove.setExpanded(true);
//        rootChildren.add(1, childAbove);
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
 
 //----------------------------------
    
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
    
    @Test
    public void testRemoveExpandedChild() {
        TreeItemX child = createBranch("expandedChild");
        child.setExpanded(true);
        int expandedCount = child.getExpandedDescendantCount();
        rootChildren.add(0, child);
        int index = expandedCount + 4;
        indicesList.setIndices(index);
        int expected = index - expandedCount;
        rootChildren.remove(child);
        assertEquals("index decreased by child's expanded size", expected, indicesList.get(0).intValue());
    }
    
    @Test
    public void testRemoveChild() {
        int index = 2;
        indicesList.setIndices(index);
        rootChildren.remove(0);
        assertEquals("index increased by one", index -1, indicesList.get(0).intValue());
    }
    
    @Test
    public void testAddToHiddenChild() {
        TreeItemX child = createBranch("collapsedChild");
        rootChildren.add(0, child);
        int index = 4;
        indicesList.setIndices(index);
        child.getChildren().add(0, createItem("added grandChild"));
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
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
        grand.getChildren().add(0, createItem("added grandChild"));
        assertEquals("index unchanged", index, indicesList.get(0).intValue());
    }
    
    @Test
    public void testAddExpandedChild() {
        TreeItemX child = createBranch("expandedChild");
        child.setExpanded(true);
        int expandedCount = child.getExpandedDescendantCount();
        int index = 4;
        indicesList.setIndices(index);
        rootChildren.add(0, child);
        int expected = index + expandedCount;
        assertEquals("index increased by child's expanded size", expected, indicesList.get(0).intValue());
    }
    
    @Test
    public void testAddChild() {
        int index = 2;
        indicesList.setIndices(index);
        rootChildren.add(0, createItem("newItemAt-0"));
        assertEquals("index increased by one", index + 1, indicesList.get(0).intValue());
    }
    
    @Test
    public void testSetIndices() {
        int index = 2;
        indicesList.setIndices(index);
        assertEquals("size after setting index", 1, indicesList.size());
        assertEquals("index stored", index, indicesList.get(0).intValue());
    }
    
    @Test
    public void testInitial() {
        assertEquals(0, indicesList.size());
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
    }
    
    protected TreeItemX createItem(Object item) {
        return new TreeItemX(item);
    }

    protected ObservableList<TreeItem> createItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(item -> items.add(createItem(item)));
        return items;
    }
    
    protected TreeItemX createBranch(Object item) {
        TreeItemX child = createItem(item);
        child.getChildren().setAll(createItems(rawItems));
        return child;
    }

}
