/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.control.skin.TableRowSkin;
import com.sun.javafx.scene.control.skin.TableViewSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewETSkin<S> extends TableViewSkin<S> implements EventTarget {

    EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        TablePosition<S, ?> focused = getFocusedCell();
        if (focused != null) {
            TableColumn<S, ?> column = focused.getTableColumn();
            if (column != null) {
                column.buildEventDispatchChain(tail);
                // PENDING delegate without nesting
                int row = focused.getRow();
                if (row > -1) {
                    IndexedCell<S> rowCell = flow.getCell(row);
                    rowCell.buildEventDispatchChain(tail);
                }
            }
        }
//        return tail.prepend(eventHandlerManager);
        return tail;
    }

//---------------- boiler-plate constructor
    
    public TableViewETSkin(TableView<S> tableView) {
        super(tableView);
    }

}
