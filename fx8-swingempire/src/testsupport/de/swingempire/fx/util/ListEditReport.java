/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.util;

import de.swingempire.fx.scene.control.cell.AbstractCellTest.EditableControl;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListEditReport extends AbstractEditReport<ListView.EditEvent> {

    /**
     * @param listView
     */
    public ListEditReport(EditableControl listView) {
        super(listView);
        listView.addEditEventHandler(listView.editAny(), e -> addEvent((EditEvent) e));
    }

    @Override
    public String getEditEventText(ListView.EditEvent event) {
      return "[ListViewEditEvent [type: " + event.getEventType() + " index " 
              + event.getIndex() + " newValue " + event.getNewValue() + "]";
      
    }

}
