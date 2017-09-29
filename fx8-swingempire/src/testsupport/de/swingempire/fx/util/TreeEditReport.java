/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.util;

import de.swingempire.fx.scene.control.cell.AbstractCellTest.EditableControl;
import javafx.scene.control.TreeView.EditEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeEditReport extends AbstractEditReport<EditEvent> {

    /**
     * @param listView
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public TreeEditReport(EditableControl listView) {
        super(listView);
        listView.addEditEventHandler(listView.editAny(), e -> addEvent((EditEvent) e));
    }

    @Override
    public String getEditEventText(EditEvent event) {
        return "[TreeViewEditEvent [type: " + event.getEventType() + " treeItem " 
                + event.getTreeItem() + " newValue " + event.getNewValue() + "]";
    }


}
