/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.util;

import javafx.event.Event;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.util.Callback;

/**
 * Decorator for editable virtualized controls. Useful
 * in testing cells and their editing behaviour.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public interface EditableControl<C extends Control, I extends IndexedCell> {
    void setEditable(boolean editable);
    void setCellFactory(Callback<C, I> factory);
    void fireEvent(Event ev);
    EventHandler getOnEditCommit();
    EventHandler getOnEditCancel();
    EventHandler getOnEditStart();
    void setOnEditCommit(EventHandler handler);
    void setOnEditCancel(EventHandler handler);
    void setOnEditStart(EventHandler handler);
    <T extends Event> void addEditEventHandler(EventType<T> type, EventHandler<? super T> handler);
    EventType editCommit();
    EventType editCancel();
    EventType editStart();
    EventType editAny();
    
    C getControl();
    int getEditingIndex();
    void edit(int index);
    
    /**
     * Returns the value at index if targetColumn is null or at index and targetColumn if not.
     * 
     * @param index
     * @return
     */
//    Object getValueAt(int index);
    default Object getTargetColumn() {
        return null;
    }
}