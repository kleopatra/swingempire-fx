/*
 * Created on 20.09.2014
 *
 */
package de.swingempire.fx.property;

import java.util.List;
import java.util.Objects;

import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

/**
 * A property that is bound to a child Observable of a root. Manages 
 * its own binding on changes of root and/or child as appropriate.
 *
 * Use-case: 
 * 
 * We need to doStuff based on the value of selectedItemProperty of a ListView's
 * selectionModel. In manual coding we would need to 
 * - wire to the current selectedItemProperty and doStuff() on change notification
 * - wire to the selectionModelProperty of the ListView and re-wire the former
 *   on changes of the model
 * 
 * A typical code snippet:
 * 
 * <code><pre>
 * ChangeListener modelPropertyListener = (source, oldModel, newModel) ->
 *    updateSelectionModel(oldModel, newModel);
 * ChangeListener itemListener = (source, oldItem, newItem) ->
 *    doStuff();
 * private void updateSelectionModel(oldModel, newModel) {
 *     if (oldModel != null) {
 *        oldModel.selectedItemProperty().removeListener(itemListener);
 *     }   
 *     if (newModel != null) {
 *        newModel.selectedItemProperty().addListener(itemListener); 
 *     }
 *     doStuff();     
 * }   
 * </pre></code>
 * 
 * A PathProperty completely takes over the re/-wiring, client code registers
 * itself to the path:
 * 
 * PathProperty path = new PathProperty(listView.selectionModelProperty(),
 *    model -> model.selectedItemProperty());
 * path.addListener(ov -> doStuff());    
 * 
 * PENDING JW: here the child is-a ObservableValue only - so it's not really
 * a Property, just convenient to use the internal bind handling. How to
 * do it cleanly?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ObservablePathAdapter<S, T> extends ObjectPropertyBase<T> {

    private T defaultValue;
    private Property<S> root;
    private Callback<S, ObservableValue<T>> childFactory;
    private ChangeListener<? super S> rootListener;
    
    /**
     * Instantiates a path with null root and the given factory for
     * accessing the child observable. The factory is accessed only
     * if the root isn't null.
     * 
     * @param factory the factory for accesing the child property, must not
     *    be null
     */
    public ObservablePathAdapter(Callback<S, ObservableValue<T>> factory) {
        this(null, factory);
    }
    
    /**
     * 
     * @param factory the factory for accesing the child property, must not
     *    be null
     * @param defaultValue value to set if root is null
     */
    public ObservablePathAdapter(Callback<S, ObservableValue<T>> factory, T defaultValue) {
        this(null, factory, defaultValue);
    }
    
    public ObservablePathAdapter(Property<S> root, Callback<S, ObservableValue<T>> factory) {
        this(root, factory, null);
    }
    
    /**
     * Instantiates a PathProperty with root, factory and defaultValue.
     * 
     * @param root the root property, may be null
     * @param factory the factory for accesing the child property, must not
     *    be null
     * @param defaultValue value to set if root is null
     */
    public ObservablePathAdapter(Property<S> root, Callback<S, ObservableValue<T>> factory, T defaultValue) {
        this.childFactory = Objects.requireNonNull(factory);
        setDefaultValue(defaultValue);
        setRoot(root);
    }
    
    /**
     * @param defaultValue
     */
    private void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        set(defaultValue);
    }

    /**
     * Sets the root property.
     * 
     * @param root
     */
    public void setRoot(Property<S> root) {
        uninstallRoot();
        this.root = root;
        installRoot();
    }
    /**
     * @param oldValue
     * @param newValue
     */
    private void updateChild(S oldValue, S newValue) {
        unbind();
        if (newValue == null) {
            set(defaultValue);
            return;
        }
        ObservableValue<T> observable = childFactory.call(newValue);
        bind(observable);
    }

    /**
     * Installs listeners/bindings related to the path.
     */
    private void installRoot() {
        if (getRoot() == null) return;
        getRoot().addListener(getRootListener());
        updateChild(null, getRoot().getValue());
    }


    /**
     * Returns the listener to root. Lazily created.
     * @return
     */
    private ChangeListener<? super S> getRootListener() {
        if (rootListener == null) {
            rootListener = (p, oldValue, newValue) -> { 
                updateChild(oldValue, newValue);
            };
        }
        return rootListener;
    }

    /**
     * Uninstalls all listeners/bindings related to the old path, if any.
     */
    private void uninstallRoot() {
        if (getRoot() == null) return;
        getRoot().removeListener(getRootListener());
        unbind();
    }


    public Property<S> getRoot() {
        return root;
    }
    
    @Override
    public Object getBean() {
        return null; // virtual property, no bean
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
