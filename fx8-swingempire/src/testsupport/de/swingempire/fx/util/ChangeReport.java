/*
 * Created on 24.05.2014
 *
 */
package de.swingempire.fx.util;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ChangeReport implements ChangeListener {

    List<ObservableValue> sources = new LinkedList<>();
    List<Object> oldValues = new LinkedList<>();
    List<Object> newValues = new LinkedList<>();
    
    ObservableValue source;
    
    public ChangeReport() {
        this(null);
    }

    /**
     * @param object
     */
    public ChangeReport(ObservableValue object) {
        source = object;
        source.addListener(this);
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue,
            Object newValue) {
        sources.add(0, observable);
        oldValues.add(0, oldValue);
        newValues.add(0, newValue);
    }
    
    public int getEventCount() {
        return sources.size();
    }
    
    public Object getLastSource() {
        return sources.size() > 0 ? sources.get(0) : null;
    }
    public Object getLastOldValue() {
        return oldValues.size() > 0 ? oldValues.get(0) : null;
    }
    
    public Object getLastNewValue() {
        return newValues.size() > 0 ? newValues.get(0) : null;
    }
    
    public Object getNewValueAt(int index) {
        return index < newValues.size() ? newValues.get(index) : null;
    }
    
    public List<Pair> getChanges() {
        List<Pair> changes = new LinkedList<>();
        for (int i = 0; i < sources.size(); i++) {
            changes.add(i, new Pair(oldValues.get(i), newValues.get(i)));
        }
        return changes;
    }
    public void clear() {
        sources.clear();
        newValues.clear();
        oldValues.clear();
    }
}
