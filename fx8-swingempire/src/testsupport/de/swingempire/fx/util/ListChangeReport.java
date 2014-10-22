/*
 * Created on 26.09.2014
 *
 */
package de.swingempire.fx.util;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.value.ObservableListValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ListChangeReport implements ListChangeListener {

    List<Change> changes = new LinkedList<>();
    
    ObservableList source;
    
    public ListChangeReport() {
        this(null);
    }
    public ListChangeReport(ObservableList source) {
        this.source = source;
        if (source != null) source.addListener(this);
    }
    
    @Override
    public void onChanged(Change c) {
        changes.add(0, c);
    }
    
    public int getEventCount() {
        return changes.size();
    }
    
    public Change getLastListChange() {
        return hasChanges() ? changes.get(0) : null;
    }
    
    public ObservableList getLastListValue() {
        return hasChanges() ? getLastListChange().getList() : null;
    }
    public boolean hasChanges() {
        return getEventCount() > 0;
    }

}
