/*
 * Created on 18.06.2014
 *
 */
package de.swingempire.fx.property;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static de.swingempire.fx.property.BugPropertyAdapters.*;
import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.cell.Person22463;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.InvalidationReport;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ObservableTest {

// bindings and unwanted logging
    
    @Test
    public void testSelectBindungToNull() {
        ObjectProperty<MenuItem> item = new SimpleObjectProperty<>();
        Bindings.selectString(item, "text");
    }
    
//--------- using method references
    
    public static class ClassWithProperty {
        private ObjectProperty<LocalDate> baseDate = new SimpleObjectProperty(this, "baseDate");
        private ObjectProperty<LocalDate> derivedDate = new SimpleObjectProperty(this, "derivedDate");
        
        public ClassWithProperty() {
            baseDate.addListener(this::dateChanged);
        }
        
        protected void dateChanged(ObservableValue sender, LocalDate ov, LocalDate nv) {
//            derivedDate.set(nv.plusDays(10));
        }
        
        protected void dateChanged(LocalDate ov) {
            derivedDate.set(baseDate.get().minusDays(10));
        }
    }
    
    @Test
    public void testMethod() {
        ClassWithProperty cl = new ClassWithProperty();
        cl.baseDate.set(LocalDate.now());
        assertEquals(10, ChronoUnit.DAYS.between(cl.baseDate.get(), cl.derivedDate.get()));
    }
//--------------------------

    @Test
    public void testIntegerSpinner() {
        int initial1 = 2;
        IntegerProperty p1 = new SimpleIntegerProperty(initial1);
        SpinnerValueFactory<Integer> spinner = new IntegerSpinnerValueFactory(0, 50);
        ObjectProperty<Integer> number = spinner.valueProperty();
        number.bindBidirectional(p1.asObject());
        assertEquals("sanity: initial value on bind source unchanged", initial1, p1.get());
        assertEquals("sanity: initial value on bind target taken from source", initial1, number.get().intValue());
        
    }
    @Test
    public void testBidiOperatorBindingIntegerSpinner() {
        int initial1 = 2;
        IntegerProperty p1 = new SimpleIntegerProperty(initial1);
        IntegerProperty p2 = new SimpleIntegerProperty(1);
        ChangeListener<Number> update2 = (source, old, value) -> {
            p2.set(value.intValue() * 2);
        };
        ChangeListener<Number> update1 = (source, old, value) -> {
            p1.set(value.intValue() / 2);
        };
        BidirectionalBindingX.<Number, Number>bindBidirectional(p1, p2, update2, update1);
        
        SpinnerValueFactory<Integer> spinner = new IntegerSpinnerValueFactory(0, 50);
        ObjectProperty<Integer> number = spinner.valueProperty();
        number.bindBidirectional(p1.asObject());
        assertEquals("sanity: initial value on bind source unchanged", initial1, p1.get());
        assertEquals("sanity: initial value on bind target taken from source", initial1, number.get().intValue());
        int five = 6;
        // set value on binding
        number.set(five);
        assertEquals(five, p1.get());
        assertEquals(2 * five, p2.get());
        
        // set value on any of the properties must update binding
        p2.set(five);
        assertEquals(five / 2, p1.get());
        assertEquals(p1.get(), number.get().intValue());
    }
    @Test
    public void testBidiOperatorBindingPlusAnotherInteger() {
        int initial1 = 2;
        IntegerProperty p1 = new SimpleIntegerProperty(initial1);
        IntegerProperty p2 = new SimpleIntegerProperty(1);
        ChangeListener<Number> update2 = (source, old, value) -> {
            p2.set(value.intValue() * 2);
        };
        ChangeListener<Number> update1 = (source, old, value) -> {
            p1.set(value.intValue() / 2);
        };
        BidirectionalBindingX.<Number, Number>bindBidirectional(p1, p2, update2, update1);
        
        ObjectProperty<Integer> number = new SimpleObjectProperty<>(0);
        number.bindBidirectional(p1.asObject());
        assertEquals("sanity: initial value on bind source unchanged", initial1, p1.get());
        assertEquals("sanity: initial value on bind target taken from source", initial1, number.get().intValue());
        int five = 6;
        // set value on binding
        number.set(five);
        assertEquals(five, p1.get());
        assertEquals(2 * five, p2.get());
        
        // set value on any of the properties must update binding
        p2.set(five);
        assertEquals(five / 2, p1.get());
        assertEquals(p1.get(), number.get().intValue());
    }
    @Test
    public void testBidiOperatorBindingPlusAnother() {
        int initial1 = 2;
        IntegerProperty p1 = new SimpleIntegerProperty(initial1);
        IntegerProperty p2 = new SimpleIntegerProperty(1);
        ChangeListener<Number> update2 = (source, old, value) -> {
            p2.set(value.intValue() * 2);
        };
        ChangeListener<Number> update1 = (source, old, value) -> {
            p1.set(value.intValue() / 2);
        };
        BidirectionalBindingX.<Number, Number>bindBidirectional(p1, p2, update2, update1);
        
        ObjectProperty<Number> number = new SimpleObjectProperty<>(0);
        number.bindBidirectional(p1);
        assertEquals("sanity: initial value on bind source unchanged", initial1, p1.get());
        assertEquals("sanity: initial value on bind target taken from source", initial1, number.get().intValue());
        int five = 6;
        // set value on binding
        number.set(five);
        assertEquals(five, p1.get());
        assertEquals(2 * five, p2.get());
        
        // set value on any of the properties must update binding
        p2.set(five);
        assertEquals(five / 2, p1.get());
        assertEquals(p1.get(), number.get());
    }
    
    @Test
    public void testBidiOperatorBinding() {
        IntegerProperty p1 = new SimpleIntegerProperty(0);
        IntegerProperty p2 = new SimpleIntegerProperty(1);
        ChangeListener<Number> update2 = (source, old, value) -> {
            p2.set(value.intValue() * 2);
        };
        ChangeListener<Number> update1 = (source, old, value) -> {
            p1.set(value.intValue() / 2);
        };
        BidirectionalBindingX.<Number, Number>bindBidirectional(p1, p2, update2, update1);
        int five = 6;
        // set value on p1
        p1.set(five);
        assertEquals(five, p1.get());
        assertEquals(2 * five, p2.get());
        
        // the other way round
        p2.set(five);
        assertEquals(five / 2, p1.get());
    }
    
//-------------- identity vs. equality    
    /**
     * Invalidation not fired on same value.
     */
    @Test
    public void testObjectPropertySameItem() {
        int index = 10;
        IntegerProperty p = new SimpleIntegerProperty(index);
        InvalidationReport report = new InvalidationReport(p);
        p.set(index);
        assertEquals(0, report.getEventCount());
    }
    
    /**
     * ObjectProperty fires on !equal only, sanity check here.
     * So why does TableRowSkin gets notified when re-setting
     * item with equals?
     * 
     */
    @Test
    public void testObjectPropertyEqualItem() {
        Person22463 first = new Person22463();
        first.setId(1l);
        first.setName("dummy 1");
        Person22463 second = new Person22463();
        second.setId(first.getId());
        second.setName("dummy doo");
        ObjectProperty property = new SimpleObjectProperty(first);
        ChangeReport report = new ChangeReport(property);
        property.set(second);
        assertEquals("doesn't fire - expected", 0, report.getEventCount());
    }
    
    /**
     * ObjectProperty fires on !equal only, sanity check here.
     * So why does TableRowSkin gets notified when re-setting
     * item with equals?
     * 
     */
    @Test
    public void testObjectPropertyEqualItemInvalidation() {
        Person22463 first = new Person22463();
        first.setId(1l);
        first.setName("dummy 1");
        Person22463 second = new Person22463();
        second.setId(first.getId());
        second.setName("dummy doo");
        ObjectProperty property = new SimpleObjectProperty(first);
        InvalidationReport report = new InvalidationReport(property);
        property.set(second);
        assertEquals("invalidation must fire", 1, report.getEventCount());
    }
    
//-------------------- end test related to https://javafx-jira.kenai.com/browse/RT-38770
    
    @Test
    public void testDynamicBooleanBinding() {
        DynamicBooleanBinding binding = new DynamicBooleanBinding() {

            @Override
            protected boolean computeValue() {
                return false;
            }
            
        };
        assertNotNull(binding.marker);
        binding.get();
        assertTrue(binding.isValid());
        BooleanProperty p = new SimpleBooleanProperty(true);
        binding.addDependencies(p);
        assertFalse(binding.isValid());
    }

    /**
     * Bug: the observable in notifications must be the instance the 
     * listener was added to. 
     * 
     * Here: testing invalidation.
     */
    @Test
    public void testReadOnlyWrapperInvalidation() {
        ReadOnlyObjectWrapper<String> wrapper = new ReadOnlyObjectWrapper<>();
        wrapper.addListener(o -> {
            assertSame("event source must be same the listener was added", wrapper, o);
        });
        wrapper.setValue("dummy");
    }
    
    /**
     * Bug: the observable in notifications must be the instance the 
     * listener was added to. 
     * 
     * Here: testing change.
     */
    @Test
    public void testReadOnlyWrapperChange() {
        ReadOnlyObjectWrapper<String> wrapper = new ReadOnlyObjectWrapper<>();
        wrapper.addListener((o, oldValue, newValue) -> {
            assertEquals(o.getValue(), newValue);
            assertSame("event source must be same the listener was added", wrapper, o);
        });
        wrapper.setValue("dummy");
    }
    @Test
    public void testNumberTyping() {
        int initial = 10;
        IntegerProperty base = new SimpleIntegerProperty(initial);
        base.setValue(10.5);
        assertEquals(Integer.class, base.getValue().getClass());
    }

    /**
     * A null value is changed to 0 in set (documented in
     * WritableIntegerValue). 
     * 
     * How about bind? That's documented and handled in IntegerPropertyBase:
     * it has its bind(observable) implemented to wrap a IntegerBinding
     * around the objectProperty with a computeValue method that
     * converts a null into a 0.
     */
    @Test
    public void testIntegerBind() {
        IntegerProperty integerProperty = new SimpleIntegerProperty();
        ObjectProperty<Integer> objectProperty = new SimpleObjectProperty<>(1);
        integerProperty.bind(objectProperty);
        objectProperty.setValue(10);
        assertEquals(10, integerProperty.get());
        objectProperty.setValue(null);
        assertEquals(0, integerProperty.get());
    }
    
    /**
     * Bug - wrapper changes the source value.
     * https://javafx-jira.kenai.com/browse/RT-37523
     * 
     * Fixed as of 8u20
     */
    @Test
    public void testBooleanCore() {
        Boolean initial = true;
        ObjectProperty<Boolean> source = new SimpleObjectProperty<>(initial); 
        BooleanProperty wrapper = BooleanProperty.booleanProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testBooleanFix() {
        Boolean initial = true;
        ObjectProperty<Boolean> source = new SimpleObjectProperty<>(initial); 
        BooleanProperty wrapper = booleanProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testLongCore() {
        Long initial = 10l;
        ObjectProperty<Long> source = new SimpleObjectProperty<>(initial); 
        LongProperty wrapper = LongProperty.longProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testLongFix() {
        Long initial = 10l;
        ObjectProperty<Long> source = new SimpleObjectProperty<>(initial); 
        LongProperty wrapper = longProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testFloatCore() {
        Float initial = 10.f;
        ObjectProperty<Float> source = new SimpleObjectProperty<>(initial); 
        FloatProperty wrapper = FloatProperty.floatProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testFloatFix() {
        Float initial = 10.f;
        ObjectProperty<Float> source = new SimpleObjectProperty<>(initial); 
        FloatProperty wrapper = floatProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testDoubleCore() {
        Double initial = 10.;
        ObjectProperty<Double> source = new SimpleObjectProperty<>(initial); 
        DoubleProperty wrapper = DoubleProperty.doubleProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testDoubleFix() {
        Double initial = 10.;
        ObjectProperty<Double> source = new SimpleObjectProperty<>(initial); 
        DoubleProperty wrapper = doubleProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
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
        assertEquals(initial, wrapper.getValue());
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
