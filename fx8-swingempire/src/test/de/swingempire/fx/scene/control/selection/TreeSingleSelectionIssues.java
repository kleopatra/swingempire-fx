/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeFocus;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeUncontained;
import static org.junit.Assert.*;

/**
 * Testing single selection api in TableViewSelectionModel, for both selection modes.
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TreeSingleSelectionIssues extends SingleSelectionIssues<TreeView, MultipleSelectionModel> {


    /**
     * Undoc'ed but nearly always implemented:
     * Have a selectedItem that is not part of the underlying list (implies
     * selectedIndex < 0), then insert that item: selectedIndex must
     * then be updated to the index of the selectedItem in the list.
     * 
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeUncontained.class)
    public void testSelectedUncontainedAfterInsertUncontained() {
        super.testSelectedUncontainedAfterInsertUncontained();
    }

    @Override
    @Test 
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusOnInsertItemAbove() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
//        items.add(0, "6-item");
        addItem(0, createItem("6-item"));
        int expected = index +1;
        Platform.runLater(() -> 
        assertEquals("selection moved by one after inserting item", 
                expected, getFocusIndex(expected))
        );
    }
    
    @Test 
    @Override
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusOnInsertItemAtSelected() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        items.add(index, "6-item");
        int expected = index +1;
        Platform.runLater(() -> 
        assertEquals("focused moved by one after inserting item", 
                expected, getFocusIndex(expected))
                );
    }
    
    /**
     * Core installs listener twice of items reset, introduces problems with insert
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusOnInsertItemAtSelected39042() {
        ObservableList other = FXCollections.observableArrayList(items.subList(0, 5));
        resetItems(other);
        int index = 2;
        getSelectionModel().select(index);
//        other.add(index, "6-item");
        addItem(index, createItem("6-item"));
        int expected = index +1;
        assertEquals("focused moved by one after inserting item", 
                expected, getFocusIndex(expected));
    }

    /**
     * Remove above: doesn't sound like RT-30931, no ambiguity if selected/focused
     * not involved.
     * 
     * Focus updated in platform.runlater
     */
    @Test
    @Override
    public void testFocusOnRemoveItemAbove() {
        int index = 2;
        getSelectionModel().select(index);
        assertEquals("sanity: focus set along with selected index", index, getFocusIndex(index));
//        items.remove(1);
        removeItem(1);
        int expected = index -1;
        Platform.runLater(() -> {
            assertEquals("open 30931 - focus after remove above focused", expected, getFocusIndex(expected));
        });
        
    }
    
    

    /**
     * Sorting needs special handling (?) for a tree.
     */
    @Test
    @Ignore
    @Override
    public void testSelectedIndexAfterSort() {
        super.testSelectedIndexAfterSort();
    }

    /**
     * Core installs listener twice of items reset, introduces problems with insert
     * Need to override here - otherwise would disturb non-tree tests.
     */
    @Test
    @Override
    public void testSelectedOnInsertItemAtSelected39042() {
        ObservableList other = FXCollections.observableArrayList(items.subList(0, 5));
        resetItems(other);
        int index = 2;
        getSelectionModel().select(index);
        addItem(index, createItem("6-item"));
        int expected = index +1;
        assertEquals("selected moved by one after inserting item", 
                expected, getSelectionModel().getSelectedIndex());
    }

    /**
     * @param multiple
     */
    public TreeSingleSelectionIssues(boolean multiple) {
        super(multiple);
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

    @Override
    protected void resetItems(ObservableList other) {
        TreeItem root = getView().getRoot();
        List items = createItems(other);
        root.getChildren().setAll(items);
    }
    
    @Override
    protected void removeItem(int pos) {
        getView().getRoot().getChildren().remove(pos);
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
    protected MultipleSelectionModel getSelectionModel() {
        MultipleSelectionModel model = getView().getSelectionModel();
        return model;
    }

    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    @Override
    protected void setSelectionModel(MultipleSelectionModel model) {
        getView().setSelectionModel(model);
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeSingleSelectionIssues.class.getName());
}
