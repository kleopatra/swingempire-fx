/*
 * Created on 12.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import de.swingempire.fx.junit.JavaFXThreadingRule;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeViewTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    private TreeItem root;
    private ObservableList rawItems;

    private TreeView tree;

    @Test
    public void testRowLessThanExpandedItemCount() {
        TreeItem child = createSubTree("child");
        TreeItem grandChild = (TreeItem) child.getChildren().get(rawItems.size() - 1);
        root.getChildren().add(child);
        assertTrue("row of item must be less than expandedItemCount, but was: " + tree.getRow(grandChild), 
                tree.getRow(grandChild) < tree.getExpandedItemCount());
    }
    
    @Test
    public void testRowOfGrandChildParentCollapsedUpdatedOnInsertAbove() {
        int grandIndex = 2;
        int childIndex = 3;
        TreeItem child = createSubTree("addedChild2");
        TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
        root.getChildren().add(childIndex, child);
        int rowOfGrand = tree.getRow(grandChild);
        root.getChildren().add(childIndex -1, createSubTree("other"));
        assertEquals(rowOfGrand + 1, tree.getRow(grandChild));
    }
    
    @Test
    public void testRowOfGrandChildParentCollapsedUpdatedOnInsertAboveWithoutAccess() {
        int grandIndex = 2;
        int childIndex = 3;
        TreeItem child = createSubTree("addedChild2");
        TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
        root.getChildren().add(childIndex, child);
        int rowOfGrand = 7; //tree.getRow(grandChild);
        root.getChildren().add(childIndex, createSubTree("other"));
        assertEquals(rowOfGrand + 1, tree.getRow(grandChild));
    }
    
    
    @Test
    public void testRowOfGrandChildParentExpandedUpdatedOnInsertAbove() {
        int grandIndex = 2;
        int childIndex = 3;
        TreeItem child = createSubTree("addedChild2");
        TreeItem grandChild = (TreeItem) child.getChildren().get(grandIndex);
        child.setExpanded(true);
        root.getChildren().add(childIndex, child);
        int rowOfGrand = tree.getRow(grandChild);
        root.getChildren().add(childIndex -1, createSubTree("other"));
        assertEquals(rowOfGrand + 1, tree.getRow(grandChild));
    }

    
    /**
     * Testing getRow on grandChild: compare collapsed/expanded parent.
     */
    @Test
    public void testRowOfGrandChildDependsOnParentExpansion() {
        int grandIndex = 2;
        int childIndex = 3;
        TreeItem collapsedChild = createSubTree("addedChild");
        TreeItem collapsedGrandChild = (TreeItem) collapsedChild.getChildren().get(grandIndex);
        root.getChildren().add(childIndex, collapsedChild);
        int collapedGrandIndex = tree.getRow(collapsedGrandChild);
        int collapsedRowCount = tree.getExpandedItemCount();
        // start again
        setup();
        assertEquals(collapsedRowCount - 1, tree.getExpandedItemCount());
        TreeItem expandedChild = createSubTree("addedChild2");
        TreeItem expandedGrandChild = (TreeItem) expandedChild.getChildren().get(grandIndex);
        expandedChild.setExpanded(true);
        root.getChildren().add(childIndex, expandedChild);
        assertNotSame("getRow must depend on expansionState " + collapedGrandIndex, 
                collapedGrandIndex, tree.getRow(expandedGrandChild));
    }
    
    @Test
    public void testRowOfGrandChildInCollapsedChild() {
        // create a collapsed new child to insert into the root
        TreeItem newChild = createSubTree("added-child");
        TreeItem grandChild = (TreeItem) newChild.getChildren().get(2);
        root.getChildren().add(6, newChild);
        // query the row of a grand-child
        int row = tree.getRow(grandChild);
        // grandChild not visible, row coordinate in tree is not available
        assertEquals("grandChild not visible", -1, row);
        // the other way round: if we get a row, expect the item at the row be the grandChild
        assertEquals(grandChild, tree.getTreeItem(row));
    }

    @Test
    public void testRowOfRootChild() {
        int index = 2;
        TreeItem child = (TreeItem) root.getChildren().get(index);
        assertEquals(index + 1, tree.getRow(child));
    }
    
    @Test
    public void testExpandedItemCount() {
        int initialRowCount = tree.getExpandedItemCount();
        assertEquals(root.getChildren().size() + 1, initialRowCount);
        TreeItem collapsedChild = createSubTree("collapsed-child");
        root.getChildren().add(collapsedChild);
        assertEquals(initialRowCount + 1, tree.getExpandedItemCount());
        TreeItem expandedChild = createSubTree("expanded-child");
        expandedChild.setExpanded(true);
        root.getChildren().add(0, expandedChild);
        assertEquals(2 * initialRowCount + 1, tree.getExpandedItemCount());
    }
    
    
    @Before
    public void setup() {
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        root = createSubTree("root");
        root.setExpanded(true);
        tree = new TreeView(root);
    }
    
    protected TreeItem createItem(Object item) {
        return new TreeItem(item);
    }

    protected ObservableList<TreeItem> createItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(item -> items.add(createItem(item)));
        return items;
    }

    protected TreeItem createSubTree(Object item) {
        TreeItem child = createItem(item);
        child.getChildren().setAll(createItems(rawItems));
        return child;
    }


}
