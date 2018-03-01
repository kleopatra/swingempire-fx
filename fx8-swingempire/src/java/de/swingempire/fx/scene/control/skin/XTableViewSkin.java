/*
 * Created on 09.02.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.skin.patch.TableViewSkin;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 * fx-9/8 compatibility - Decision: wait for bug fix bubbling up into ea! 
 * --------
 * <ul>
 * 
 * <li> commented: Trying to update the editing state after a layout pass - not working.
 * </ul>
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked" })
public class XTableViewSkin<T> extends TableViewSkin<T> {

//    private VirtualFlow<TableRow<T>> flowAlias;

    public XTableViewSkin(TableView<T> control) {
        super(control);
//        flowAlias = (VirtualFlow<IndexedCell<T>>) FXUtils.invokeGetMethodValue(VirtualContainerBase.class, this, "getVirtualFlow");
    }
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
//        updateEditingCell();
    }
    
    /**
     * Can't: it's package private in fx-9. Can't inject, the field is
     * final. Hmmm... 
     * 
     * Can't do anything flow-specific in this concrete class - would
     * break compatibility.
     * @return
     */
//    @Override
//    protected VirtualFlow<TableRow<T>> createVirtualFlow() {
//        flowAlias = new VirtualFlow<>();
//        return flowAlias;
//    }
    /**
     * Meant to start a pending edit, if any, after a layout pass.
     * 
     * Using the sequence edit(-1), edit(old editing) works a bit:
     * Cell editing, but 
     * - not necessarily focused at once - how to?
     * - sometimes incorrect cell, why? Well, might be the editingCell
     * is ill-defined across list mutations ...
     * 
     * Note that we are cancelling any ongoing edit, not nice!
     * 
     * Trying to find a "real" cell and start editing on it doesn't
     * work at all.
     */
    @SuppressWarnings("rawtypes")
    private void updateEditingCell() {
        TablePosition editingCell = getSkinnable().getEditingCell();
        LOG.info("editing after add " + editingCell);
        if (editingCell == null) return;
        // nearly works, how to grab focus onto the editing component?
        getSkinnable().edit(-1, null);
        getSkinnable().edit(editingCell.getRow(), editingCell.getTableColumn());
        // trying to find the target cell then startEdit doesn't work
//        TableRow tableRow = (TableRow) flowAlias.getCell(editingCell.getRow());
//        if (tableRow == null) return;
//        if (!(tableRow.getSkin() instanceof TableRowSkin)) return;
//        TableRowSkin rowSkin = (TableRowSkin) tableRow.getSkin();
//        TableCell cell = (TableCell) FXUtils.invokeGetMethodValue(
//                TableRowSkin.class, rowSkin, "getCell", TableColumnBase.class, editingCell.getTableColumn());
//        cell.startEdit();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XTableViewSkin.class.getName());

}
