/*
 * Created on 14.12.2014
 *
 */
package de.swingempire.fx.collection;

import javafx.collections.ObservableListBase;

/**
 * Nothing here yet, just notes. <p>
 * 
 * PENDING JW: planned to extract the BitSet backing into a base list, then extend
 * to be backed by a list or a TreeView (outch ..). The IndicesBase would 
 * provide the set/add/clearIndices and supporting infrastructure, the extending
 * classes would handle the updates triggered by notifications of the backing
 * data structure.
 * <p>
 * Cannot use a Transform for it,
 * but doesn't matter that much, see TreeIndicesList (where we copied all the bitSet
 * related functionality, so far). 
 * 
 * <p>
 * 
 * Design/Implementation:
 * <li> IndicesBase is-a kind-of transfrom of a observable sequential indexable backing data structure
 * <li> IndicesBase is-an unmodifiable ObservableList of Integers
 * <li> IndicesBase has api to set/add/clear indices as valid by the backing structure
 * <li> concrete subclasses listen to changes in the backing data and update the indices as
 *     appropriate
 * 
 * @see IndicesList
 * @see TreeIndicesList
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IndicesBase<T> extends ObservableListBase<Integer> {

    @Override
    public Integer get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

}
