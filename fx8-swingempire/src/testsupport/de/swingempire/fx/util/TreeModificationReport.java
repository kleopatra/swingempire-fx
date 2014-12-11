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

    /**
     * Returns and resets the change of the last event, or null
     * if not supported/available.
     *  
     * @return
     */
    public Change getLastChange() {
        return getLastChange(true);
    }
    
    /**
     * returns the change of the last event or null if not supported.
     * The change is reset or not according to the given flag.
     * 
     * @param reset
     * @return
     */
    public Change getLastChange(boolean reset) {
        if (!canHaveChange(getLastEvent())) {
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
    private boolean canHaveChange(TreeModificationEvent lastEvent) {
        return lastEvent instanceof TreeModificationEventX;
    }
    
    
}
