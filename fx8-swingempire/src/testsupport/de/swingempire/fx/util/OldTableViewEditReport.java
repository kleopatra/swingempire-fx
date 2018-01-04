/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.util;

import java.util.Optional;

import static javafx.scene.control.TableColumn.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView.EditEvent;
/**
 * Listener for editEvents on first column.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OldTableViewEditReport {

    TableView source;
    
    ObservableList<CellEditEvent> editEvents = FXCollections.observableArrayList();
    
    /**
     * Instantiates and add an eventHandler for edit events on the first column
     * of the given table.
     * 
     * @param table
     * @throws NullPointerException if table is null or has no columns
     */
    public OldTableViewEditReport(TableView table) {
        this.source = table;
        TableColumn column = (TableColumn) table.getColumns().get(0);
        column.addEventHandler(editAnyEvent(), e -> addEvent((CellEditEvent) e));
    }
    
    /**
     * Returns the list of editEvents as unmodifiable list.
     * @return
     */
    public ObservableList<CellEditEvent> getEditEvents(){
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
    
    public Optional<CellEditEvent> getLastEditStart() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(editStartEvent()))
                .findFirst();
    }
    
    public Optional<CellEditEvent> getLastEditCancel() {
        return editEvents.stream()
                .filter(e -> e.getEventType().equals(editCancelEvent()))
                .findFirst();
    }
    public Optional<CellEditEvent> getLastEditCommit() {
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
    
    public CellEditEvent getLastAnyEvent() {
        return hasEditEvents() ? editEvents.get(0) : null;
    }
    
    public boolean hasEditEvents() {
        return !editEvents.isEmpty();
    }
    
    
    protected void addEvent(CellEditEvent event) {
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
        for (CellEditEvent editEvent : editEvents) {
            edits += getEditEventText(editEvent) + "\n";
        }
        return edits;
    }
    
    public static String getEditEventText(CellEditEvent event) {
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