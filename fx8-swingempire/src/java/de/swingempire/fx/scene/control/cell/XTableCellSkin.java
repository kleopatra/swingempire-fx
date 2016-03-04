/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TableCellBehavior;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.skin.TableCellSkin;
import javafx.scene.control.skin.TableCellSkinBase;

/**
 * Can't - scope of columnVisibility/Width changed to package, no way to override.
 * TabelCellSkinBase rather useless, needed to 
 * 
 * reported: https://bugs.openjdk.java.net/browse/JDK-8148573
 * status: in-progress, there's a patch attached
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTableCellSkin<S,T> extends TableCellSkinBase<TableCell<S,T>> {
    
    private final TableCell<S,T> tableCell;
    private final TableColumn<S,T> tableColumn;
    
    private final BehaviorBase<TableCell<S,T>> behavior;

    public XTableCellSkin(TableCell<S,T> tableCell) {
        super(tableCell);
        
        behavior = new XTableCellBehavior<>(tableCell);
        this.tableCell = tableCell;
        this.tableColumn = tableCell.getTableColumn();
        
    }

//    @Override 
    protected BooleanProperty columnVisibleProperty() {
        return tableColumn.visibleProperty();
    }

//    @Override 
    protected ReadOnlyDoubleProperty columnWidthProperty() {
        return tableColumn.widthProperty();
    }
}
