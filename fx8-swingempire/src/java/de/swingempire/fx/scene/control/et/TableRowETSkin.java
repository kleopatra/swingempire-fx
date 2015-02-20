/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.Cell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import com.sun.javafx.scene.control.skin.TableRowSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowETSkin<T> extends TableRowSkin<T> implements EventTarget {
    
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        TableView<T> tableView = getSkinnable().getTableView();
        TablePosition<T, ?> focused = tableView.getFocusModel().getFocusedCell();
        if (focused != null && focused.getTableColumn() != null) {
            TableColumn<T, ?> column = focused.getTableColumn();
            Cell<?> cell = cellsMap.get(column);
            if (cell != null) {
                cell.buildEventDispatchChain(tail);
            }
        }
        return tail;
    }

//-------------------- boiler-plate constructor
    
    public TableRowETSkin(TableRow<T> tableRow) {
        super(tableRow);
    }

}
