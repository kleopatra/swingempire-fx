/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeDeferredIssue;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeFocus;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTreeUncontained;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Base class for testing single selection behaviour in treeView.
 * 
 * Note: so far, this tests only the linear part, that is the only branch
 * is the root which is expanded.
 *  
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class AbstractTreeSingleSelectionIssues extends
        SingleSelectionIssues<TreeView, MultipleSelectionModel> {

    /**
     * @param multiple
     */
    public AbstractTreeSingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

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
                    expected, getFocusedIndex(expected))
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
                expected, getFocusedIndex(expected))
                );
    }

    /**
     * Core installs listener twice of items reset, introduces problems with
     * insert
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTreeFocus.class)
    public void testFocusOnInsertItemAtSelected39042() {
        super.testFocusOnInsertItemAtSelected39042();
    }

    /**
     * For a tree, the requirement is unclear. 
     * PENDING JW: why should it be different than a list?
     * fails in isolated testing of TreeIndicesList/TreeIndexMappedList - because
     * it's simply not yet implemented
     * 
     * so the real
     * question here is why it doesn't at this complete test?
     * In fact, it does with the conditionalIgnore set to false ..
     */
    @Override
    @Test
    @ConditionalIgnore (condition = IgnoreTreeDeferredIssue.class)
    public void testSelectedOnSetItemAtSelectedFocused() {
        super.testSelectedOnSetItemAtSelectedFocused();
    }

    @Test
    @Override
    @ConditionalIgnore (condition = IgnoreTreeFocus.class)
    public void testFocusOnClearSelectionAt() {
        super.testFocusOnClearSelectionAt();
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
            assertEquals("sanity: focus set along with selected index", index, getFocusedIndex(index));
    //        items.remove(1);
            removeItem(1);
            int expected = index -1;
            Platform.runLater(() -> {
                assertEquals("open 30931 - focus after remove above focused", expected, getFocusedIndex(expected));
            });
            
        }

    /**
     * Sorting needs special handling (?) for a tree.
     */
    @Test
    @Override
    @ConditionalIgnore(condition = IgnoreTreeDeferredIssue.class)
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
                expected, getSelectedIndex());
    }

    @Override
    protected void resetItems(ObservableList other) {
        TreeItem root = getView().getRoot();
        List items = createItems(other);
        root.getChildren().setAll(items);
    }

    
    @Override
    protected void removeAll(int... indices) {
        ObservableList children = getView().getRoot().getChildren();
        List toRemove = new ArrayList();
        Arrays.stream(indices).forEach(i -> toRemove.add(children.get(i)));
        children.removeAll(toRemove);
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

}
