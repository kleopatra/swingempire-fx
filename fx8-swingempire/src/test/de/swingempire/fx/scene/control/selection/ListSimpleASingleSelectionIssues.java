/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Testing singleselection api in ListViewSelectionModel in both selection modes.
 * 
 * Here we test a ListViewAnchored configured with SimpleAListSelectionModel 
 * (which is using indicesList/indexMappedItems). 
 * Skin/behaviour relies on the model for anchor handling. <p>
 * 
 * Note: failing tests - might be due to focusModel being slave but simpleA
 * not yet updating correctly.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListSimpleASingleSelectionIssues extends SingleSelectionIssues<ListView, MultipleSelectionModel> {


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
    public ListSimpleASingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected ListViewAnchored createView(ObservableList items) {
        ListViewAnchored listView = new ListViewAnchored(items);
        listView.setSelectionModel(new SimpleASelectionModel<>(listView));
        // disable default selection
        listView.getProperties().put("selectFirstRowByDefault", Boolean.FALSE);
        // initial focus on 0 (as of 8u40b9), force into unfocused
        // done in super
//        listView.getFocusModel().focus(-1);
        MultipleSelectionModel model = listView.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        
        return listView;
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
    protected int getAnchorIndex(int index) {
        return ((AnchoredSelectionModel) getSelectionModel()).getAnchorIndex();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListSimpleASingleSelectionIssues.class.getName());

    @Override
    protected void setSelectionModel(MultipleSelectionModel model) {
        getView().setSelectionModel(model);
    }

    @Override
    protected void resetItems(ObservableList other) {
        getView().setItems(other);
    }

}
