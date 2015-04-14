/*
 * Created on 27.06.2014
 *
 */
package de.swingempire.fx.property;

import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.FloatPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ListPropertyBase;
import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import com.sun.istack.internal.NotNull;
import com.sun.javafx.binding.BidirectionalBinding;

/**
 * Typed wrappers for ObjectProperty<TYPE>. Temporary workaround for 
 * https://javafx-jira.kenai.com/browse/RT-37523, will be fixed in 8u20.
 * <p>
 * Confirmed: fixed as of 8u20.
 * 
 * <p>
 * 
 * ListProperty wrapper around ObjectProperty<ObservableList> that 
 * re-wires itself on replacing list with equal list.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class BugPropertyAdapters {
    
    /**
     * Returns a ListProperty that adapts the property and is bidi-bound.
     * <p>
     * 
     * This is a fix for missing config option for ObjectProperty to force fire
     * changeEvents based on identity (vs. equality).
     * <p>
     * Without change to core api, there's no nice solution except adapting
     * a ListProperty that's bidi-binding itself to the objectProperty and
     * additionally registering an invalidationListener - on invalidation of
     * the bound objectProperty it'll explicitly sets its own value to that of
     * the objectProperty. Will not fire anything in itself, but rewires itself
     * to the new list, thus correctly firing notifications on modifications
     * to the new list (vs. nothing without, as its internal listChangeListener 
     * wasn't rewired)
     * <p>
     * 
     * Consequence for usage of list-valued properties: 
     * - don't use a ChangeListener on a listValued ObjectProperty if you
     *   are interested in content-listening, instead use an invalidationListener
     * - don't use listValued ObjectProperties at all, instead use ListProperty   
     * 
     * Consequences for usage of ListProperty 
     * - don't raw bidi-bind listProperty to a listValued ObjectProperty, instead
     * - either: bind to listValued property (if the listProperty can be read-only)
     * - or: additionally add an InvalidationListener that updates the value
     *   of the listProperty in case the new value is equals  
     * 
     * @param property the property to adapt as ListProperty
     * @return a ListProperty that's bidi-bound to the property and updates itself
     *   on invalidation if needed.
     */
    public static <T> ListProperty<T> listProperty(final Property<ObservableList<T>> property) {
        Objects.requireNonNull(property, "property must not be null");
        ListProperty<T> adapter = new ListPropertyBase<T>() {
            // PENDING JW: need weakListener?
            private InvalidationListener hack15793;
            {
                Bindings.bindBidirectional(this, property);
                hack15793 = o -> {
                    ObservableList<T> newItems =property.getValue();
                    ObservableList<T> oldItems = get();
                    // force rewiring to new list if equals
                    boolean changedEquals = (newItems != null) && (oldItems != null) 
                            && newItems.equals(oldItems);
                    if (changedEquals) {
                        set(newItems);
                    }
                };
                property.addListener(hack15793);
            }
            
            @Override
            public Object getBean() {
                return null; // virtual property, no bean
            }

            @Override
            public String getName() {
                return property.getName();
            }
            
            @Override
            protected void finalize() throws Throwable {
                try {
                    Bindings.unbindBidirectional(property, this);
                    property.removeListener(hack15793);
                } finally {
                    super.finalize();
                }
            }

        };
        return adapter;
        
    }

    public static BooleanProperty booleanProperty(final Property<Boolean> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return property instanceof BooleanProperty ? (BooleanProperty)property : new BooleanPropertyBase() {
            {
                BidirectionalBinding.bind(this, property);
            }

            @Override
            public Object getBean() {
                return null; // Virtual property, no bean
            }

            @Override
            public String getName() {
                return property.getName();
            }

            @Override
            protected void finalize() throws Throwable {
                try {
                    BidirectionalBinding.unbind(property, this);
                } finally {
                    super.finalize();
                }
            }
        };
    }

    public static LongProperty longProperty(final Property<Long> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return new LongPropertyBase() {
            {
                bindBidirectional(cast(property));
                // original:
                //BidirectionalBinding.bindNumber(property, this);
            }
            
            @Override
            public Object getBean() {
                return null; // Virtual property, no bean
            }
            
            @Override
            public String getName() {
                return property.getName();
            }
            
            @Override
            protected void finalize() throws Throwable {
                try {
                    unbindBidirectional(cast(property));
                    // this is fine in core, even with the core fix
                    // because the BidirectionalBinding (aka: listener registered
                    // to each of the properties) is created with this sequence
                    // original
                    // BidirectionalBinding.unbindNumber(property, this);
                } finally {
                    super.finalize();
                }
            }
        };
    }
    
    public static FloatProperty floatProperty(final Property<Float> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return new FloatPropertyBase() {
            {
                bindBidirectional(cast(property));
                // original:
                //BidirectionalBinding.bindNumber(property, this);
            }
            
            @Override
            public Object getBean() {
                return null; // Virtual property, no bean
            }
            
            @Override
            public String getName() {
                return property.getName();
            }
            
            @Override
            protected void finalize() throws Throwable {
                try {
                    unbindBidirectional(cast(property));
                    // this is fine in core, even with the core fix
                    // because the BidirectionalBinding (aka: listener registered
                    // to each of the properties) is created with this sequence
                    // original
                    // BidirectionalBinding.unbindNumber(property, this);
                } finally {
                    super.finalize();
                }
            }
        };
    }
    
    public static DoubleProperty doubleProperty(final Property<Double> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return new DoublePropertyBase() {
            {
                bindBidirectional(cast(property));
                // original:
                //BidirectionalBinding.bindNumber(property, this);
            }
            
            @Override
            public Object getBean() {
                return null; // Virtual property, no bean
            }
            
            @Override
            public String getName() {
                return property.getName();
            }
            
            @Override
            protected void finalize() throws Throwable {
                try {
                    unbindBidirectional(cast(property));
                    // this is fine in core, even with the core fix
                    // because the BidirectionalBinding (aka: listener registered
                    // to each of the properties) is created with this sequence
                    // original
                    // BidirectionalBinding.unbindNumber(property, this);
                } finally {
                    super.finalize();
                }
            }
        };
    }
    
    public static IntegerProperty integerProperty(final Property<Integer> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return new IntegerPropertyBase() {
            {
                bindBidirectional(cast(property));
                // original:
                //BidirectionalBinding.bindNumber(property, this);
            }

            @Override
            public Object getBean() {
                return null; // Virtual property, no bean
            }

            @Override
            public String getName() {
                return property.getName();
            }

            @Override
            protected void finalize() throws Throwable {
                try {
                    unbindBidirectional(cast(property));
                    // this is fine in core, even with the core fix
                    // because the BidirectionalBinding (aka: listener registered
                    // to each of the properties) is created with this sequence
                    // original
                    // BidirectionalBinding.unbindNumber(property, this);
                } finally {
                    super.finalize();
                }
            }
        };
    }

    /**
     * Type cast to allow bidi binding with a concrete XXProperty (with
     * XX = Integer, Double ...). This is (?) safe because the XXProperty
     * internally copes with type conversions from Number to the concrete
     * type on setting its own value and exports the concrete type as
     * needed by the object property.
     * 
     */
    private static <T extends Number> Property<Number> cast(Property<T> p) {
        return (Property<Number>) p;
    }

//------------------------ c&p from core, just to see    
    public static BidirectionalBinding bindNumber(Property<Integer> property1, IntegerProperty property2) {
        return bindNumber(property1, property2);
    }

    private static <T extends Number> BidirectionalBinding bindNumber(Property<T> property1, Property<Number> property2) {
//        checkParameters(property1, property2);
        
        final BidirectionalBinding<Number> binding = null; //new TypedNumberBidirectionalBinding<T>(property1, property2);
        
        property1.setValue((T)property2.getValue());
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }
    
//------------- end c&p from core
    
    private BugPropertyAdapters(){};
}
