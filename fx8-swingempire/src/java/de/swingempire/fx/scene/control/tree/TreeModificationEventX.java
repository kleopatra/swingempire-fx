/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;

/**
 * Extended to expose the Change from an item's children list. Note that listeners
 * must be aware of extension if they are listening the changes of the children:
 * super installed internal state regarding removed/added is rather meaningless. 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeModificationEventX<T> extends TreeModificationEvent<T> {

    private Change<? extends TreeItem<T>> change;

    /**
     * @param eventType
     * @param treeItem
     * @param added
     * @param removed
     * @param change
     */
    public TreeModificationEventX(EventType<? extends Event> eventType,
            TreeItem<T> treeItem, List<? extends TreeItem<T>> added,
            List<? extends TreeItem<T>> removed,
            Change<? extends TreeItem<T>> change) {
        super(eventType, treeItem, added, removed, change);
        this.change = change;
    }

    /**
     * This is meanungful only for a single remove
     */
    @Override
    public boolean wasRemoved() {
        if (change == null) 
            return super.wasRemoved();
        boolean hasRemove = false;
        while(change.next()) {
            if (change.wasRemoved()) {
                hasRemove = true;
                break;
            }
        }
        change.reset();
        return hasRemove;
    }


    @Override
    public boolean wasPermutated() {
        if (change == null) {
            return super.wasPermutated();
        }
        boolean wasPermutated = false;
        while (change.next()) {
            if (change.wasPermutated()) {
                wasPermutated = true;
                break;
            }
        }
        change.reset();
        return wasPermutated;
    }


    @Override
    public boolean wasAdded() {
        if (change == null)
        return super.wasAdded();
        boolean wasAdded = false;
        while (change.next()) {
            if (change.wasAdded()) {
                wasAdded = true;
                break;
            }
        }
        change.reset();
        return wasAdded;
    }


    public Change<? extends TreeItem<T>> getChange() {
        return change;
    }
    
    /**
     * @param eventType
     * @param treeItem
     * @param expanded
     */
    public TreeModificationEventX(EventType<? extends Event> eventType,
            TreeItem<T> treeItem, boolean expanded) {
        super(eventType, treeItem, expanded);
    }

    /**
     * @param eventType
     * @param treeItem
     * @param newValue
     */
    public TreeModificationEventX(EventType<? extends Event> eventType,
            TreeItem<T> treeItem, T newValue) {
        super(eventType, treeItem, newValue);
    }

    /**
     * @param eventType
     * @param treeItem
     */
    public TreeModificationEventX(EventType<? extends Event> eventType,
            TreeItem<T> treeItem) {
        super(eventType, treeItem);
    }


}
