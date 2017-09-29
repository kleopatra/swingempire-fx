/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.util;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractEditReport<E extends Event> {


    private EditableControl source;
    
    protected ObservableList<E> editEvents = FXCollections.<E>observableArrayList();
    
    public AbstractEditReport(EditableControl listView) {
        this.source = listView;
    }
    
    public EditableControl getSource() {
        return source;
    }
    /**
     * Returns the list of editEvents as unmodifiable list, most recent first.
     * @return
     */
    public ObservableList<E> getEditEvents(){
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
    
    public Optional<E> getLastEditStart() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(source.editStart()))
                .findFirst();
    }
    
    public Optional<E> getLastEditCancel() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(source.editCancel()))
                .findFirst();
    }
    public Optional<E> getLastEditCommit() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(source.editCommit()))
                .findFirst();
    }
    
    /**
     * Returns true if the last event in the received events represents editStart,
     * false otherwise.
     * @return
     */
    public boolean isLastEditStart() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(source.editStart()) : false;
    }
    
    /**
     * Returns true if the last event in the received events represents editCommit,
     * false otherwise.
     * @return
     */
    public boolean isLastEditCommit() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(source.editCommit()) : false;
    }
    
    /**
     * Returns true if the last event in the received events represents editCancel,
     * false otherwise.
     * @return
     */
    public boolean isLastEditCancel() {
        return hasEditEvents() ? getLastAnyEvent().getEventType().equals(source.editCancel()) : false;
    }
    
    public E getLastAnyEvent() {
        return hasEditEvents() ? editEvents.get(0) : null;
    }
    
    public boolean hasEditEvents() {
        return !editEvents.isEmpty();
    }
    
    
    protected void addEvent(E event) {
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
        for (E editEvent : editEvents) {
            edits += getEditEventText(editEvent) + "\n";
        }
        return edits;
    }
    
    public abstract String getEditEventText(E event); 
    
}
