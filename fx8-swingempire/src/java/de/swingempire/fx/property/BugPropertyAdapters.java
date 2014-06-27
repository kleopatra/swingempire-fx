/*
 * Created on 27.06.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.Property;

import com.sun.javafx.binding.BidirectionalBinding;

/**
 * Typed wrappers for ObjectProperty<TYPE>. Temporary workaround for 
 * https://javafx-jira.kenai.com/browse/RT-37523, will be fixed in 8u20.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class BugPropertyAdapters {

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
