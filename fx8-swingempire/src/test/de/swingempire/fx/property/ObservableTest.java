/*
 * Created on 18.06.2014
 *
 */
package de.swingempire.fx.property;

import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static de.swingempire.fx.property.BugPropertyAdapters.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class ObservableTest {

    @Test
    public void testNumberTyping() {
        int initial = 10;
        IntegerProperty base = new SimpleIntegerProperty(initial);
        base.setValue(10.5);
        assertEquals(Integer.class, base.getValue().getClass());
    }
    
    @Test
    public void testObjectTyping() {
        Integer initial = 10;
        ObjectProperty<Integer> source = new SimpleObjectProperty<>(initial); 
        // illegal as expected
        //source.setValue(10.5);
    }
    
    @Test
    public void testBooleanCore() {
        Boolean initial = true;
        ObjectProperty<Boolean> source = new SimpleObjectProperty<>(initial); 
        BooleanProperty wrapper = BooleanProperty.booleanProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testBooleanFix() {
        Boolean initial = true;
        ObjectProperty<Boolean> source = new SimpleObjectProperty<>(initial); 
        BooleanProperty wrapper = booleanProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testLongCore() {
        Long initial = 10l;
        ObjectProperty<Long> source = new SimpleObjectProperty<>(initial); 
        LongProperty wrapper = LongProperty.longProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testLongFix() {
        Long initial = 10l;
        ObjectProperty<Long> source = new SimpleObjectProperty<>(initial); 
        LongProperty wrapper = longProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testFloatCore() {
        Float initial = 10.f;
        ObjectProperty<Float> source = new SimpleObjectProperty<>(initial); 
        FloatProperty wrapper = FloatProperty.floatProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testFloatFix() {
        Float initial = 10.f;
        ObjectProperty<Float> source = new SimpleObjectProperty<>(initial); 
        FloatProperty wrapper = floatProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testDoubleCore() {
        Double initial = 10.;
        ObjectProperty<Double> source = new SimpleObjectProperty<>(initial); 
        DoubleProperty wrapper = DoubleProperty.doubleProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    @Test
    public void testDoubleFix() {
        Double initial = 10.;
        ObjectProperty<Double> source = new SimpleObjectProperty<>(initial); 
        DoubleProperty wrapper = doubleProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    /**
     * Issue: core adapter changes state of source property
     */
    @Test
    public void testIntegerCore() {
        Integer initial = 10;
        ObjectProperty<Integer> source = new SimpleObjectProperty<>(initial); 
        IntegerProperty wrapper = IntegerProperty.integerProperty(source);
        assertEquals(initial, source.getValue());
    }
    
    /**
     * Issue: core adapter changes state of source property
     * 
     * fix by c&p ... has generic typing issues in bindNumber, method signature
     * 
     *     bindNumber(Property<T>, IntegerProperty);
     *     
     * aka: 
     *     bindNumber(Property<T>, Property<Number>)    
     *     
     * reverse can't be done:
     * 
     *     bindNumber(Property<Number>, Property<T>)
     *     
     * fix in u20 has a whole lot of methods with swapped parameter types, which at
     * the end swap the parameter when creating the TypedNumberBidirectionalBinding
     * (which is only the listener, registered after the binding synchs the values    
     * 
     */
    @Test
    public void testIntegerFix() {
        Integer initial = 10;
        ObjectProperty<Integer> source = new SimpleObjectProperty<>(initial); 
        IntegerProperty wrapper = integerProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
        wrapper.setValue(new Double(10.5));
    }
    
    /**
     * Reverting state in changeListener triggers a StackoverflowException
     * 
     * Which is to be expected: the general rule is to never-ever change the
     * state of the caller in a notification method.
     * 
     * Crap: was error in test code - there's no inherent reason for the
     * framework code to throw up, application might suffer, though (think
     * of different listeners reverting to different default values)
     * 
     * https://javafx-jira.kenai.com/browse/RT-32139
     * 
     */
    @Test 
    public void testStateChangeInChangeListener() {
        String sourceValue = "original";
        StringProperty source = new SimpleStringProperty(sourceValue);
        ChangeListener<String> l = new ChangeListener<String>() {
            
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                source.setValue(sourceValue);
            }
        };
        source.addListener(l);
        source.setValue("something else");
    }
    
    /**
     * Reverting state in Invalidation is fine, it's simply marked as invalid.
     * Crap, see above
     */
    @Test
    public void testStateChangeInvalidationListener() {
        String sourceValue = "original";
        StringProperty source = new SimpleStringProperty(sourceValue);
        InvalidationListener l = new InvalidationListener() {
            
            @Override
            public void invalidated(Observable observable) {
                if (!sourceValue.equals(source.getValue() )) {
                    source.setValue(sourceValue);
                }
            }
        };
        source.addListener(l);
        source.setValue("something else");
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ObservableTest.class
            .getName());
}
