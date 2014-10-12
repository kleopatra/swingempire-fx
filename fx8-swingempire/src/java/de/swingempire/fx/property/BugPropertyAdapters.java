/*
 * Created on 27.06.2014
 *
 */
package de.swingempire.fx.property;

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
 * 
 * Confirmed: fixed as of 8u20.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class BugPropertyAdapters {
    
    /**
     * Trying invers binding initial source = this, initial target = property
     * Wild experiment - similar setup in external listener (in comboBox)
     * looks fine. Where's the difference?
     * @param property
     * @return
     */
    public static <T> ListProperty<T> listInverseProperty(@NotNull final Property<ObservableList<T>> property) {
        if (property instanceof ListProperty) return (ListProperty<T>) property;
        ListProperty<T> adapter = new ListPropertyBase<T>() {
            
            {
                // PENDING JW: this is the other way round... temporarily!!
                Bindings.bindBidirectional(property, this);
                InvalidationListener hack15793 = o -> {
                    // this is the hack that seems to be working in comboX?
                    ObservableList<T> newItems =property.getValue();
                    ObservableList<T> oldItems = get();
                    boolean changedEquals = (newItems != null) && (oldItems != null) && newItems.equals(oldItems);
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
                } finally {
                    super.finalize();
                }
            }
            
        };
        return adapter;
        
    }
    
    /**
     * Normal binding: initial source = property, initial target = adapter
     * @param property
     * @return
     */
    public static <T> ListProperty<T> listProperty(@NotNull final Property<ObservableList<T>> property) {
        if (property instanceof ListProperty) return (ListProperty<T>) property;
        ListProperty<T> adapter = new ListPropertyBase<T>() {

            {
                Bindings.bindBidirectional(this, property);
                InvalidationListener hack15793 = o -> {
                    ObservableList<T> newItems =property.getValue();
                    ObservableList<T> oldItems = get();
                    boolean changedEquals = (newItems != null) && (oldItems != null) && newItems.equals(oldItems);
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
