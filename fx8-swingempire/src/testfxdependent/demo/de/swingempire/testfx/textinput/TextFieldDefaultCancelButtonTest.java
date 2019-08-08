/*
 * Created on 08.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.ButtonMatchers;

import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(Parameterized.class)
public class TextFieldDefaultCancelButtonTest extends ApplicationTest {

    private TextFieldDefaultCancelButtonPane root;
    
    /**
     * Remove the textformatter: then this should pass.
     * Doesn't - action is triggered twice ... already reported?
     */
    @Test
    public void testTextNoFormatterEscapeCancelButton() {
        root.field.setTextFormatter(null);
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(e -> actions.add(e));
        press(ESCAPE);
        //release(ESCAPE);
        assertEquals("cancel action must be triggered once", 1, actions.size());
    }
    
    /**
     * Remove the textformatter: check field text.
     */
    @Test
    public void testTextNoFormatter() {
        String text = root.field.getText();
        root.field.setTextFormatter(null);
        assertEquals(text, root.field.getText());
    }
    
    /**
     * Test that escape on uncommitted text does not trigger cancel button.
     * This passes as a side-effect of not firing at all if field 
     * has formatter
     */
    @Test
    public void testTextUncommittedChangeEscapeCancelButton() {
        String text = "some";
        root.field.appendText(text);
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(e -> actions.add(e));
        press(ESCAPE);
        //release(ESCAPE);
        
        assertEquals("uncommitted changes, cancel action must not be triggered", 0, actions.size());
        if (!(root.field.getSkin() instanceof XTextFieldSkin))
            fail("passing accidentally - cancel is never delivered to cancel button");
    }
    
    /**
     * Test that enter on unchanged text triggers default button.
     * This fails because TextFieldBehavior consumes the event 
     * if the field has a TextFormatter.
     */
    @Test
    public void testTextEscapeCancelButton() {
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(e -> actions.add(e));
        press(ESCAPE);
        //release(ESCAPE);
            
        assertEquals("cancel action must be triggered once", 1, actions.size());
    }
    
    /**
     * Test that enter on uncommitted text does not trigger default button.
     */
    @Test
    public void testTextUncommittedChangeEnterDefaultButton() {
        String text = "some";
        root.field.appendText(text);
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(e -> actions.add(e));
        press(ENTER);
//        release(ENTER);
        assertEquals("uncommitted changes, default action must not be triggered", 0, actions.size());
    }
    
    /**
     * Test that enter on unchanged text triggers default button.
     */
    @Test
    public void testTextEnterDefaultButton() {
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(e -> actions.add(e));
        press(ENTER);
//        release(ENTER);
        assertEquals("default action must be triggered once", 1, actions.size());
    }
    
    /**
     * Test that enter commits text.
     */
    @Test
    public void testTextAppendEnter() {
        String initial = root.field.getText();
        String text = "some";
        root.field.appendText(text);
        press(ENTER);
//        release(ENTER);
        assertEquals(initial + text, root.field.getText());
        assertEquals(initial + text, getValue(root.field));
    }
    
    /**
     * Test that appendText doesn't commit.
     */
    @Test
    public void testTextAppend() {
        String initial = root.field.getText();
        String text = "some";
        root.field.appendText(text);
        assertEquals(initial + text, root.field.getText());
        assertEquals(initial, getValue(root.field));
    }
    
    /**
     * @param field
     * @return
     */
    private <T> String getValue(TextField field) {
        TextFormatter<T> textFormatter = (TextFormatter<T>) field.getTextFormatter();
        StringConverter<T> valueConverter = textFormatter.getValueConverter();
        String formatterText = valueConverter.toString(textFormatter.getValue());
        return formatterText;
    }

    @Test
    public void testTextIsFocused() {
        verifyThat(root.field, NodeMatchers.isFocused());
    }
    
    @Test
    public void testIsCancelButton() {
        verifyThat(root.cancel, ButtonMatchers.isCancelButton());
    }
    
    @Test
    public void testIsDefaultButton() {
       verifyThat(root.ok, ButtonMatchers.isDefaultButton());
    }
    
    @Override
    public void start(Stage stage) {
        root = new TextFieldDefaultCancelButtonPane(skinProvider);
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Before
    public void setup() {
        
    }
    
    Function<TextField, TextFieldSkin> skinProvider;
    public TextFieldDefaultCancelButtonTest(Function<TextField, TextFieldSkin> skinProvider) {
        this.skinProvider = skinProvider;
    }
    
    @Parameters
    public static Collection<Function<TextField, TextFieldSkin>> skinProviders() {
        return  List.of(
                field -> new XTextFieldSkin(field), 
                field -> new TextFieldSkin(field)
                );
    }

    public static class TextFieldDefaultCancelButtonPane extends VBox { // fixme: don't extend a layout!
        
        protected TextField field;
        protected Button cancel;
        protected Button ok;
        
        public TextFieldDefaultCancelButtonPane() {
            this(null);
        }
        public TextFieldDefaultCancelButtonPane(Function<TextField, TextFieldSkin> skinProvider) {
            super(100);
            field = new TextField() {

                @Override
                protected Skin<?> createDefaultSkin() {
                    return skinProvider == null ? super.createDefaultSkin() : skinProvider.apply(this);
                }
                
            };
            TextFormatter<String> fieldFormatter = new TextFormatter<>(
                    TextFormatter.IDENTITY_STRING_CONVERTER, "textField ...");
            field.setTextFormatter(fieldFormatter);

            // add ok button
            ok = new Button("I'm the default");
            ok.setDefaultButton(true);
            ok.setOnAction(e -> LOG.info("triggered: " + ok.getText()));

            // add cancel button
            cancel = new Button("I'm the cancel");
            cancel.setCancelButton(true);
            cancel.setOnAction(e -> LOG.info("triggered: " + cancel.getText()));
            
            getChildren().addAll(field, cancel, ok);
            
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldDefaultCancelButtonTest.class.getName());
}
