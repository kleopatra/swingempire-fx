/*
 * Created on 22.01.2015
 *
 */
package de.swingempire.fx.util;

import java.util.List;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import de.swingempire.fx.demobean.Person;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
//@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class FXUtilsTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    private ObservableList<String> rawItems;
    
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

}
