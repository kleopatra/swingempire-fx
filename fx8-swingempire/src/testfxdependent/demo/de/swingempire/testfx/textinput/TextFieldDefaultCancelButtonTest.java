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
import static de.swingempire.testfx.util.TestFXUtils.*;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Test textField behaviour in the context of default/cancel button.
 * 
 * This uses plain core classes.
 * <p>
 *  
 * Note: using a parameterized test is working fine, but takes double the 
 * time for running .. so extended.
 *  
 * @author Jeanette Winzenburg, Berlin
 */
//@RunWith(Parameterized.class)
public class TextFieldDefaultCancelButtonTest extends ApplicationTest {

    protected TextFieldDefaultCancelButtonPane root;
    
    /**
     * https://bugs.openjdk.java.net/browse/JDK-8207759 default button triggered
     * even if enter is consumed
     */
    @Test
    public void testTextNoFormatterEnterHandlerConsume() {
        root.field.setOnKeyPressed(e -> {
            if (ENTER == e.getCode()) {
                e.consume();
                LOG.info("consumed: " + e);
            }
        });

        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        assertEquals("enter with consuming onKeyPressed must not trigger default button", 0, actions.size());
    }
    
    /**
     * Remove the textformatter and register an action handler
     * that consumes the actionEvent
     * cancel button should be  triggered once
     * 
     * Core doesn't pass - action is triggered twice ... already reported?
     */
    @Test
    public void testTextNoFormatterWithActionHandlerEscapeCancelButton() {
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, e -> e.consume());
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(e -> actions.add(e));
        press(ESCAPE);
        assertEquals("esc with consuming action handler must trigger cancel button", 1, actions.size());
    }
    
    /**
     * Remove the textformatter and register an action handler
     * that consumes the actionEvent
     * default button should not be triggered.
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8207774
     */     
    @Test
    public void testTextNoFormatterWithActionHandlerEnterDefaultButton() {
        root.field.setTextFormatter(null);
        root.field.addEventHandler(ActionEvent.ACTION, e -> e.consume());
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        release(ENTER);
        assertEquals("enter with consuming action handler must not trigger default button", 0, actions.size());
    }
    
    /**
     * Remove the textformatter and setOnAction, cancel button should be 
     * triggered
     * 
     * Core doesn't pass - action is triggered twice ... already reported?
     */
    @Test
    public void testTextNoFormatterWithOnActionEscapeCancelButton() {
        root.field.setTextFormatter(null);
        root.field.setOnAction(e -> {});
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(actions::add);
        press(ESCAPE);
        assertEquals("esc with onAction handler must trigger default button", 1, actions.size());
    }
    
    /**
     * Remove the textformatter and setOnAction: default button 
     * should not be triggered.
     */     
    @Test
    public void testTextNoFormatterWithOnActionEnterDefaultButton() {
        root.field.setTextFormatter(null);
        root.field.setOnAction(e -> {});
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        assertEquals("enter with onAction handler must not trigger default button", 0, actions.size());
    }
    
    /**
     * Remove the textformatter: then this should pass.
     * Core doesn't - action is triggered twice ... already reported?
     */
    @Test
    public void testTextNoFormatterEscapeCancelButton() {
        root.field.setTextFormatter(null);
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(actions::add);
        press(ESCAPE);
        assertEquals("esc without formatter must trigger cancel button", 1, actions.size());
    }
    
    /**
     * Remove the textformatter: then this should pass.
     * Core doesn't - action is triggered twice ... already reported?
     */
    @Test
    public void testTextNoFormatterEnterDefaultButton() {
        root.field.setTextFormatter(null);
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(actions::add);
        press(ENTER);
        assertEquals("enter without formatter must trigger default button", 1, actions.size());
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
        
        assertEquals("uncommitted changes, cancel action must not be triggered", 0, actions.size());
        if (!(root.field.getSkin() instanceof XTextFieldSkin))
            fail("passing accidentally - cancel is never delivered to cancel button");
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
        assertEquals("enter on uncommitted changes must not trigger default button", 0, actions.size());
    }
    
    /**
     * Test that esc on unchanged text triggers cancel button.
     * This fails because TextFieldBehavior consumes the event 
     * if the field has a TextFormatter.
     */
    @Test
    public void testTextEscapeCancelButton() {
        List<ActionEvent> actions = new ArrayList<>();
        root.cancel.setOnAction(e -> actions.add(e));
        press(ESCAPE);
        assertEquals("esc on unchanged text must trigger cancel button", 1, actions.size());
    }

    /**
     * Test that enter on unchanged text triggers default button.
     */
    @Test
    public void testTextEnterDefaultButton() {
        List<ActionEvent> actions = new ArrayList<>();
        root.ok.setOnAction(e -> actions.add(e));
        press(ENTER);
        assertEquals("enter on unchanged text must trigger default button", 1, actions.size());
    }
  
//--------------------- sanity testing of ui state   
    /**
     * Remove the textformatter: check field text is unchanged.
     */
    @Test
    public void testTextNoFormatter() {
        String text = root.field.getText();
        root.field.setTextFormatter(null);
        assertEquals(text, root.field.getText());
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
        assertEquals(initial + text, getFormatterValue(root.field));
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
        assertEquals(initial, getFormatterValue(root.field));
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
    
    /**
     * Returns the value of the formatter of a textfield, converted to a String by
     * its converter. The formatter must not be null.
     * 
     * @param field the TextField 
     * @return the value of the formatter converted to a string by
     *   its converter
     * @throws NullPointerException if the field has no formatter  
     */
    protected <T> String getFormatterValue(TextField field) {
        TextFormatter<T> textFormatter = (TextFormatter<T>) field.getTextFormatter();
        StringConverter<T> valueConverter = textFormatter.getValueConverter();
        String formatterText = valueConverter.toString(textFormatter.getValue());
        return formatterText;
    }

    @Override
    public void start(Stage stage) {
        stopStoringFiredEvents(stage);
        root = new TextFieldDefaultCancelButtonPane(getSkinProvider());
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return TextFieldSkin::new;
    }
    
    @Before
    public void setup() {
        
    }
    
    // fields/methods needed when using parameterized test 
//    Function<TextField, TextFieldSkin> skinProvider;
//    public TextFieldDefaultCancelButtonTest(Function<TextField, TextFieldSkin> skinProvider) {
//        this.skinProvider = skinProvider;
//    }
//    
//    @Parameters
//    public static Collection<Function<TextField, TextFieldSkin>> skinProviders() {
//        return  List.of(
//                field -> new XTextFieldSkin(field), 
//                field -> new TextFieldSkin(field)
//                );
//    }

    /**
     * TestUI to exercise: single textfield, default and cancel button. 
     * The textField starts with having a textFormatter and no action handler. 
     * 
     * @author Jeanette Winzenburg, Berlin
     */
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
