/*
 * Created on 18.09.2015
 *
 */
package de.swingempire.fx.scene.control;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static javafx.scene.control.TextFormatter.*;
import static org.junit.Assert.*;

import de.swingempire.fx.GlobalIgnores.IgnoreSpecUnclear;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.StageLoader;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TextFormatterTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

// ------------ dirty property in textField with formatter
    
    // field with empty text and identityformatter with initially empty string as value
    private TextField emptyField;
    
    public static <T> boolean isDirty(TextField field) {
        TextFormatter<T> textFormatter = (TextFormatter<T>) field.getTextFormatter();
        if (textFormatter == null || textFormatter.getValueConverter() == null) return false;
        String fieldText = field.getText();
        StringConverter<T> valueConverter = textFormatter.getValueConverter();
        String formatterText = valueConverter.toString(textFormatter.getValue());
        // todo: handle empty string vs. null value
        return !Objects.equals(fieldText, formatterText);
    }
    
    @Test
    public void testDirtyClearedOnCancel() {
        emptyField.appendText("some");
        emptyField.cancelEdit();
        assertFalse(isDirty(emptyField));
    }
    
    @Test
    public void testDirtyClearedOnCommit() {
        emptyField.appendText("some");
        emptyField.commitValue();
        assertFalse(isDirty(emptyField));
    }
    
    @Test
    public void testDirtyOnAppend() {
        emptyField.appendText("text");
        assertTrue("", isDirty(emptyField));
    }
    /**
     * Setting the formatter changes the field's text to the value of 
     * the formatter, expected notification from textfield
     */
    @Test
    public void testTextFieldInitialNotification() {
        String initial = "initial";
        String dummy = "dummy";
        TextField field = new TextField(dummy);
        ChangeReport report = new ChangeReport(field.textProperty());
        TextFormatter formatter = new TextFormatter(IDENTITY_STRING_CONVERTER, initial);
        field.setTextFormatter(formatter);
        assertEquals(1, report.getEventCount());
        assertEquals(dummy, report.getLastOldValue());
        assertEquals(initial, report.getLastNewValue());
    }
    
    /**
     * Setting the formatter changes the field's text to the value of 
     * the formatter.
     */
    @Test
    public void testTextFieldInitial() {
        String initial = "initial";
        TextField field = new TextField("dummy");
        TextFormatter formatter = new TextFormatter(IDENTITY_STRING_CONVERTER, initial);
        field.setTextFormatter(formatter);
        assertEquals(initial, field.getText());
    }
    
