/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;

import static de.swingempire.testfx.util.TestFXUtils.*;
import static de.swingempire.fx.scene.control.skin.XTextFieldSkin.*;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Test textField behaviour in the context of default/cancel button.
 * 
 * This uses XTextFieldSkin.
 * @author Jeanette Winzenburg, Berlin
 * @see de.swingempire.fx.scene.control.skin.XTextFieldSkin
 */
public class XTextFieldDefaultCancelButtonTest
        extends TextFieldDefaultCancelButtonTest {

 // ---------- testing hack around interfering EventFilter   
    
    /**
     * Test that eventFilter on parent does interfere without using hack
     */
    @Test
    public void testTextNoFormatterParentEventFilterEnterDefaultButtonTriggered() {
        // limit the hack to when absolutely necessary
        // remove the eventFilter installed by TestFX
        stopStoringFiredEvents((Stage) root.getScene().getWindow());
        root.addEventFilter(EventType.ROOT, e -> {});
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, e -> {
//            consumeAction(e, root.field);
            e.consume();
        });
        
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        // release(ENTER);
        assertEquals(
                "enter with consuming action handler but no hack trigger default button",
                1, actions.size());
        
    }
    
    /**
     * Test that eventFilter on parent doesn't interfere (if using hack)
     */
    @Test
    public void testTextNoFormatterParentEventFilterEnterDefaultButtonNotTriggered() {
        // limit the hack to when absolutely necessary
        // remove the eventFilter installed by TestFX
        stopStoringFiredEvents((Stage) root.getScene().getWindow());
        root.addEventFilter(EventType.ROOT, e -> {});
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, e -> {
            // use utility method to consume
            consumeAction(e, root.field);
        });

        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        // release(ENTER);
        assertEquals(
                "enter with consuming action handler must not trigger default button",
                0, actions.size());
       
    }
    
    /**
     * Test the hack around eventFilters (let utility method in skin consume).
     * This should pass without the hack
     */
    @Test
    public void testTextNoFormatterWithActionHandlerEnterDefaultButtonConsumedBySkin() {
        // Runnable r = () -> {
        // };
        // TestFXUtils.runAndWaitForFx(r);
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, e -> {
            consumeAction(e, root.field);
        });

        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        // release(ENTER);
        assertEquals(
                "enter with consuming action handler must not trigger default button",
                0, actions.size());
    }
    
    /**
     * Test that the fired event is stored during dispatch and removed after.
     */
    @Test
    public void testTextActionFired() {
        root.field.addEventHandler(ActionEvent.ACTION, e -> {
            ActionEvent firedAction = (ActionEvent) root.field.getProperties().get(TEXT_FIELD_FIRED_ACTION);
            assertNotNull("fired event must be available during dispatch", firedAction);
            //assertEquals(e, firedAction); can't, here we are receiving a copy
            consumeAction(e, root.field);
            assertTrue("hack must consume the fired action", firedAction.isConsumed());
            assertTrue("hack must consume the passed-in action", e.isConsumed());
        });
        press(ENTER);
        assertNull("fired event must be removed after dispatch", 
                root.field.getProperties().get(TEXT_FIELD_FIRED_ACTION));
    }

    /**
     * Test that the fired action is null initially and on receiving
     * a cancel
     */
    @Test
    public void testTextActionFiredEmpty() {
        assertNull(root.field.getProperties().get(TEXT_FIELD_FIRED_ACTION));
        root.field.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            assertNull(root.field.getProperties().get(TEXT_FIELD_FIRED_ACTION));
        });
        press(CANCEL);
    }
//-------------- end hack testing
    
    /**
     * Remove the textformatter and register an action handler that consumes the
     * actionEvent default button should not be triggered.
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8207774
     * 
     * now: 
     * overridden to remove the eventFilter installed by ApplicationTest
     * before calling super.
     * 
     * was: 
     * Temporarily overridden to debug: works okay in app, fails here
     * Reason is the eventFilter on stage (registered for debugging by TestFX
     * to log all events). Leads to passing a copy of the action around, vs
     * the instance fired by the skin. Need to get really dirty: once an
     * eventFilter is added to a parent, it's not fully removed.
     * 
     */
    @Override
    @Test
    public void testTextNoFormatterWithActionHandlerEnterDefaultButton() {
        // limit the hack to when absolutely necessary
        stopStoringFiredEvents((Stage) root.getScene().getWindow());
        super.testTextNoFormatterWithActionHandlerEnterDefaultButton();
        // KEEP commented: might need to debug again ..
//        Runnable r = () -> {
//        };
//        TestFXUtils.runAndWaitForFx(r);
//        root.field.setTextFormatter(null);
//        root.field.addEventHandler(ActionEvent.ACTION, 
//                e -> {
//                    e.consume();
//                });
//        
//        List<ActionEvent> actions = new ArrayList<>();
//        root.ok.setOnAction(actions::add);
//        press(ENTER);
//        //release(ENTER);
//        assertEquals(
//                "enter with consuming action handler must not trigger default button", 0,
//                actions.size());
    }
    
    /**
     * Overriddent to return XTextFieldSkin.
     */
    @Override
    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return XTextFieldSkin::new;
    }

    
}
