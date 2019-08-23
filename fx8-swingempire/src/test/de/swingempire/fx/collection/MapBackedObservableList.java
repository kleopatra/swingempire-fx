/*
 * Created on 23.08.2019
 *
 */
package de.swingempire.fx.collection;

import javafx.collections.MapChangeListener.Change;

import java.util.Iterator;

import static org.junit.Assert.*;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableListBase;
import javafx.collections.ObservableMap;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class MapBackedObservableList<K, E> extends ObservableListBase<E> {

    private ObservableMap<K, E> map;
    
    public MapBackedObservableList(ObservableMap<K, E> backingMap) {
        this.map = backingMap;
        map.addListener(this::mapChanged);
    }

    protected void mapChanged(Change<? extends K, ? extends E> c) {
        
    }
    
    @Override
    public E get(int index) {
        Iterator<E> it = map.values().iterator();
        int loc = 0;
        while (it.hasNext()) {
            E next = it.next();
            if (loc == index) {
                return next;
            }
            loc++;
        }
        return null;
    }

    @Override
    public int size() {
        return map.size();
    }
    
    

}
