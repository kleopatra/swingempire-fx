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
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListMultipleSelectionIssues extends MultipleSelectionIssues<ListView, MultipleSelectionModel> {

    public ListMultipleSelectionIssues(boolean multiple) {
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
    protected  MultipleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListMultipleSelectionIssues.class
        .getName());

}