/*
 * Created on 09.03.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.skin.ListCellSkin;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.control.skin.TreeTableRowSkin;

/**
 * Divers tests around all cell types.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CellTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    /**
     * Test about treeTableRowSkin: registers
     * a listener on the treeTableView treeColumn in constructor 
     * - throws if not yet bound to a treeTable
     * 
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8151524
     */
    @Test
    public void testTreeTableRowSkinInit() {
        TreeTableRow row = new TreeTableRow();
        row.setSkin(new TreeTableRowSkin(row));
    }
    
    @Test
    public void testListCellSkinInit() {
        ListCell cell = new ListCell();
        cell.setSkin(new ListCellSkin(cell));
    }
    
    @Test
    public void testTreeCellSkin() {
        TreeCell cell = new TreeCell();
        cell.setSkin(new TreeCellSkin(cell));
    }

}
