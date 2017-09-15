/*
 * Created on 15.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

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
    public void testListEditStartOnCellStandalone() {
        // TODO Auto-generated method stub
//        super.testListEditStartOnCellStandalone();
    }

    @Override
    protected Callback<ListView<String>, ListCell<String>> createTextFieldListCell() {
        return DebugTextFieldListCell.forListView();
    }


}
