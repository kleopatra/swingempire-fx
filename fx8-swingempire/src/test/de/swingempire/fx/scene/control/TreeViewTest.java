/*
 * Created on 12.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.property.PropertyIgnores.IgnoreReported;
import de.swingempire.fx.util.ListChangeReport;
import de.swingempire.fx.util.TreeModificationReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;

/**
 * Unexpected behaviour of tree.getRow(TreeItem). Reported
 * https://javafx-jira.kenai.com/browse/RT-39661
 * 
 * fixed for 8u60, changesset:
 * http://hg.openjdk.java.net/openjfx/8u-dev/rt/rev/83f197be0dcf
 * 
 * verified fix, closed issue.
 * 
 * ----------------
 * 
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
    private ObservableList<String> rawItems;

    private TreeView tree;

//---- test tree notification events    
    
    /**
     * No use to check the selection - notification from selectedIndices/items still utterly broken!
     * see old JDK-8090563 (request to expose the change that was fired by children)
     */
    @Test
    public void testRetainChildren() {
        List retain = List.of(root.getChildren().get(2), root.getChildren().get(5));
        ListChangeReport indicesReport = new ListChangeReport(root.getChildren());
        TreeModificationReport report = new TreeModificationReport(root);
        root.getChildren().retainAll(retain);
        assertEquals(indicesReport.getLastChange(), report.getLastChange());
        TreeModificationEvent event = report.getLastEvent();
        LOG.info("" + event.getRemovedChildren());
//        prettyPrint(report.getLastChange());
        indicesReport.prettyPrint();
    }
//------------- end notification    
    /**
     * 32620 is about misbehaviour in CheckBoxTreeItem (recursive up/down 
     * updates didn't work properly with a custom selected provider - they
     * still don't but maybe something changed?) 
     */
    @Test
    public void testLeafExpanded() {
        // code comment in expandedProperty:
        // We don't fire expanded events for leaf nodes (RT-32620)
        // test in TreeViewTest test_rt28114 (ignored to due not yet done) has lines
        // the first is incorrect anyway, but assuming it were true by having 
        // removed all children children while it was expanded - should we expect
        // a change of the expanded property?
//        assertTrue(itSupport.isLeaf());
//        assertTrue(!itSupport.isExpanded());
        TreeItem child = createSubTree("child");
        child.setExpanded(true);
        tree.getRoot().getChildren().set(2, child);
        assertFalse("node with children is not a leaf", child.isLeaf());
        assertTrue(child.isExpanded());
        child.getChildren().clear();
        assertTrue("node without children is a leaf", child.isLeaf());
        assertTrue("expanded state unrelated to leaf state", child.isExpanded());
        TreeModificationReport report = new TreeModificationReport(child);
        child.setExpanded(false);
        // we can modify the expanded property of a node
        assertFalse(child.isExpanded());
        // but must not fire an expanded event
        assertEquals(0, report.getEventCount());
    }
    
//-------------------- CheckBoxTreeItem    
    /**
     * CheckBoxTreeItem must update parent state when modifying the
     * children list.
     * 
     * Reported
     * https://javafx-jira.kenai.com/browse/RT-40349
     */
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testCheckBoxTreeItemDataModification() {
        CheckBoxTreeItem checkBoxRoot = createCheckBoxSubTree("checked-root");
        checkBoxRoot.setExpanded(true);
        tree.setRoot(checkBoxRoot);
        CheckBoxTreeItem child = (CheckBoxTreeItem) checkBoxRoot.getChildren().get(2);
        child.setSelected(true);
        assertTrue(checkBoxRoot.isIndeterminate());
        root.getChildren().remove(child);
        assertFalse("root must update its check state on modifications to children list", 
                checkBoxRoot.isIndeterminate());
    }
    protected CheckBoxTreeItem createCheckBoxItem(Object item) {
        return new CheckBoxTreeItem(item);
    }

    protected ObservableList<CheckBoxTreeItem> createCheckBoxItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(item -> items.add(createCheckBoxItem(item)));
        return items;
    }

    protected CheckBoxTreeItem createCheckBoxSubTree(Object item) {
        CheckBoxTreeItem child = createCheckBoxItem(item);
        child.getChildren().setAll(createCheckBoxItems(rawItems));
        return child;
    }

    
    
//---------------------- tests around 30661    
    @Test
    public void testRowLessThanExpandedItemCount() {
        TreeItem child = createSubTree("child");
        TreeItem grandChild = (TreeItem) child.getChildren().get(rawItems.size() - 1);
        root.getChildren().add(child);
        assertTrue("row of item must be less than expandedItemCount, but was: " + tree.getRow(grandChild), 
                tree.getRow(grandChild) < tree.getExpandedItemCount());
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
        // decision was to return -1 if not visible, so the following line is an
        // incorrect assumption
//        assertEquals(grandChild, tree.getTreeItem(row));
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeViewTest.class.getName());
}