//------------ end dirty property  
    
    /**
     * Replace the text of the control with a text in the filter which is
     * longer than the current.
     */
    @Test
    public void testFormatterReplaceFilter() {
        String initial = "dummy";
        String replace = "1dummy1";
        UnaryOperator<Change> filter = c -> {
            if (c.isContentChange()) {
                c.setText(replace);
                c.setRange(0, initial.length());
            }
            return c;
        };
        TextField field = new TextField(initial);
        field.setTextFormatter(new TextFormatter(filter));
        field.replaceText(initial.length(), initial.length(), initial);
        assertEquals(replace, field.getText());
    }
    /**
     * Sanitiy test of semantics of replace: can replace with longer
     */
    @Test
    public void testReplaceText() {
        String initial = "dummy";
        TextField field = new TextField(initial);
        String replace = initial + initial;
        field.replaceText(0, initial.length(), replace);
        assertEquals(replace, field.getText());
    }
    
    /**
     * Test interaction between spinner/valuefactory and textFormatter
     * in editor. 
     */
    @Test
    @Ignore
    public void testSpinnerAndFormatter() {
        Spinner<Integer> spinner = new Spinner<>();
        TextFormatter<Integer> formatter = new TextFormatter<Integer>(
                new IntegerStringConverter(), 0);
//                filter);

        SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 10000, 0);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(formatter);
        assertSame(formatter.getValueConverter(), factory.getConverter());
    }
    
    
    /**
     * Doc error: undocumented IllegalStateException if StringConverter is null.
     * 
     */
    @Test
    @Ignore
    public void testTextFormatterConstructors() {
        new TextFormatter((UnaryOperator)null);
        new TextFormatter((StringConverter) null);
        new TextFormatter<Integer>(null, 5);
    }
    

    /**
     * Test issue:
     * https://bugs.openjdk.java.net/browse/JDK-8152557
     *
     * Can't replace behavior - final! No way to intercept ...?
     * Maybe replace mapping?
     */
    @Test
    public void testTextFormatterCommittedBeforeAction() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        StringProperty formatterValue = new SimpleStringProperty(formatter.getValue());
        IntegerProperty count = new SimpleIntegerProperty(0);
        field.setOnAction(e -> {
            count.set(count.get() + 1);
            formatterValue.set(formatter.getValue());
        });
        new StageLoader(field);
        field.replaceText(0, 1, "k");
        assertEquals(initialValue, formatter.getValue());
        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, field.getSkin(), "behavior");
        FXUtils.invokeGetMethodValue(TextFieldBehavior.class, behavior, "fire", KeyEvent.class, null);
        assertEquals(1, count.get());
        assertEquals(field.getText(), formatterValue.getValue());
    }
    
    /**
     * Test the hack: replace the event handler for 
     */
    @Test
    public void testTextFormatterCommittedBeforeActionHack() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        StringProperty formatterValue = new SimpleStringProperty(formatter.getValue());
        IntegerProperty count = new SimpleIntegerProperty(0);
        field.setOnAction(e -> {
            count.set(count.get() + 1);
            formatterValue.set(formatter.getValue());
        });
        new StageLoader(field);
        replaceEnter(field);
        field.skinProperty().addListener((src, ov, nv) -> {
            
        });
        field.replaceText(0, 1, "k");
        assertEquals(initialValue, formatter.getValue());
        fire(field);
        assertEquals(1, count.get());
        assertEquals(field.getText(), formatterValue.getValue());
    }

    /** 
     * Hack-around: text not committed on receiving action
     * https://bugs.openjdk.java.net/browse/JDK-8152557
     * 
     * A - reflectively replace the field's keyBinding to ENTER, must
     * be called after the skin is installed.
     * 
     * B - eventhandler that commits before firing the action
     * <p>
     * Note: we can't extend/change the skin's behavior, it's
     * final!
     * 
     * @param field
     */
    protected void replaceEnter(TextField field) {
        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, field.getSkin(), "behavior");
        InputMap inputMap = behavior.getInputMap();
        KeyBinding binding = new KeyBinding(KeyCode.ENTER);
        
        KeyMapping keyMapping = new KeyMapping(binding, e -> {
            e.consume();
            fire(field);
        });
        // note: this fails prior to 9-ea-108
        // due to https://bugs.openjdk.java.net/browse/JDK-8150636
        inputMap.getMappings().remove(keyMapping); 
        inputMap.getMappings().add(keyMapping);
    }
    
    protected void fire(TextField textField) {
        EventHandler<ActionEvent> onAction = textField.getOnAction();
        ActionEvent actionEvent = new ActionEvent(textField, null);
        // first commit, then fire
        textField.commitValue();
        textField.fireEvent(actionEvent);
        // PENDING JW: missing forwardToParent
    }


    /**
     * Test interaction between textFormatter and textField.
     * Here we change the value in formatter and check if Action
     * is fired.
     * 
     * No action fired, should it? It's doc'ed to be fired if
     * ENTER pressed.
     */
    @Test
    @ConditionalIgnore(condition = IgnoreSpecUnclear.class)
    public void testTextFormatterValueChangeAction() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        IntegerProperty count = new SimpleIntegerProperty(0);
        field.setOnAction(e -> {
            count.set(count.get() + 1);
        });
        String updatedValue = "updated";
        formatter.setValue(updatedValue);
        assertEquals("action must be fired?", 1, count.get());
    }
    
    /**
     * Test interaction between textFormatter and textField.
     * Here we change the value in formatter and verify update 
     * of text in field.
     */
    @Test
    public void testTextFormatterValueChange() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        String updatedValue = "updated";
        formatter.setValue(updatedValue);
        assertEquals("field must be updated on value change", updatedValue, field.getText());
    }

    /**
     * Test interaction between textFormatter and textField.
     * Change text in field, test effect on formatter.
     * 
     * Unexpected (missing doc): setText effectively commits!
     */
    @Test
    public void testTextFormatterSetText() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        String updatedValue = "updated";
        field.setText(updatedValue);
        // undocumented: setText updates value in formatter
        // assertEquals("formatter unchanged on setting text", initialValue, formatter.getValue());
        assertEquals("formatter must be updated on setting text", updatedValue, formatter.getValue());
    }
    
    /**
     * Test interaction between textFormatter and textField.
     * Change text in field, test effect on formatter.
     * 
     * field.appendText doesn't commit
     */
    @Test
    public void testTextFormatterAppendText() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        String updatedValue = "updated";
        field.appendText(updatedValue);
        assertEquals("formatter unchanged on appending text", initialValue, formatter.getValue());
        field.commitValue();
        assertEquals("formatter must be updated on text commit", initialValue + updatedValue, formatter.getValue());
    }
    
    /**
     * Test interaction between textFormatter and textField.
     * Setting textFormatter updates text in field.
     */
    @Test
    public void testTextFormatterSet() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        assertEquals("field must be updated on setting formatter", initialValue, field.getText());
    }
    
    @Before
    public void setup() {
        emptyField = new TextField();
        emptyField.setTextFormatter(new TextFormatter<>(IDENTITY_STRING_CONVERTER, ""));
        assertEquals("sanity: empty field", emptyField.getTextFormatter().getValue(), emptyField.getText());
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFormatterTest.class.getName());
}
