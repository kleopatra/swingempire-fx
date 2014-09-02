/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import fx.util.FXUtils;

import static org.junit.Assert.*;

/**
 * Testing singleselection api in ListViewSelectionModel in both selection modes.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListSingleSelectionIssues extends SingleSelectionIssues<ListView, MultipleSelectionModel> {

    
    @Test
    public void testDummy() {
        
    }
    /**
     * Anchor not set on selecting 
     * 
     * Note: anchor testing doesn't make sense here - it's controlled by behaviour which is 
     * part of skin which is not yet installed after instantiation ... 
     * 
     * ListViewBehaviour _is_ listening to selection changes and updates the anchor as
     * needed (not quite right, but at least it does) but without skin it doesn't show
     * Yet another reason for having the anchor at the model?
     * 
     * Trying the paintPulse approach .. hangs.
     */
//    @Override
//    public void testAnchor() {
//        int index = 2;
//        Scene scene = new Scene(getView());
//        getSelectionModel().getSelectedIndices().addListener((Change c) -> {
//            FXUtils.prettyPrint(c);
//        });
////        FXUtils.waitForPaintPulse();
//        FXUtils.runThenWaitForPaintPulse(() -> {
//            LOG.info("has skin? " + getView().getSkin());
//            getSelectionModel().select(index);
//        }) ;
//        assertEquals("anchor must be same as selected index", index, getAnchorIndex(index));
//    }

    /**
     * @param multiple
     */
    public ListSingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected ListView createView(ObservableList items) {
        ListView table = new ListView(items);
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
    protected MultipleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    
    
    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListSingleSelectionIssues.class.getName());

}
