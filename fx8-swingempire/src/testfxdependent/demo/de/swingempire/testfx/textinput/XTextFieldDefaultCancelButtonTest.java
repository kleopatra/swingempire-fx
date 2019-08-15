/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import static de.swingempire.testfx.util.TestFXUtils.*;
import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import de.swingempire.testfx.util.TestFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
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

    /**
     * Remove the textformatter and register an action handler that consumes the
     * actionEvent default button should not be triggered.
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8207774
     * 
     * Temporarily overridden to debug: works okay in app, fails here
     * Reason is the eventFilter on stage (registered for debugging by TestFX
     * to log all events). Leads to passing a copy of the action around, vs
     * the instance fired by the skin. Need to get really dirty: once an
     * eventFilter is added to a parent, it's not fully removed.
     */
    @Override
    @Test
    public void testTextNoFormatterWithActionHandlerEnterDefaultButton() {
        // limit the hack to when absolutely necessary
        stopStoringFiredEvents((Stage) root.getScene().getWindow());

        Runnable r = () -> {
        };
        TestFXUtils.runAndWaitForFx(r);
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, 
                e -> {
                    e.consume();
                });
        
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        //release(ENTER);
        assertEquals(
                "enter with consuming action handler must not trigger default button", 0,
                actions.size());
    }
    
    /**
     * Test the hack around eventFilters (let utility method in skin consume). 
     * This should pass without the hack
     */
    @Test
    public void testTextNoFormatterWithActionHandlerEnterDefaultButtonConsumedBySkin() {
//        Runnable r = () -> {
//        };
//        TestFXUtils.runAndWaitForFx(r);
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, 
                e -> {
                    XTextFieldSkin.consumeAction(e, root.field);
                });

        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        //release(ENTER);
        assertEquals(
                "enter with consuming action handler must not trigger default button", 0,
                actions.size());
    }

    
    @Override
    public void start(Stage stage) {
        //stopStoringFiredEvents(stage);
        super.start(stage);
    }


    @Override
    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return XTextFieldSkin::new;
    }

    
}
