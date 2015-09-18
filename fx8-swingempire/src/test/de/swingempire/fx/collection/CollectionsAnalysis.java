/*
 * Created on 09.09.2015
 *
 */
package de.swingempire.fx.collection;

import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableListBase;

import com.sun.javafx.collections.ChangeHelper;
import com.sun.javafx.collections.ElementObservableListDecorator;
import com.sun.javafx.collections.ImmutableObservableList;
import com.sun.javafx.collections.ListListenerHelper;
import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.SortHelper;
import com.sun.javafx.collections.SortableList;
import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;


/**
 * Just quick check of content com.sun.javafx.collections
 * @author Jeanette Winzenburg, Berlin
 * @param <T>
 */
public class CollectionsAnalysis<E, F> {

//---------------- in com.sun.javafx.collections
    
    /** version that's truely immutable: content added at instantiation
     *  backed by array of elements 
     *  add/remove listener methods are no-ops
     *  extends AbstractList implements ObservableList with all modification
     *  methods throwing UnsupportedOp
     *  Unsafe though, nothing is final!
     *  Usage: by core, only in BooleanBinding for dependencies.
     */
    ImmutableObservableList<E> il;
    
    /**
     * from-scratch immutable implementation of ObservableList with
     * public method callObservers(Change). Seems to be used 
     * only in MultipleSelectionModelBase? One other usage
     * is in TreeTableViewSkin, some wrapper around treeItems? <p>
     * 
     * This burdens client code with creation of correct Change - the
     * outcome is brittle (see many issues around selectedItems/Indices
     * in MultipleSelectionModelBase and subclasses) 
     * 
     * NOTE: this is not in the collections package but in scene.control
     * and as such NOT a candidate for public access.
     */
    ReadOnlyUnbackedObservableList<E> unbacked;
    
    /**
     * concrete implementation of ModifiableObservableList that is 
     * backed by a List.
     * Implements SortableList - leftover? Not necessarily: 
     * the implementation uses SortHelper to create the permutation
     * just the same as public SortedList does.
     *  
     * PENDING: what happens with the sort on modifications to the list?
     *   probably unsorted after? One-time sort?
     */
    ObservableListWrapper<E> wrapper;
    
    /**
     * Abstract extension of ObservableListWrapper that hooks a listener
     * method into super.
     * 
     * Subclasses implement onChange to be notified when super fires.
     */
    TrackableObservableList<E> trackable;
    
    /**
     * Interface for lists that support effective sorts.
     * 
     * Implemented by ObservableListWrapper.
     */
    SortableList<E> sortable;
    
    /**
     * From scratch implementation of a wrapper around an ObservableList that has 
     * a hook that may veto list modifications by throwing runtimeExceptions as
     * appropriate. The hook is invoked in every modification method. After
     * calling the hook, modifications are delegated to the backing list and
     * changes in the backing list are fired after re-sourceing to self.
     * 
     * The idea (probably?) is to do all modification on the backing list via 
     * the methods of the wrapper, making the backing list essentially invisible
     * and thus immutable/unmodifiable to client code.
     * 
     * Signature of the hook is a bit confusing.
     */
    VetoableListDecorator<E> vetoable;
    
    /**
     * Unused - seems to be a left-over from before extractors were supported by 
     * core.
     */
    ElementObservableListDecorator<E> ol;
    
    /**
     * Implementation of in-place (?) sort of a List which
     * keeps track of the permutations along the sort process.
     */
    SortHelper helper;

    /**
     * Encapsulates registration and notification of list change listeners.
     */
    ListListenerHelper<E> listHelper;
    
    /**
     * Creates string representations of a List change.
     */
    ChangeHelper changeHelper;
    
    MappingChange<E, F> mapping;
    
    /**
     *  Abstract base for specialized Change classes.
     */
    NonIterableChange<E> nonIterable;
    
    //------------- core
    
    /**
     * Base class for all core implementations.
     * Supports listener registration and methods to create 
     * change events.
     * All safe: final methods
     */
    ObservableListBase<E> base;
    
    /**
     * Extends ObservableBase and implements all list modification methods
     * with notification, delegating the actual mods to doXX.
     */
    ModifiableObservableListBase<E> modifiableBase;
    
    
}
