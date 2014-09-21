/*
 * Created on 25.05.2014
 *
 */
package de.swingempire.fx.property;

import java.util.Objects;
import java.util.logging.Logger;

import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A property that defers committing/flushing changes to a wrapped property until
 * getting a signal (aka: "buffering").
 * 
 * Note that the unidirectial bind of this property is used for internal purposes,
 * using it externally will mess up the internal buffering management!
 * 
 * TBD: implement reflective property lookup.
 * 
 * Note: there are impedance mismatches between the type specific (set(primitive)) and
 * generic (set(<TypeOfPrimitive)) hierarchy. Specifically for boolean: the latter allows
 * null, while the former doesn't. 
 * 
 * BooleanProperty.setValue converts a null to false
 * BooleanProperty.bindBidirectional(Property<Boolean>) takes a generically typed property 
 *   and delegates to Bindings
 * Bindings delegates to BidirectionalBinding
 * BidirectionalBinding uses setValue to update property1  
 * should be fine (as of the first conversion) but throws NPE 
 * 
 * ahhh .... doesn't throw an NPE, logs an NPE as info on doing the conversion!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
//@SuppressWarnings({"rawtypes", "unchecked"})
public class BufferedObjectProperty<T> extends ObjectPropertyBase<T> {

    private ReadOnlyBooleanWrapper buffering;
    private Trigger trigger;
    private ChangeListener<Boolean> triggerListener;
    private Property<T> subject;
    // THINK wrap the subject into a property by itself: advantage would be 
    // notification on subject change - would we need it?
//    private ObjectProperty property;
    
    private String name;
    private T defaultValue;
    
    public BufferedObjectProperty(Trigger trigger) {
        this(null, trigger);
    }

    public BufferedObjectProperty(String name, Trigger trigger) {
        this(name, trigger, null);
    }

    /**
     * Note: hacking constructor around excessive logging message if value is null
     * for boolean type.
     * 
     * @param name
     * @param trigger
     * @param defaultValue
     */
    public BufferedObjectProperty(String name, Trigger trigger, T defaultValue) {
        this.name = name;
        initProperties();
        setTrigger(trigger);
        setDefaultValue(defaultValue);
    }
    
    /**
     * Sets the default value to use if unbound. Hacking around the excessive logging
     * for boolean nulls.
     * 
     * @param defaultValue
     */
    private void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        super.set(defaultValue);
    }

    /**
     * PENDING JW - think about value if subject null or buffering
     * 
     * This is implemented as unbind if subject == null.
     * The default behaviour of unbind is to use the last value of
     * the observable as the value of this.
     * 
     * PENDING JW - think about buffering state 
     * 
     * goodies keep buffering state, really want it here?
     * 
     * PENDING JW: BooleanProperty logs longish warning on setting
     * the value to null ... hacked around with defaultValue.
     * fixed as of 8u20, think about removal? Maybe still need
     * it because the old value is sticky after nulling the subject.
     * 
     * @param subject
     */
    @SuppressWarnings("unchecked")
    public void setSubject(Property<T> subject) {
        this.subject = subject;
        if (subject == null) {
            unbind();
            super.set(defaultValue);
//            set(defaultValue);
        } else {
            bind(subject);
        }
    }

    public Property<T> getSubject() {
        return subject;
    }
    
    /**
     * Sets the trigger that controls the commit/flush of this buffer.
     * 
     * @param trigger the trigger used to commit/flush this buffer, must
     *   not be null.
     * @throws NullPointerException if trigger is null
     */
    public void setTrigger(Trigger trigger) {
        Trigger old = getTrigger();
        if (old != null) {
            old.removeListener(triggerListener);
        }
        this.trigger = Objects.requireNonNull(trigger, "trigger must not be null");
        trigger.addListener(getTriggerListener());
    }
    
    /**
     * @return
     */
    public Trigger getTrigger() {
        return trigger;
    }
    
    /**
     * Commits the value to the buffered property and re-binds to it, if 
     * buffering. Does nothing if not buffering.
     */
    protected void commit() {
        if (isBuffering()) {
            subject.setValue(get());
            bind(subject);
        }
    }

    /**
     * Rebinds itselt to the buffered propety if buffering. Does nothing if not
     * buffering.
     */
    protected void flush() {
        if (isBuffering()) {
            bind(subject);
        }
    }

    /**
     * Overridden to unbind and start buffering.
     * 
     * PENDING JW: what if the subject is null? Goodies throws IllegalState - 
     * feels too restrictive as the underlying bean might be null.
     * 
     * Can't anyway, as bidi binding might happen when wrapping into a 
     * native type wrapper (f.i. Boolean.booleanProperty)
     * 
     */
    @Override
    public void set(T newValue) {
        if (isBound()) {
            unbind();
            setBuffering(true);
        }
        super.set(newValue);
    }

    /**
     * The unidirectional bind is reserved for internal purposes, such
     * that we need to disable independent usage. 
     * 
     * Throw or silently do nothing? For now throwing IllegalState, though
     * that's breaking super's contract ... 
     * 
     * Note that it is not entire
     * safe: external bind to the subject still possible, messing up with
     * internal state. TODO: entirely bypass with local methods superXX
     */
    @Override
    public void bind(ObservableValue<? extends T> observable) {
        if (observable != getSubject()) throw 
            new IllegalStateException("can only bind to subject");
        setBuffering(false);
        super.bind(observable);
    }

    /**
     * 
     * Overridden for documentation warning.
     * 
     * The unidirectional binding is reserved for internal purposes, such that 
     * we would need to disable independent usage. That's not possible (not even technically 
     * which is breaking super's contract anyway), as automatic
     * unbind is used extensively in super. So all we can do for now is to strongly
     * discourage its usage at all.
     * 
     */
    @Override
    public void unbind() {
        super.unbind();
    }

    /**
     * @return
     */
    protected ChangeListener<Boolean> getTriggerListener() {
        if (triggerListener == null) {
            triggerListener = new ChangeListener<Boolean>(){

                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (Boolean.TRUE.equals(newValue)) {
                        commit();
                    } else if (Boolean.FALSE.equals(newValue)) {
                        flush();
                    }
                }
            };
        }
        return triggerListener;
    }


    /**
     * 
     */
    private void initProperties() {
        buffering = new ReadOnlyBooleanWrapper(this, "buffering", false);
    }
 
//---------- property boilerplate
    
    public final boolean isBuffering() {
        return buffering.get();
    }
    
    public ReadOnlyBooleanProperty bufferingProperty() {
        return buffering.getReadOnlyProperty();
    }
    
    protected final void setBuffering(boolean buffering) {
        this.buffering.set(buffering);
    }
// ---------- bean methods ...

    /**
     * THINK: return the bean of the subject, if any?
     * Probably not, virtual properties don't have a bean (see f.i. the 
     * adapters in native type properties) 
     */
    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BufferedObjectProperty.class.getName());
}
