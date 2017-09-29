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

public interface EditableControl<C extends Control, I extends IndexedCell> {
    void setEditable(boolean editable);
    void setCellFactory(Callback<C, I> factory);
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
    
    default Object getTargetColumn() {
        return null;
    }
}