/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeDeferredIssue;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeFocus;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeUncontained;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TreeMultipleSelectionIssues extends MultipleSelectionIssues<TreeView, MultipleSelectionModel> {

    /**
     * PENDING JW: report after download u17!
     * 
     * Standalone for bug report: selectedItem not cleared on removing all
     * children.
     * 
     * Looks like https://javafx-jira.kenai.com/browse/RT-34725
     * null element in selectedItems, selectedIndex at 0
     */
    @Test
    public void testSelectedOnClearItemsReport() {
        ObservableList content = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        TreeItem root = new TreeItem("root");
        content.stream().forEach(item -> root.getChildren().add(new TreeItem(item)));
        TreeView view = new TreeView(root);
        root.setExpanded(true);
        view.setShowRoot(false);
        int index = 3;
        view.getSelectionModel().select(index );
        assertEquals("sanity: ", content.get(index), ((TreeItem) root.getChildren().get(index)).getValue());
        root.getChildren().clear();
        assertEquals("itemCount must be ", 0, view.getExpandedItemCount());
        assertEquals("selectedItem must be cleared", null, view.getSelectionModel().getSelectedItem());
        if (view.getSelectionModel().getSelectedItems().size() > 0) {
//            LOG.info("selectedItems at 0: " + view.getSelectionModel().getSelectedItems().get(0)
//                    + "\n + selectedIndices at 0: " + view.getSelectionModel().getSelectedIndices().get(0) );
        }
        assertEquals("selectedIndex must be cleared", -1, view.getSelectionModel().getSelectedIndex());
        assertEquals("selectedItems must be empty", 0, view.getSelectionModel().getSelectedItems().size());
        assertEquals("selectedIndices must be empty", 0, view.getSelectionModel().getSelectedIndices().size());
    }
    
    @Test
    @Override
    @ConditionalIgnore (condition = IgnoreTreeFocus.class)
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
        assertEquals(last, getSelectionModel().getSelectedIndices().get(0));
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
        Object uncontained = createItem("uncontained");
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
        Object uncontained = createItem("uncontained");
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

    public TreeMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected MultipleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    @Override
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        ObservableList content = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        items = createItems(content);
        view = createView(items);
        // complete override, need to handle focus here as well
        if (getFocusModel() != null) {
            getFocusModel().focus(-1);
        }
    }


    @Override
    protected TreeView createView(ObservableList items) {
        TreeItem root = new TreeItem("root");
        root.getChildren().setAll(items);
        TreeView table = new TreeView(root);
        root.setExpanded(true);
        table.setShowRoot(false);
        MultipleSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
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
    protected void removeAllItems(Object... object) {
        getView().getRoot().getChildren().removeAll(object);
        items.removeAll(object);
    }


    @Override
    protected void removeItem(int pos) {
        getView().getRoot().getChildren().remove(pos);
        items.remove(pos);
    }

    @Override
    protected void addItem(int pos, Object item) {
        getView().getRoot().getChildren().add(pos, item);
    }

    @Override
    protected void setItem(int pos, Object item) {
        getView().getRoot().getChildren().set(pos, item);
    }
    
    @Override
    protected Object modifyItem(Object item, String mod) {
        TreeItem old = (TreeItem) item;
        return createItem(old.getValue() + mod);
    }
    
    @Override
    protected void clearItems() {
        getView().getRoot().getChildren().clear();
    }

    @Override
    protected Object createItem(Object item) {
        return new TreeItem(item);
    }

    protected ObservableList createItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        for (Object object : other) {
            items.add(createItem(object));
        }
        return items;
    }

    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

//    @Override
//    protected void setSelectionModel(MultipleSelectionModel model) {
//        getView().setSelectionModel(model);
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeMultipleSelectionIssues.class.getName());
    
}
