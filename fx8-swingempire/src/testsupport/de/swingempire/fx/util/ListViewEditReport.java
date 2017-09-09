/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.util;

import java.util.Optional;

import static javafx.scene.control.ListView.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListViewEditReport {

    ListView source;
    
    ObservableList<ListView.EditEvent> editEvents = FXCollections.observableArrayList();
    
    public ListViewEditReport(ListView listView) {
        this.source = listView;
        listView.addEventHandler(ListView.editAnyEvent(), this::addEvent);
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
    
    
    protected void addEvent(ListView.EditEvent event) {
        editEvents.add(0, event);
    }
}
