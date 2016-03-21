/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import com.sun.javafx.scene.control.skin.TableCellSkinBase;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellSkin<S, T> extends TableCellSkinBase<TableCell<S, T>, TableCellBehavior<S, T>> {

    private final TableCell<S,T> tableCell;
    private final TableColumn<S,T> tableColumn;
    
    /**
     * @param control
     * @param behavior
     */
    public TableCellSkin(TableCell<S, T> control,
            TableCellBehavior<S, T> behavior) {
        super(control, behavior);
        this.tableCell = control;
        this.tableColumn = tableCell.getTableColumn();
    }

    @Override protected BooleanProperty columnVisibleProperty() {
        return tableColumn.visibleProperty();
    }

    @Override protected ReadOnlyDoubleProperty columnWidthProperty() {
        return tableColumn.widthProperty();
    }

}
