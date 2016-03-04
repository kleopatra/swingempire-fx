/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import com.sun.javafx.event.EventHandlerManager;

import de.swingempire.fx.util.FXUtils;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * PENDING JW: flow not accessible, hacked reflectively.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewETSkin<S> extends TableViewSkin<S> implements EventTarget {

    VirtualFlow flowAlias;
    EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        TablePosition<S, ?> focused = getSkinnable().getFocusModel().getFocusedCell();// jdk9: no longer visible getFocusedCell();
        if (focused != null) {
            TableColumn<S, ?> column = focused.getTableColumn();
            if (column != null) {
                column.buildEventDispatchChain(tail);
                // PENDING delegate without nesting
                int row = focused.getRow();
                if (row > -1) {
                    IndexedCell<S> rowCell = getFlow().getCell(row);
                    rowCell.buildEventDispatchChain(tail);
                }
            }
        }
//        return tail.prepend(eventHandlerManager);
        return tail;
    }

    
    private VirtualFlow getFlow() {
        if (flowAlias == null) {
            flowAlias = (VirtualFlow) FXUtils.invokeGetFieldValue(TableViewSkinBase.class, this, "flow");
        }
        return flowAlias;
    }
//---------------- boiler-plate constructor
    
    public TableViewETSkin(TableView<S> tableView) {
        super(tableView);
    }

}
