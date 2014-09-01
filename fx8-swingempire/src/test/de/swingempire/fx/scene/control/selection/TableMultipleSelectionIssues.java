/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import static org.junit.Assert.*;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TableViewBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TableMultipleSelectionIssues extends MultipleSelectionIssues<TableView, TableViewSelectionModel> {

    public TableMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Test
    public void testTablePosition() {
        TableView table = getView();
        TablePosition pos = new TablePosition(table, 0, null);
        assertEquals(pos, new TablePosition(table, 0, null));
    }
    
    @Test
    public void testSanity() {
        TableView table = getView();
        assertEquals(1, table.getColumns().size());
        assertEquals(items, table.getItems());
    }

    
    @Override
    protected TableViewSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    /**
     * 
     */

    @Override
    protected TableView createView(ObservableList items) {
        TableView table = TableViewBuilder
                .create()
                .items(items)
                .columns(
                        TableColumnBuilder.create().text("numberedItems")
//                        .cellValueFactory(factory)
                                .build())
                .build();
        TableViewSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
    }

}
