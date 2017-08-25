/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.edit;

import de.swingempire.fx.scene.control.edit.impl.TableCellSkin;
import javafx.scene.control.TableCell;

/**
 * A clean inject of cusstom behavior should override the abstract base class.
 * Can't - scope of columnVisibility/Width changed to package, no way to override.
 * Compatibility layer takes over the hacking.
 * 
 * reported: https://bugs.openjdk.java.net/browse/JDK-8148573
 * status: fixed, maybe not yet in public ea
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTableCellSkin<S,T> extends TableCellSkin<S,T> {
    
//    private final TableCell<S,T> tableCell;
//    private final TableColumn<S,T> tableColumn;
//    
//    private final BehaviorBase<TableCell<S,T>> behavior;

    public XTableCellSkin(TableCell<S,T> tableCell) {
        super(tableCell, new XTableCellBehavior<>(tableCell));
        
//        behavior = new XTableCellBehavior<>(tableCell);
//        this.tableCell = tableCell;
//        this.tableColumn = tableCell.getTableColumn();
        
    }

////    @Override 
//    protected BooleanProperty columnVisibleProperty() {
//        return tableColumn.visibleProperty();
//    }
//
////    @Override 
//    protected ReadOnlyDoubleProperty columnWidthProperty() {
//        return tableColumn.widthProperty();
//    }
}
