/*
 * Created on 22.01.2015
 *
 */
package de.swingempire.fx.util;

import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import javafx.beans.Observable;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
//@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class FXUtilsTest {
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    private ObservableList<String> rawItems;
    
    
    @Test
    public void testFormattedInputLogger() {
        String prefix = "record:";
        IntegerProperty counter = new SimpleIntegerProperty(0);
        IntegerExpression ex;
        Formatter formatter = new SimpleFormatter() {

            @Override
            public String format(LogRecord record) {
                counter.set(counter.get() + 1);
                return super.format(record);
            }
            
        };
        Logger input = getInputLogger(Level.FINE, formatter);
        TextField field = new TextField();
        new StageLoader(field);
        field.requestFocus();
        String ch = "A";
        KeyEvent ke = new KeyEvent(field, field, KeyEvent.KEY_PRESSED, null, ch, KeyCode.A, false, false, false, false);
        field.fireEvent(ke);
        assertEquals(1, counter.get());
    }
    @Test
    public void testCollectToObservableListExtractor() {
        List<Person> persons = Person.persons();
        ObservableList<Person> result = persons.stream().map(e -> e)
                .collect(toObservableList(person -> { 
                    return new Observable[]{person.firstNameProperty()};
                }));
        assertEquals("sanity", persons, result);
        ListChangeReport report = new ListChangeReport(result);
        persons.get(0).setFirstName("newName");
        assertEquals(1, report.getEventCount());
    }
    
    @Test
    public void testCollectToObservableListParam() {
        ObservableList<String> list = FXCollections.observableArrayList();
        List<String> result = rawItems.stream().map(e -> e)
                .collect(toObservableList(list));
        assertSame(list, result);
        assertEquals(rawItems, result);
    }
    
    @Test
    public void testCollectToObservableList() {
        List<String> result = rawItems.stream().map(e -> e).collect(toObservableList());
        assertTrue(result instanceof ObservableList);
        assertEquals(rawItems, result);
    }
    
    @Before
    public void setup() {
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FXUtilsTest.class.getName());

}
