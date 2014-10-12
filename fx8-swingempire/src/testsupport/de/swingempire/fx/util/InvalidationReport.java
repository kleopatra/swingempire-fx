/*
 * Created on 24.05.2014
 *
 */
package de.swingempire.fx.util;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class InvalidationReport implements InvalidationListener {

    List<Observable> sources = new LinkedList<>();
    
    Observable source;
    
    public InvalidationReport() {
        this(null);
    }

    /**
     * @param object
     */
    public InvalidationReport(Observable object) {
        source = object;
        source.addListener(this);
    }

    @Override
    public void invalidated(Observable observable) {
        sources.add(0, observable);
    }
    
    public int getEventCount() {
        return sources.size();
    }
    
    public Object getLastSource() {
        return sources.size() > 0 ? sources.get(0) : null;
    }
    
    public void clear() {
        sources.clear();
    }
}
