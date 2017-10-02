/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DebugTableCellTest extends TableCellTest {

    /**
     * @param cellSelection
     */
    public DebugTableCellTest(boolean cellSelection) {
        super(cellSelection);
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return e -> new DebugTextFieldTableCell();
    }

}
