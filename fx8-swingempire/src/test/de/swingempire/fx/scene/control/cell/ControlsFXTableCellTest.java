/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.util.Callback;

/**
 * Testing controlsFX Textfield2TableCell.
 * Same errors as core - no wonder, it _is_ core plus some listener magic to by-pass
 * cancels.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ControlsFXTableCellTest extends TableCellTest {

    /**
     * @param cellSelection
     */
    public ControlsFXTableCellTest(boolean cellSelection) {
        super(cellSelection);
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return TextField2TableCell.forTableColumn();
    }

}
