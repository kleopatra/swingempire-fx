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
@SuppressWarnings({ "rawtypes", "unchecked" })
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
    
    /**
     * Pretty prints last change. The change is reset before and 
     * after printing.
     */
    public void prettyPrint() {
        Change c = getLastChange();
        if (c == null) return;
        FXUtils.prettyPrint(c);
        c.reset();
    }
    
    /**
     * Pretty prints all recorded changes, in invers order of having received them
     * that is last is first.
     */
    public void prettyPrintAll() {
        changes.stream().forEach(c -> {
            // not necessary, done by prettyprint
//            c.reset();
           System.out.println("--- change at: " + changes.indexOf(c));
           FXUtils.prettyPrint(c);
           c.reset();
        });
    }
    /**
     * @return the last change, reset if available
     */
    public Change getLastChange() {
        return getLastChange(true);
    }
    
    /**
     * Returns the last change that was received. Resets the change if reset == true,
     * returns it unchanged otherwise.
     * 
     * @param reset
     * @return
     */
    public Change getLastChange(boolean reset) {
        Change c = hasChanges() ? changes.get(0) : null;
        if (c != null && reset) {
            c.reset();
        }
        return c;
    }
    public ObservableList getLastValue() {
        return hasChanges() ? getLastChange().getList() : null;
    }
    
    public boolean hasChanges() {
        return getEventCount() > 0;
    }
    
    public void clear() {
        changes.clear();
    }

}
