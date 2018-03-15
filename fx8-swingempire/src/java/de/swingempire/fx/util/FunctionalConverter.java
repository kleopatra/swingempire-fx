/*
 * Created on 15.03.2018
 *
 */
package de.swingempire.fx.util;

import java.util.Objects;
import java.util.function.Function;

import javafx.util.StringConverter;

/**
 * Implementation of StringConverter that uses Functions to convert to and from
 * String. The to-converter must not be null, the from-converter can be null but
 * then the from method must not be used as it will throw an UnsupportedOperationException. 
 * While doing so strictly is violating super's contract (which has no constraints
 * whatever) trying to convert from String to T without a from-converter is most
 * probably a coding error which should be detected as early as possible. 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FunctionalConverter<T> extends StringConverter<T> {

    private final Function<T, String> toString;
    private final Function<String, T> fromString;
    
    /**
     * Instantiates a StringConverter that uses the given function in converting
     * toString and no fromString function. The latter implies that calling
     * fromString method will throw an UnsupportedOperationException 
     * 
     * @param toString the function to use in toString, must not be null
     * @throws NullPointerException if toString is null.
     * 
     * @see #toString(Object)
     * @see #fromString(String)
     */
    public FunctionalConverter(Function<T, String> toString) {
        this(toString, null);
    };
    
    /**
     * Instantiates a StringConverter that uses the given functions in converting
     * to/fromString. 
     * 
     * @param toString the function to use in toString, must not be null
     * @param fromString the function to use in fromString, may be null to indicate that
     *    from conversion is not supported
     * @throws NullPointerException if toString is null.
     * 
     * @see #toString(Object)
     * @see #fromString(String)
     */
    public FunctionalConverter(Function<T, String> toString, Function<String, T> fromString) {
        this.toString = Objects.requireNonNull(toString, "toString function must not be null"); 
        this.fromString = fromString;
        
    }
    /**
     * {@inheritDoc} <p>
     * Implemented to use the to-function in conversion. This will succeed always. 
     * 
     * Note: it's the task of the given function to handle a null item.
     */
    @Override
    public String toString(T item) {
        return toString.apply(item);
    }
    
    /**
     * {@inheritDoc} <p>
     * 
     * Implemented to use the from-function in conversion, if available. Otherwise, it
     * will throw an UnsupportedOperationException (to fail fast).
     * 
     * @throws UnsupportedOperationException if there's  not from-converter.
     */
    @Override
    public T fromString(String text) {
        if (fromString == null) throw new UnsupportedOperationException("from conversion is not supported, "
                + "as the from-converter is null");
        return fromString.apply(text);
    }
    
//--------------- factory methods
    
    /**
     * Creates and returns a StringConverter that uses the given toString function for conversion.
     * The converter is configured to not support from-conversion.
     * 
     * @param toString the function to use in toString, must not be null
     * @throws NullPointerException if toString is null.
     * @return a StringConverter for toString conversion.
     * 
     * @see #toString(Object)
     * @see #fromString(String)
     */
    public static <M> StringConverter<M> asConverter(Function<M, String> toString) {
        return new FunctionalConverter<M>(toString);
    }
    
    /**
     * Creates and returns a StringConverter that uses the given functions in converting
     * to/fromString. 
     * 
     * @param toString the function to use in toString, must not be null
     * @param fromString the function to use in fromString, may be null to indicate that
     *    from conversion is not supported
     * @throws NullPointerException if toString is null.
     * 
     * @return a StringConverter for toString conversion which supports fromString if the
     * from function is not null.
     * 
     * @see #toString(Object)
     * @see #fromString(String)
     */
    public static <M> StringConverter<M> asConverter(Function<M, String> toString, Function<String, M> fromString) {
        return new FunctionalConverter<M>(toString, fromString);
    }
    
}
