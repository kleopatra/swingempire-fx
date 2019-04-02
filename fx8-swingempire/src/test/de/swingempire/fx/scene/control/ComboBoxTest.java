/*
 * Created on 07.02.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.time.LocalDate;
import java.util.logging.Logger;

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
import de.swingempire.fx.util.StageLoader;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.skin.ColorPickerSkin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.paint.Color;

/**
 * 
 * Quickcheck: https://bugs.openjdk.java.net/browse/JDK-8196827
 * NPE in tests
 * <p>
 * To make the tests fail, we need to hook into the uncaughtExceptionHandler to
 * catch errors in listeners.
 * 
 * <p>
 * This is copied from 
 * http://hg.openjdk.java.net/openjfx/9/rt/file/c734b008e3e8/modules/javafx.controls/src/test/java/test/javafx/scene/control/ComboBoxTest.java
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ComboBoxTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    ComboBox<String> comboBox;
    SelectionModel<String> sm;

//------------ combo action fired by skin    
    /**
     * quick test: action only fired after combo has a skin,
     * here by adding to the scenegraph via StageLoader
     * 
     * open issue: https://bugs.openjdk.java.net/browse/JDK-8087704
     * 
     * related issue: https://bugs.openjdk.java.net/browse/JDK-8133328
     * wants DatePicker to _not_ fire action if value changed programmatically
     * that's a wrong expectation because ComboBoxBase specifies action
     * behavior to be fired whenever the value changes
     */
    @Test
    public void testActionBeforeScenegraph() {
        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("one", "two"));
        IntegerProperty before = new SimpleIntegerProperty(0);
        IntegerProperty after = new SimpleIntegerProperty(0);
        cb.setOnAction(e -> before.set(1));
        cb.setValue(cb.getItems().get(0));
        new StageLoader(cb);
        cb.setOnAction(e -> after.set(1));
        cb.setValue(cb.getItems().get(1));
        assertEquals("after", 1, after.get());
        assertEquals("before", 1, before.get());
    }
    
    /**
     * quick test: action only fired after combo has a skin,
     * here by setting the skin at instantiation time
     */
    @Test
    public void testActionWithSkin() {
        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("one", "two"));
        IntegerProperty before = new SimpleIntegerProperty(0);
        cb.setOnAction(e -> before.set(1));
        cb.setSkin(new ComboBoxListViewSkin<>(cb));
        cb.setValue(cb.getItems().get(0));
        assertEquals("before", 1, before.get());
    }
    
    /**
     * same for ColorPicker
     */
    @Test
    public void testActionWithSkinColorPicker() {
        ColorPicker picker = new ColorPicker();
        IntegerProperty before = new SimpleIntegerProperty(0);
        picker.setOnAction(e -> before.set(1));
        picker.setSkin(new ColorPickerSkin(picker));
        picker.setValue(Color.RED);
        assertEquals("before", 1, before.get());
    }
    
    /**
     * Same for datePicker: no action before
     */
    @Test
    public void testActionBeforeScenegraphColorPicker() {
        ColorPicker cb = new ColorPicker();
        IntegerProperty before = new SimpleIntegerProperty(0);
        IntegerProperty after = new SimpleIntegerProperty(0);
        cb.setOnAction(e -> before.set(1));
        cb.setValue(Color.RED);
        new StageLoader(cb);
        cb.setOnAction(e -> after.set(1));
        cb.setValue(Color.BEIGE);
        assertEquals("after", 1, after.get());
        assertEquals("before", 1, before.get());
    }
    
    
    /**
     * same for datePicker
     */
    @Test
    public void testActionWithSkinDatePicker() {
        DatePicker picker = new DatePicker();
        IntegerProperty before = new SimpleIntegerProperty(0);
        picker.setOnAction(e -> before.set(1));
        picker.setSkin(new DatePickerSkin(picker));
        picker.setValue(LocalDate.now());
        assertEquals("before", 1, before.get());
    }

    /**
     * Same for datePicker: no action before
     */
    @Test
    public void testActionBeforeScenegraphDatePicker() {
        DatePicker cb = new DatePicker();
        IntegerProperty before = new SimpleIntegerProperty(0);
        IntegerProperty after = new SimpleIntegerProperty(0);
        cb.setOnAction(e -> before.set(1));
        cb.setValue(LocalDate.now());
        new StageLoader(cb);
        cb.setOnAction(e -> after.set(1));
        cb.setValue(LocalDate.now().minusDays(5));
        assertEquals("after", 1, after.get());
        assertEquals("before", 1, before.get());
    }

    
//------------- end combo action fired by skin
    
    @Test (expected = IllegalStateException.class)
    public void testPropertyWithThrowingListener() {
        Property p = new SimpleObjectProperty();
        p.addListener(e -> {throw new IllegalStateException();});
        p.setValue("high-five");
    }
    
    @Test (expected = IllegalStateException.class)
    public void testPropertyWithListener() {
        Property p = new SimpleObjectProperty() {
            
            @Override
            protected void invalidated() {
                throw new IllegalStateException();
            }
            
        };
        p.addListener(e -> LOG.info("nothing"));
        p.setValue("high-five");
    }
    
    @Test (expected = IllegalStateException.class)
    public void testProperty() {
        Property p = new SimpleObjectProperty() {

            @Override
            protected void invalidated() {
                throw new IllegalStateException();
            }
            
        };
        p.setValue("high-five");
    }
    
    @Test 
    public void ensureCanToggleShowing() {
        // using stageloader is fine
//        new StageLoader(comboBox);
        comboBox.show();
        assertTrue(comboBox.isShowing());
        comboBox.hide();
        assertFalse(comboBox.isShowing());

    }


    @Test
    public void ensureCanNotToggleShowingWhenDisabled() {
        comboBox.setDisable(true);
        comboBox.show();
        assertFalse(comboBox.isShowing());
        comboBox.setDisable(false);
        comboBox.show();
        assertTrue(comboBox.isShowing());
    }


    @Before 
    public void setup() {
        // this is possible, but moved into JavaFXThreadingRule
//        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
//            if (throwable instanceof RuntimeException) {
//                throw (RuntimeException)throwable;
//            } else {
//                Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);
//
////                throw new RuntimeException(throwable);
//            }
//            });
        comboBox = new ComboBox<String>();
//        comboBox.setSkin(new ComboBoxListViewSkin<>(comboBox));
        comboBox.setSkin(new XComboBoxListViewSkin<>(comboBox));
        sm = comboBox.getSelectionModel();
    }
    
    public static class XComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> {
        
        /**
         * @param combo
         */
        public XComboBoxListViewSkin(ComboBox<T> combo) {
            super(combo);
        }

        @Override
        public void show() {
            // poc - this should happen in positionAndShowPopup
            if (getSkinnable().getScene() == null) return;
            super.show();
        }
        
        
        
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxTest.class.getName());
}
