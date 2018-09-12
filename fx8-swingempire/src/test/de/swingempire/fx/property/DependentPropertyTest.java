/*
 * Created on 12.09.2018
 *
 */
package de.swingempire.fx.property;

import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.InvalidationReport;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableIntegerValue;

/**
 * https://stackoverflow.com/q/52265201/203657
 * bounded values
 * 
 * option A: ignore invalid
 * 
 * option B: update other if not in range
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class DependentPropertyTest {

    public static interface ARange<T extends Number> {
        
        /** 
         * upper limit of range
         * @return
         */
        ObjectProperty<T> maxProperty();
        
        /**
         * lower limit of range
         * @return
         */
        ObjectProperty<T> minProperty();
        
        default boolean isValidMax(T value) {
            return value != null && value.doubleValue() >= minProperty().get().doubleValue();
        }
        
        default boolean isValidMin(T value) {
            return value != null && value.doubleValue() <= maxProperty().get().doubleValue();
        }
    }
    
    public static class AbstractARange<T extends Number> implements ARange<T> {
        
        private ObjectProperty<T> min;
        private ObjectProperty<T> max;

        public AbstractARange(T lower, T upper) {
            Objects.requireNonNull(lower, "lower must not be null");
            Objects.requireNonNull(upper, "upper must not be null");
            if (!(lower.doubleValue() <= upper.doubleValue())) {
                throw new IllegalArgumentException("lower " + lower + " must be less-equal upper " + upper);
            }
            min = new SimpleObjectProperty<T>(this, "min", lower) {
                T old = lower;
                @Override
                protected void invalidated() {
                    if (isValidMin(get())) {
                        old = get();
                    } else {
                        set(old);
                    }
                }
                
            };
            
            max = new SimpleObjectProperty<T>(this, "max", upper) {
                T old = upper;

                @Override
                protected void invalidated() {
                    if (isValidMax(get())) {
                        old = get();
                    } else {
                        set(old);
                    }
                }
                
            };
        }
        @Override
        public ObjectProperty<T> maxProperty() {
            return max;
        }

        @Override
        public ObjectProperty<T> minProperty() {
            return min;
        }
        
    }
    
    
    @Test
    public void testResetInListenerNotification() {
        int min = 0;
        int max = 100;
        IntegerProperty value = new SimpleIntegerProperty(max);
        ChangeListener<Number> ignore = (src, ov, nv) -> {
            if (nv.intValue() < min) {
                ((WritableIntegerValue) src).setValue(ov);
            }
        };
        value.addListener(ignore);
        ChangeReport report = new ChangeReport(value);
        value.set(min - 10);
        // change listeners see the intermediate (incorrect) value
        System.out.println(report.getChanges());
        assertEquals(2, report.getEventCount());
    }
    
    @Test
    public void testRangeSetMaxBeyondMinInvalidationNotification() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        InvalidationReport report = new InvalidationReport(range.maxProperty());
        range.maxProperty().set(min - 10);
        // reverting to old value in invalidated fires 2 invalidation events
        assertEquals(2, report.getEventCount());
    }
    
    @Test
    public void testRangeSetMinBeyondMaxInvalidationNotification() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        InvalidationReport report = new InvalidationReport(range.minProperty());
        range.minProperty().set(max + 10);
        // reverting to old value in invalidated fires 2 invalidation events
        assertEquals(2, report.getEventCount());
    }
    
    @Test
    public void testRangeSetMaxBeyondMinChangeNotification() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        ChangeReport report = new ChangeReport(range.maxProperty());
        range.maxProperty().set(min - 10);
        // reverting to old value in invalidated doesn't notify (which is good!)
        assertEquals(0, report.getEventCount());
    }
 
    @Test
    public void testRangeSetMinBeyondMaxChangeNotification() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        ChangeReport report = new ChangeReport(range.minProperty());
        range.minProperty().set(max + 10);
        // reverting to old value in invalidated doesn't notify (which is good!)
        assertEquals(0, report.getEventCount());
    }
   
    @Test
    public void testRangeSetMaxBeyondMin() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        range.maxProperty().set(min - 10);
        assertEquals(max, range.maxProperty().get().intValue());
    }
    
    @Test
    public void testRangeSetMinBeyondMax() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        range.minProperty().set(max + 10);
        assertEquals(min, range.minProperty().get().intValue());
    }
    
    @Test
    public void testRangeSetMinSameMax() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        range.minProperty().set(max);
        assertEquals(max, range.minProperty().get().intValue());
    }
    
    @Test
    public void testRangeSetMaxSameMin() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        range.maxProperty().set(min);
        assertEquals(min, range.maxProperty().get().intValue());
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testRangeInitIllegal() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(max, min);
    }
    
    @Test
    public void testRangeInitSame() {
        int min = 100;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        assertEquals(min, range.minProperty().get().intValue());
        assertEquals(max, range.maxProperty().get().intValue());
    }
    @Test
    public void testRangeInit() {
        int min = 0;
        int max = 100;
        ARange<Integer> range = new AbstractARange<>(min, max);
        assertEquals(min, range.minProperty().get().intValue());
        assertEquals(max, range.maxProperty().get().intValue());
        
    }

}
