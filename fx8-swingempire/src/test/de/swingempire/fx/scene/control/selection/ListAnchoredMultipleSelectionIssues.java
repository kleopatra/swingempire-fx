/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListAnchoredMultipleSelectionIssues extends AbstractListMultipleSelectionIssues<ListView> {

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
    
    public ListAnchoredMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }
 
    @Override
    protected ListViewAnchored createView(ObservableList items) {
        ListViewAnchored table = new ListViewAnchored(items);
        MultipleSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
    }

    protected AnchoredSelectionModel getAnchoredSelectionModel() {
        return (AnchoredSelectionModel) getSelectionModel();
    }
    @Override
    protected int getAnchorIndex() {
        return getAnchoredSelectionModel().getAnchorIndex();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListAnchoredMultipleSelectionIssues.class
            .getName());
    
}