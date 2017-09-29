/*
 * Created on 15.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import org.junit.Test;

import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Obsolete - refactored test/tool hierarchy to simplify.
 * 
 * Divers tests around all listCell types. Initially copied all from 
 * DebugCellTest, then deleted all tests that are not listCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugListCellTest extends ListCellTest {

 
    
    @Test
    public void testTyping() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4")) {
            
        };
        control.setEditable(true);
        control.setCellFactory(createTextFieldListCell());

    }

    @Override
    protected Callback<ListView<String>, ListCell<String>> createTextFieldListCell() {
        return DebugTextFieldListCell.forListView();
    }


}
