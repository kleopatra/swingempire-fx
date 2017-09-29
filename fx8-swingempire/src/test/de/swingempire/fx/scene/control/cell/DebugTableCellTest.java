/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import static org.junit.Assert.*;

import de.swingempire.fx.util.EditableControl;
import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DebugTableCellTest extends TableCellTest {

    @Override
    protected void assertValueAt(int index, Object editedValue,
            EditableControl control) {
        fail("tbd: assert edited value");
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return e -> new DebugTextFieldTableCell();
//                (Callback<ListView, ListCell>)TextFieldListCell.forListView();
    }

}
