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
public class DependentPropertyTestB {

    public static interface BRange<T extends Number> {
        
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
        
        default void adjustMax() {
            if (minProperty().get().doubleValue() > maxProperty().get().doubleValue()) {
                maxProperty().set(minProperty().get());
            };
        }
        
        default void adjustMin() {
            if (maxProperty().get().doubleValue() < minProperty().get().doubleValue()) {
                minProperty().set(maxProperty().get());
            };
        }
    }
    
    public static class AbstractBRange<T extends Number> implements BRange<T> {
        
        private ObjectProperty<T> min;
        private ObjectProperty<T> max;

        public AbstractBRange(T lower, T upper) {
            Objects.requireNonNull(lower, "lower must not be null");
            Objects.requireNonNull(upper, "upper must not be null");
            if (!(lower.doubleValue() <= upper.doubleValue())) {
                throw new IllegalArgumentException("lower " + lower + " must be less-equal upper " + upper);
            }
            min = new SimpleObjectProperty<T>(this, "min", lower) {
                @Override
                protected void invalidated() {
                    adjustMax();
                }
                
            };
            
            max = new SimpleObjectProperty<T>(this, "max", upper) {
                @Override
                protected void invalidated() {
                    adjustMin();
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
        System.out.println(report.getChanges());
        assertEquals(2, report.getEventCount());
    }
    
    @Test
    public void testRangeSetMaxBeyondMinInvalidationNotification() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        InvalidationReport report = new InvalidationReport(range.maxProperty());
        range.maxProperty().set(min - 10);
        assertEquals(1, report.getEventCount());
    }
    
    @Test
    public void testRangeSetMinBeyondMaxInvalidationNotification() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        InvalidationReport report = new InvalidationReport(range.minProperty());
        range.minProperty().set(max + 10);
        assertEquals(1, report.getEventCount());
    }
    
    @Test
    public void testRangeSetMaxBeyondMinChangeNotification() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        ChangeReport minReport = new ChangeReport(range.minProperty());
        ChangeReport maxReport = new ChangeReport(range.maxProperty());
        range.maxProperty().set(min - 10);
        assertEquals(1, maxReport.getEventCount());
        assertEquals(1, minReport.getEventCount());
    }
 
    @Test
    public void testRangeSetMinBeyondMaxChangeNotification() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        ChangeReport minReport = new ChangeReport(range.minProperty());
        ChangeReport maxReport = new ChangeReport(range.maxProperty());
        
        range.minProperty().set(max + 10);
        assertEquals(1, minReport.getEventCount());
        assertEquals(1, maxReport.getEventCount());
    }
   
    @Test
    public void testRangeSetMaxBeyondMin() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        int value = min - 10;
        range.maxProperty().set(value);
        assertEquals(value, range.minProperty().get().intValue());
    }
    
    @Test
    public void testRangeSetMinBeyondMax() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        int value = max + 10;
        range.minProperty().set(value);
        assertEquals(value, range.maxProperty().get().intValue());
    }
    
    @Test
    public void testRangeSetMinSameMax() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        range.minProperty().set(max);
        assertEquals(max, range.minProperty().get().intValue());
    }
    
    @Test
    public void testRangeSetMaxSameMin() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        range.maxProperty().set(min);
        assertEquals(min, range.maxProperty().get().intValue());
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testRangeInitIllegal() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(max, min);
    }
    
    @Test
    public void testRangeInitSame() {
        int min = 100;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        assertEquals(min, range.minProperty().get().intValue());
        assertEquals(max, range.maxProperty().get().intValue());
    }
    @Test
    public void testRangeInit() {
        int min = 0;
        int max = 100;
        BRange<Integer> range = new AbstractBRange<>(min, max);
        assertEquals(min, range.minProperty().get().intValue());
        assertEquals(max, range.maxProperty().get().intValue());
        
    }

}
