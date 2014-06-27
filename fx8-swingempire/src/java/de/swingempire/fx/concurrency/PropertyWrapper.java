/*
 * Created on 07.06.2013
 *
 */
package de.swingempire.fx.concurrency;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.SwingUtilities;

/**
 * Wrapper that switches to FX-AT/EDT as appropriate. The assumption is
 * that the delegate needs to be accessed on the EDT while this property 
 * allows client access on the FX-AT.
 * 
 * related SO questions:
 * http://stackoverflow.com/q/19589542/203657
 * http://stackoverflow.com/q/19656810/203657
 * 
 * (this is the latest experiment, extracted from the pro-fx code base,
 * used in the example on SO)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PropertyWrapper<T> extends ObjectPropertyBase<T> {
    // the delegate we are keeping synched to
    private Property<T> delegate;
    // the value which is kept in synch (on being notified) with the delegate's value
    // JW: does this make sense at all?
    private volatile T value;
    // keeping a copy of the bean ... ? better not allow accessing at all? 
    // private Object delegateBean;
    private String delegateName;
    private ChangeListener<T> changeListener;
    
    public PropertyWrapper(Property<T> delegate) {
        this.delegate = delegate;
        bindDelegate();
    }

    /**
     * Returns the value which is kept synched to the delegate's value.
     */
    @Override
    public T get() {
        return value;
    }

    /**
     * Implemented to set the value of this property, immediately 
     * fire a value change if needed and then update the delegate.
     * 
     * The sequence may be crucial if the value is changed by a bidirectionally
     * bound property (like f.i. a TextProperty): that property reacts to 
     * change notifications triggered by its own change in a different 
     * way as by those from the outside, detected by a flag (sigh ...)
     * set while firing.
     */
    @Override
    public void set(T value) {
        T oldValue = this.value;
        this.value = value;
        if (!areEqual(oldValue, value)) {
            fireValueChangedEvent();
        }
        updateToDelegate(value);
        // PENDING: think about uni-directional binding
    }

    private boolean areEqual(T value, T other) {
        if (value == other) return true;
        if (value != null) return value.equals(other);
        return false;
    }


    /**
     * Updates the delegate's value to the given value. 
     * Guarantees to do the update on the EDT.
     * 
     * @param value
     */
    protected void updateToDelegate(final T value) {
        if (SwingUtilities.isEventDispatchThread()) {
            doUpdateToDelegate(value);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    doUpdateToDelegate(value);
                }
            });
        }
    }

    /**
     * Updates the delegate's value to the given value
     * This methods runs on the thread that it is called from.
     * 
     * @param the value to set. 
     * 
     */
    private void doUpdateToDelegate(T value) {
        delegate.setValue(value);
    }

    /**
     * Adds a ChangeListener to the delegate and synchs the value
     * to the delegate's value.
     * 
     * This is called once from the constructor, assuming that the thread it is
     * called on is compatible with the delegates threading rules.
     */
    private void bindDelegate() {
        if (changeListener != null) throw new IllegalStateException("cannot bind twice");
        value = delegate.getValue();
        delegateName = delegate.getName();
        changeListener = createChangeListener();
        delegate.addListener( 
                changeListener); 
    }
    
    /**
     * Creates and returns the ChangeLister that's registered to the delegate.
     * @return
     */
    private ChangeListener<T> createChangeListener() {
        ChangeListener<T> l = new ChangeListener<T>() {

            @Override
            public void changed(ObservableValue<? extends T> observable,
                    T oldValue, T newValue) {
                updateFromDelegate(newValue);
                
            }
            
        };
        // weakchangelistener doesn't work ... for some reason
        // we seem to need a strong reference to the wrapped listener
        // return new WeakChangeListener<T>(l);
        return l;
    }

    /**
     * Updates the internal value and notifies its listeners. Schedules the
     * activity for execution on the fx-thread, if not already called on it.
     * 
     * @param newValue
     */
    protected void updateFromDelegate(final T newValue) {
        if (Platform.isFxApplicationThread()) {
            doUpdateFromDelegate(newValue);
        } else {
            Platform.runLater(new Runnable() {
                
                @Override
                public void run() {
                    doUpdateFromDelegate(newValue);
                }}); 
        }
    }


    /**
     * Updates the internal value and notifies its listeners, if needed.
     * Does nothing if the newValue equals the current value.<p>
     * 
     * It runs on the thread it is called from.
     * 
     * @param newValue the new value.
     */
    protected void doUpdateFromDelegate(T newValue) {
        if (areEqual(newValue, value)) return;
        value = newValue;
        fireValueChangedEvent();
    }

    /**
     * Overridden to guarantee calling super on the fx-thread.
     */
    @Override
    protected void fireValueChangedEvent() {
        if (Platform.isFxApplicationThread()) {
            superFireChangedEvent();
        } else {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    superFireChangedEvent();
                }}); 
        }
    }

    protected void superFireChangedEvent() {
        super.fireValueChangedEvent();
    }

    /**
     * Implemented to return null.<p>
     * PENDING: allow access to delegate's bean? It's risky, as this method
     * most probably will be called on the fx-thread: even if we keep a copy
     * around, clients might poke around the bean without switching to the EDT.
     */
    @Override
    public Object getBean() {
        return null; //delegate != null ? delegate.getBean() : null;
    }

    @Override
    public String getName() {
        return delegateName; //delegate != null ? delegate.getName() : null;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(PropertyWrapper.class
            .getName());
}
