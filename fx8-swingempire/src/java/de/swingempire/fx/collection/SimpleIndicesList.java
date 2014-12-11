/*
 * Created on 25.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import com.sun.javafx.collections.SortHelper;

/**
 * Unused, will be removed ...
 * 
 * TransformationList that maps positions of the source. 
 * 
 * This implementation uses a simple list as storage of the indices, 
 * optimized implementations would do better (f.i. using a BitSet), 
 * reducing complexity (for me :) here.
 * 
 * @see IndicesList
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class SimpleIndicesList<T> extends TransformationList<Integer, T> {
    protected SortHelper helper = new SortHelper();
    protected List<Integer> indices;
    
    
    public SimpleIndicesList(ObservableList<? extends T> source) {
        super(source);
        indices = new ArrayList<>();
        
    }
    
    /**
     * 
     * @param source
     */
    public SimpleIndicesList(ObservableList<? extends T> source, int... initial) {
        this(source);
        // testing only!
        for (int i = 0; i < initial.length; i++) {
            indices.add(initial[i]);
        }
    }

    @Override
    protected void sourceChanged(Change<? extends T> c) {
        c.next();
        if (!c.wasPermutated()) throw new IllegalStateException("permutations accepted, only");
        beginChange();
        List<Integer> copy = new ArrayList<>(indices);
        for (int i = 0; i < copy.size(); i++) {
            int newValue = c.getPermutation(copy.get(i));
            indices.set(i, newValue);
//            nextSet(i, copy.get(i));
        }
        int[] reverse = helper.sort(indices);
        for (int i = 0; i < indices.size(); i++) {
            nextSet(i, copy.get(reverse[i]));
        }
        endChange();
    }

    @Override
    public int getSourceIndex(int index) {
        return get(index);
    }

    @Override
    public Integer get(int index) {
        return indices.get(index);
    }

    @Override
    public int size() {
        return indices.size();
    }


}
