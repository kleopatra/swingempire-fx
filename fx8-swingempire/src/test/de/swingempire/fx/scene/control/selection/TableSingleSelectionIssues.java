/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TableViewBuilder;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Testing single selection api in TableViewSelectionModel, for both selection modes.
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TableSingleSelectionIssues extends SingleSelectionIssues<TableView, MultipleSelectionModel> {

    /**
     * @param multiple
     */
    public TableSingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected TableView createView(ObservableList items) {
        TableView table = new TableView(items);
        TableColumn column = new TableColumn("numberedItems");
        table.getColumns().add(column);
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
        MultipleSelectionModel model = getView().getSelectionModel();
        return model;
    }

    
    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    

    @Override
    protected int getAnchorIndex(int index) {
        TablePosition anchor = (TablePosition) getView().getProperties().get(ANCHOR_KEY);
        return anchor != null ? anchor.getRow() : -1;
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableSingleSelectionIssues.class.getName());



    @Override
    protected void setSelectionModel(MultipleSelectionModel model) {
        getView().setSelectionModel((TableViewSelectionModel) model);
    }

    @Override
    protected void resetItems(ObservableList other) {
        getView().setItems(other);
    }
}
