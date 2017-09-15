/*
 * Created on 11.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Use debugging cells instead of core cells.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DebugTableCellTest extends TableCellTest {
    
    

    @Override
    protected Callback<TableColumn<TableColumn, String>, TableCell<TableColumn, String>> createTextFieldTableCell() {
        return DebugTextFieldTableCell.forTableColumn();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugTableCellTest.class.getName());
}
