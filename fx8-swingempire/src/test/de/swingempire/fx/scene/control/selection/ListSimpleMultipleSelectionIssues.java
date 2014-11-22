/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.javafx.css.SimpleSelector;

import de.swingempire.fx.scene.control.comboboxx.SingleMultipleSelectionModel;
import static org.junit.Assert.*;

/**
 * Testing MultipleSelection api for both modes.
 * 
 * Here we test core ListView configured with SimpleListViewSelectionModel.
 * <p>
 * Note: using focus slave makes all tests for singleSelectionIssuespass 
 * (except the sort-related, which is not
 * yet implemented). Doing the same for MultipleSelectionIssues increases the 
 * failures. WHY?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListSimpleMultipleSelectionIssues extends AbstractListMultipleSelectionIssues<ListView> {

    
    public ListSimpleMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }
 
    @Override
    protected ListView createView(ObservableList items) {
        ListView table = new ListView(items);
        table.setSelectionModel(new SimpleListSelectionModel<>(table));
//        table.setFocusModel(new ListViewAFocusModel<>(table));
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
    public void setUp() throws Exception {
        super.setUp();
//        needsKey = true;
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListSimpleMultipleSelectionIssues.class
        .getName());


    @Override
    protected ListView createEmptyView() {
        return new ListView();
    }

}