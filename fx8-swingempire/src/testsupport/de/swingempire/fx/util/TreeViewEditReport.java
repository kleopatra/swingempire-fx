/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.util;

import java.util.Optional;

import static javafx.scene.control.TreeView.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeView;
/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeViewEditReport {

    private TreeView source;
    
    private ObservableList<EditEvent> editEvents = FXCollections.observableArrayList();
    
    public TreeViewEditReport(TreeView listView) {
        this.source = listView;
        listView.addEventHandler(editAnyEvent(), this::addEvent);
    }
    
    /**
     * Returns the list of editEvents as unmodifiable list, most recent first.
     * @return
     */
    public ObservableList<EditEvent> getEditEvents(){
        return FXCollections.unmodifiableObservableList(editEvents);
    }
    /**
     * Clears list of received events. 
     */
    public void clear() {
        editEvents.clear();
    }
    
    public int getEditEventSize() {
        return editEvents.size();
    }
    
    public Optional<EditEvent> getLastEditStart() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(editStartEvent()))
                .findFirst();
    }
    
    public Optional<EditEvent> getLastEditCancel() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(editCancelEvent()))
                .findFirst();
    }
    public Optional<EditEvent> getLastEditCommit() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(editCommitEvent()))
                .findFirst();
    }
    
    /**
     * Returns true if the last event in the received events represents editStart,
     * false otherwise.
     * @return
     */
    public boolean isLastEditStart() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(editStartEvent()) : false;
    }
    
    /**
     * Returns true if the last event in the received events represents editCommit,
     * false otherwise.
     * @return
     */
    public boolean isLastEditCommit() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(editCommitEvent()) : false;
    }
    
    /**
     * Returns true if the last event in the received events represents editCancel,
     * false otherwise.
     * @return
     */
    public boolean isLastEditCancel() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(editCancelEvent()) : false;
    }
    
    public EditEvent getLastAnyEvent() {
        return hasEditEvents() ? editEvents.get(0) : null;
    }
    
    public boolean hasEditEvents() {
        return !editEvents.isEmpty();
    }
    
    
    protected void addEvent(EditEvent event) {
        editEvents.add(0, event);
    }
    
    /**
     * Returns the enhanced edit text of all events received, most 
     * recent first.
     * 
     * @param message
     * @return
     */
    public String getAllEditEventTexts(String message) {
        if (!hasEditEvents()) return "noEvents";
        String edits = message + "\n";
        for (EditEvent editEvent : editEvents) {
            edits += getEditEventText(editEvent) + "\n";
        }
        return edits;
    }
    
    public static String getEditEventText(EditEvent event) {
        return "[TreeViewEditEvent [type: " + event.getEventType() + " treeItem " 
                + event.getTreeItem() + " newValue " + event.getNewValue() + "]";
        
    }
}
