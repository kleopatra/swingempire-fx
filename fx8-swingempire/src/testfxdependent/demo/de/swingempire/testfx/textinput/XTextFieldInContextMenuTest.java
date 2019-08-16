/*
 * Created on 16.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.function.Function;

import org.junit.Test;

import static de.swingempire.testfx.util.TestFXUtils.*;
import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;
import static de.swingempire.fx.scene.control.skin.XTextFieldSkin.*;
import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.event.ActionEvent;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldInContextMenuTest extends TextFieldInContextMenuTest {

    /**
     * use extended consume: passing.
     */
    @Test
    public void testEnterActionHandlerXConsume() {
        root.textField.addEventHandler(ActionEvent.ACTION, action 
                -> consumeAction(action, root.textField));
        doOpenAndFocus();
        press(ENTER);
        assertNoActions();
    }

    
    /**
     * Passes for plain skin, fails with xSkin: doesn't know the 
     * disableForward flag.
     */
    @Override
    public void testEnterDisableForward() {
        super.testEnterDisableForward();
    }



    /**
     * plain consume: doesn't help, even without testFX firedEvents?
     */
    @Override
    public void testEnterActionHandlerConsume() {
        stopStoringFiredEvents((Stage) root.getScene().getWindow());
        super.testEnterActionHandlerConsume();
    }

    /**
     * No consume: passed on to ?
     */
    @Override
    public void testEnter() {
        stopStoringFiredEvents((Stage) root.getScene().getWindow());
        super.testEnter();
    }

    /**
     * Sanity: really xtextFieldSkin?
     */
    @Test
    public void testXTextFieldSkin() {
        doOpenAndFocus();
        Skin<?> skin = root.textField.getSkin();
        assertTrue("skin must be xSkin but was " + skin, skin instanceof XTextFieldSkin );
    }
    /**
     * Overridden to return XTextFieldSkin.
     */
    @Override
    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return XTextFieldSkin::new;
    }

}
