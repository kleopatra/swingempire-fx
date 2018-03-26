/*
 * Created on 29.07.2015
 *
 */
package de.swingempire.fx.scene.control.pagination;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Tests around enhanced pagination skin. Here we test a stand-alone NavigationModel implementation.
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class NavigationTest {

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    protected NavigationModel nav;
    
    @Test
    public void testCurrentUpdateOnDecreasedSize() {
        nav.sizeProperty().set(10);
        nav.setCurrent(5);
        nav.sizeProperty().set(4);
        assertEquals(3, nav.getCurrent());
    }
    
    @Test
    public void testCurrentPropertyNegative() {
        nav.currentProperty().set(-1);
        assertEquals(0, nav.getCurrent());
    }
    
    @Test
    public void testCurrentPropertyEqualSize() {
        nav.currentProperty().set(nav.getSize());
        assertEquals(0, nav.getCurrent());
    }
    
    @Test
    public void testCurrentPropertyGreaterThanSize() {
        nav.currentProperty().set(5);
        assertEquals(0, nav.getCurrent());
    }
    
    @Test
    public void testCurrentPropertyInRange() {
        nav.sizeProperty().set(10);
        nav.currentProperty().set(5);
        assertEquals(5, nav.getCurrent());
    }
    
    @Test
    public void testCurrentNegativeOnNotEmpty() {
        nav.sizeProperty().set(10);
        nav.setCurrent(-1);
        assertEquals(0, nav.getCurrent());
    }
    @Test
    public void testCurrentNegativeOnEmpty() {
        nav.setCurrent(-1);
        assertEquals(0, nav.getCurrent());
    }
    @Test
    public void testCurrentGreaterThanSize() {
        nav.setCurrent(5);
        nav.sizeProperty().set(1);
        assertEquals(0, nav.getCurrent());
    }
    
    @Test
    public void testCurrentInRange() {
       nav.sizeProperty().set(10);
       nav.setCurrent(5);
       assertEquals(5, nav.getCurrent());
    }
    
    @Test
    public void testSizeMinimumIsOne() {
        nav.sizeProperty().set(0);
        assertEquals(1, nav.getSize());
    }
    
    @Test
    public void testSizePropertyMinimumIsOne() {
        nav.sizeProperty().set(0);
        assertEquals(1, nav.getSize());
    }
    
    @Test
    public void testInitial() {
        assertEquals(0, nav.getCurrent());
        assertEquals(1, nav.getSize());
    }
    
    @Before
    public void setup() {
       nav = createNavigation();
    }
    
    protected NavigationModel createNavigation() {
        return new TestNavigation();
    };
    
    /**
     * Simple implementation of NavigationModel intended to test default methods.
     */
    public static class TestNavigation implements NavigationModel {

        private IntegerProperty current;
        private IntegerProperty size;

        @Override
        public IntegerProperty currentProperty() {
            if (current == null) {
                current = new SimpleIntegerProperty() {

                    @Override
                    protected void invalidated() {
                        int current = get();
                        if (current >= getSize()) {
                            set(getSize() - 1);
                        } else if (current < 0) {
                            set(0);
                        }
                    }
                    
                    
                };
            }
            return current;
        }

        @Override
        public IntegerProperty sizeProperty() {
            if (size == null) {
                size = new SimpleIntegerProperty(this, "size", 1) {

                    @Override
                    protected void invalidated() {
                        int val = get();
                        if (val < 1) {
                            set(1);
                        }
                        updateCurrent();
                    }
                };
            }
            return size;
        }

        /**
         * called when size is changed: must enforce invariant 0 <= current < size.
         * 
         */
        protected void updateCurrent() {
            if (getCurrent() >= getSize()) {
                setCurrent(getSize() - 1);
            }
        }
        
    }
}
