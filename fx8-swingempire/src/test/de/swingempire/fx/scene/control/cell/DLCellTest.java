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
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;

/**
 * Test DebugListCell
 * Initially copied all from 
 * DebugCellTest, then deleted all tests that are not listCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DLCellTest extends LCellTest {

    
    /**
     * no-op here
     */
    @Override
    public void testListEditStartOnBaseCellTwiceStandalone() {
    }

    /**
     * no-op here
     */
    @Override
    public void testListEditStartOnCellTwiceStandalone() {
    }

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
        ListView control = new ListView(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4")) {
            
        };
        control.setEditable(true);
        control.setCellFactory(createTextFieldCellFactory());

    }

//    @Override
//    protected Callback<ListView<String>, ListCell<String>> createTextFieldListCell() {
//        return DebugTextFieldListCell.forListView();
//    }
//
    @SuppressWarnings("rawtypes")
    @Override
    protected Callback<ListView, ListCell> createTextFieldCellFactory() {
        return e -> new DebugTextFieldListCell();
//                (Callback<ListView, ListCell>)DebugTextFieldListCell.forListView();
    }


}
