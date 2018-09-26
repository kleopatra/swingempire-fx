/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.selection.SelectionUtils.TestMultipleSelectionModel;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

/**
 *
 * Testing singleselection api in ListViewSelectionModel in both selection modes.
 * Here we test unchanged core ListView with core selection/focusModel.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListCoreSingleSelectionIssues extends SingleSelectionIssues<ListView, MultipleSelectionModel> {

    /**
     *  Old models still hanging around and regarding themselves as still
     *  attached to the listView.
     *  
     *  Revise to _not_ use the outdated custom focusModel.
     *  No: The custom focusModel is just fine, compiles in all versions
     *
     */
    @Test
    public void testFocusModelReleased() {
        ListView listView = new ListView(items);
        FocusModel oldFocus = listView.getFocusModel();
        int initialFocus = 0;
        assertEquals(initialFocus , oldFocus.getFocusedIndex());
        FocusModel focusModel = new ListViewAFocusModel<>(listView);
        listView.setFocusModel(focusModel);
        items.add(0, "newItem");
        assertEquals(initialFocus, oldFocus.getFocusedIndex());
    }
    
    @Test
    public void testSelectionModelReleased() {
        ListView listView = new ListView(items);
        int index = 3;
        listView.getSelectionModel().select(index);
        listView.setSelectionModel(new TestMultipleSelectionModel());
        items.add(0, "newItem");
        assertEquals(listView.getSelectionModel().getSelectedIndex(), listView.getFocusModel().getFocusedIndex());
    }
    
    @Test
    public void testSelectionModelReleasedNoEffect() {
        ListView listView = new ListView(items);
        int index = 3;
        MultipleSelectionModel old = listView.getSelectionModel();
        listView.setSelectionModel(new TestMultipleSelectionModel());
        old.select(index);
        items.add(0, "newItem");
        assertEquals(listView.getSelectionModel().getSelectedIndex(), listView.getFocusModel().getFocusedIndex());
    }

    
    
    /**
     * PENDING JW: unexpected failure on inserting items
     * (ListViewBehaviour _is_ listening to selection changes)
     * Fails with anchor == 4 when we expect 2 ...
     * 
     * Overridden for commenting, only.
     */
    @Override
    public void testAnchorOnInsertItemAbove() {
        super.testAnchorOnInsertItemAbove();
    }

    /**
     * PENDING JW: unexpected failure on removing items
     * (ListViewBehaviour _is_ listening to selection changes)
     * Fails with anchor == 0 when we expect 1 ...
     * 
     * Overridden for commenting, only.
     */
    @Override
    public void testAnchorOnRemoveItemAbove() {
        super.testAnchorOnRemoveItemAbove();
    }

    /**
     * @param multiple
     */
    public ListCoreSingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected ListView createView(ObservableList items) {
        ListView table = new ListView(items);
        MultipleSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        // done in super
//        table.getFocusModel().focus(-1);
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        
        return table;
    }

    @Override
    protected MultipleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }
    
    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    @Override
    protected void setSelectionModel(MultipleSelectionModel model) {
        getView().setSelectionModel(model);
    }

    @Override
    protected void resetItems(ObservableList other) {
        getView().setItems(other);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListCoreSingleSelectionIssues.class.getName());
}
