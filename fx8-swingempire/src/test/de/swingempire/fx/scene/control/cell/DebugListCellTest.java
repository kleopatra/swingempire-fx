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
 * Test DebugListCell
 * Initially copied all from 
 * DebugCellTest, then deleted all tests that are not listCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DebugListCellTest extends ListCellTest {

    
    @Test
    public void testTyping() {
        ListView control = new ListView(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4")) {
            
        };
        control.setEditable(true);
        control.setCellFactory(createTextFieldCellFactory());

    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Callback<ListView, ListCell> createTextFieldCellFactory() {
        return e -> new DebugTextFieldListCell();
//                (Callback<ListView, ListCell>)DebugTextFieldListCell.forListView();
    }


}