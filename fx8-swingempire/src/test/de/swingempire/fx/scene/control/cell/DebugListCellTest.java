/*
 * Created on 15.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import org.junit.Test;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Divers tests around all listCell types. Initially copied all from 
 * DebugCellTest, then deleted all tests that are not listCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugListCellTest extends ListCellTest {

    
    /**
     * no-op here
     */
    @Override
    public void testBaseNullControlOnStartEditStandalone() {
    }

    /**
     * no-op here
     */
    @Override
    public void testTextFieldCellNullControlOnStartEditStandalone() {
    }

    /**
     * no-op here
     */
    @Override
    public void testListEditStartOnCellStandalone() {
    }
    
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
