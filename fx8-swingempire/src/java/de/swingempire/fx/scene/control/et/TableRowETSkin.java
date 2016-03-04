/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import java.lang.ref.Reference;
import java.util.WeakHashMap;

import com.sun.javafx.event.EventHandlerManager;

import de.swingempire.fx.util.FXUtils;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.Cell;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.control.skin.TableRowSkinBase;

/**
 * PENDING JW: cellMaps not accessible in java9. Nor getCell(column) - not
 * included in patch of https://bugs.openjdk.java.net/browse/JDK-8148573
 * 
 * Added reflective hack for now.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowETSkin<T> extends TableRowSkin<T> implements EventTarget {

    
    EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        TableView<T> tableView = getSkinnable().getTableView();
        TablePosition<T, ?> focused = tableView.getFocusModel().getFocusedCell();
        if (focused != null && focused.getTableColumn() != null) {
            TableColumn<T, ?> column = focused.getTableColumn();
            Cell<?> cell = getCellFromCellsMap(column);
            if (cell != null) {
                cell.buildEventDispatchChain(tail);
            }
        }
//        return tail.prepend(eventHandlerManager);
        return tail;
    }

    /**
     * @param column
     * @return
     */
    private Cell<?> getCellFromCellsMap(TableColumn<T, ?> column) {
        
        Reference<IndexedCell> cellReference = getCellsMap().get(column);
        Cell<?> cell = cellReference != null ? cellReference.get() : null;
        return cell;
    }

    private WeakHashMap<TableColumnBase, Reference<IndexedCell>> cellsMapAlias;
    private WeakHashMap<TableColumnBase, Reference<IndexedCell>> getCellsMap() {
        if (cellsMapAlias == null) {
            cellsMapAlias = (WeakHashMap<TableColumnBase, Reference<IndexedCell>>) FXUtils.invokeGetFieldValue(TableRowSkinBase.class, this, "cellsMap");
        }
        return cellsMapAlias;
    }
//-------------------- boiler-plate constructor
    
    public TableRowETSkin(TableRow<T> tableRow) {
        super(tableRow);
    }

}
