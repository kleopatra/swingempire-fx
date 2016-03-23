/*
 * Created on 18.09.2015
 *
 */
package de.swingempire.fx.scene.control;

import java.util.function.UnaryOperator;

import org.junit.ClassRule;
import org.junit.Ignore;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
//@SuppressWarnings({ "unchecked", "rawtypes" })
public class TextFormatterTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

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
     * Test interaction between textFormatter and textField.
     * Here we change the value in formatter and check if Action
     * is fired.
     * 
     * No action fired, should it? It's doc'ed to be fired if
     * ENTER pressed.
     */
    @Test
    public void testTextFormatterValueChangeAction() {
        TextField field = new TextField();
        String initialValue = "initial";
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        IntegerProperty count = new SimpleIntegerProperty(0);
        field.setOnAction(e -> {
            count.set(count.get() + 1);
        });
        String updatedValue = "updated";
        formatter.setValue(updatedValue);
        assertEquals("action must be fired?", 1, count.get());
        assertEquals("field must be updated on value change", updatedValue, field.getText());
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
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
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
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
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
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
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
        TextFormatter<String> formatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER, initialValue);
        field.setTextFormatter(formatter);
        assertEquals("field must be updated on setting formatter", initialValue, field.getText());
    }
    
    
}
