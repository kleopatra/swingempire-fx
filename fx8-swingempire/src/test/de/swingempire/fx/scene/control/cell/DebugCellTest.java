/*
 * Created on 11.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * Use debugging cells instead of core cells.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DebugCellTest extends CellTest {
    
    
    /**
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 2 items
     * and DebugTextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn
     * 
     * @return
     */
    @Override
    protected TableView<TableColumn> createEditableTable() {
        TableView<TableColumn> table = new TableView<>(
                FXCollections.observableArrayList(new TableColumn("first"),
                        new TableColumn("second")));
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(DebugTextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        return table;

    }
    

    /**
     * Creates and returns an editable List configured with 4 items
     * and DebugTextFieldListCell as cellFactory
     * 
     */
    @Override
    protected ListView<String> createEditableList() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(DebugTextFieldListCell.forListView());
        return control;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugCellTest.class.getName());
}
