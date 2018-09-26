/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

/**
 * Test MultipleSelection api for both modes.
 * 
 * Here we test a ListViewAnchored configured with SimpleAListSelectionModel 
 * (which is using indicesList/indexMappedItems). 
 * Skin/behaviour relies on the model for anchor handling. <p>
 * 
 * 
 * Hierarchy of SM: <p>
 * AbstractSelectionModel <- SimpleListViewSelectionModel <- SimpleAListViewSelectionModel.
 * 
 * Note: failing tests - might be due to focusModel being slave but simpleA
 * not yet updating correctly.
 * 
 *  
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListSimpleAMultipleSelectionIssues extends AbstractListMultipleSelectionIssues<ListView> {

    
    
//---------- direct tests of action methods in behaviour: access reflectively  

    /**
     * AnchoredSelectionModel allows to anchor the focus. Raw testing.
     */
    @Test
    public void testAnchorToFocus() {
        int focus = 2;
        getFocusModel().focus(focus);
        getAnchoredSelectionModel().anchor();
        assertEquals(focus, getAnchorIndex());
    }
    
    public ListSimpleAMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }
 
    @Override
    protected ListViewAnchored createView(ObservableList items) {
        ListViewAnchored listView = new ListViewAnchored(items);
        listView.setSelectionModel(new SimpleASelectionModel<>(listView));
        listView.setFocusModel(new ListViewAFocusModel<>(listView));
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

    protected AnchoredSelectionModel getAnchoredSelectionModel() {
        return (AnchoredSelectionModel) getSelectionModel();
    }
    @Override
    protected int getAnchorIndex() {
        return getAnchoredSelectionModel().getAnchorIndex();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListSimpleAMultipleSelectionIssues.class
            .getName());

    @Override
    protected ListView createEmptyView() {
        return new ListViewAnchored<>();
    }
    
}