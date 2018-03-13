/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.util;

import java.util.LinkedList;
import java.util.List;

import de.swingempire.fx.scene.control.tree.TreeModificationEventX;
import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeModificationReport implements EventHandler<TreeModificationEvent>{

    List<TreeModificationEvent> events = new LinkedList<>();
    public TreeModificationReport() {
        this(null);
    }
    
    public TreeModificationReport(TreeItem source) {
        if (source != null) {
            source.addEventHandler(TreeItem.treeNotificationEvent(), this);
        }
    }

    @Override
    public void handle(TreeModificationEvent event) {
        events.add(0, event);
    }
    
    public int getEventCount() {
        return events.size();
    }
    
    public boolean hasEvents() {
        return getEventCount() > 0;
    }
    
    public TreeModificationEvent getLastEvent() {
        return hasEvents() ? events.get(0) : null;
    }

    public Change getLastChange() {
        return getLastChange(true);
    }
    
    public Change getLastChange(boolean reset) {
        Change change = getLastChangeX(reset);
        if (change == null && !canHaveChangeX(getLastEvent())) {
            // fall back to reflection
            change = invokeGetChange(getLastEvent());
        }
        return change;
    }
    
    /**
     * @param lastEvent the event to access the change from, must not be null
     * @return the change notification from the children list to its parent, might be
     *    null if any of the event constructors without change was used
     */
    private Change invokeGetChange(TreeModificationEvent lastEvent) {
        return (Change) FXUtils.invokeGetMethodValue(TreeModificationEvent.class, lastEvent, "getChange");
    }

    /**
     * Returns and resets the change of the last event, or null
     * if not supported/available.
     *  
     * @return
     */
    public Change getLastChangeX() {
        return getLastChangeX(true);
    }
    
    /**
     * returns the change of the last event or null if not supported.
     * The change is reset or not according to the given flag.
     * 
     * @param reset
     * @return
     */
    public Change getLastChangeX(boolean reset) {
        if (!canHaveChangeX(getLastEvent())) {
            return null;
        }
        Change change = ((TreeModificationEventX) getLastEvent()).getChange();
        if (reset && change != null) {
            change.reset();
        }
        return change;
    }

    public boolean wasAdded(TreeModificationEvent event) {
        return event.wasAdded();
    }
    /**
     * @param lastEvent
     * @return
     */
    private boolean canHaveChangeX(TreeModificationEvent lastEvent) {
        return lastEvent instanceof TreeModificationEventX;
    }
    
    
}
