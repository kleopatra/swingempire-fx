/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.util;

import static javafx.scene.control.TableColumn.*;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewEditReport extends AbstractEditReport<CellEditEvent> {

    /**
     * @param listView
     */
    public TableViewEditReport(EditableControl listView) {
        super(listView);
        listView.addEditEventHandler(listView.editAny(), e -> addEvent((CellEditEvent) e));
    }

    @Override
    public String getEditEventText(CellEditEvent event) {
        // table, tablePosition (aka: row/column), eventType, newValue
        TablePosition pos = event.getTablePosition();
        TableColumn column = pos != null ? event.getTableColumn() :null;
        int row = pos != null ? pos.getRow() : -1;
        Object oldValue = pos != null ? event.getOldValue() : null;
        Object rowValue = pos != null ? event.getRowValue() : null;
        return "[tableViewEditEvent [ type: " + event.getEventType() + " pos: " + pos + " rowValue: " + rowValue + " oldValue: " 
                + oldValue + " newValue: " + event.getNewValue();
      
    }

}
