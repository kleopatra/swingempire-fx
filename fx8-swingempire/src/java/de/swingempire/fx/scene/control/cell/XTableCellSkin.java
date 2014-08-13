/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import com.sun.javafx.scene.control.skin.TableCellSkinBase;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class XTableCellSkin<S,T> extends TableCellSkinBase<TableCell<S,T>, TableCellBehavior<S,T>> {
    
    private final TableCell<S,T> tableCell;
    private final TableColumn<S,T> tableColumn;
    
    public XTableCellSkin(TableCell<S,T> tableCell) {
        super(tableCell, new XTableCellBehavior<S,T>(tableCell));
        
        this.tableCell = tableCell;
        this.tableColumn = tableCell.getTableColumn();
        
        super.init(tableCell);
    }

    @Override protected BooleanProperty columnVisibleProperty() {
        return tableColumn.visibleProperty();
    }

    @Override protected ReadOnlyDoubleProperty columnWidthProperty() {
        return tableColumn.widthProperty();
    }
}
