/*
 * Created on 07.02.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.function.Consumer;
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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.skin.ComboBoxListViewSkin;

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