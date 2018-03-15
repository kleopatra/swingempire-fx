/*
 * Created on 15.03.2018
 *
 */
package de.swingempire.fx.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import javafx.util.StringConverter;
import static de.swingempire.fx.util.FunctionalConverter.*;
/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class ConverterTest {

    @Test (expected = UnsupportedOperationException.class)
    public void testFromConverstionWithoutConverter() {
        StringConverter<Person> converter = new FunctionalConverter<>(
                person -> person.getLastName());
        converter.fromString("dummy");
    }
    
    @Test
    public void testFactoryTwoParam() {
        StringConverter<Person> converter = asConverter(
                person -> person.getLastName(), s -> new Person(null, s));
        Person person = Person.persons().get(0);
        assertEquals("converted to lastname", person.getLastName(), converter.toString(person));
        String name = "dummy";
        assertNotNull(converter.fromString(name));
        assertEquals(name, converter.fromString(name).getLastName());
    }
    
    @Test
    public void testFactoryOneParam() {
        StringConverter<Person> converter = asConverter(person -> person.getLastName());
        Person person = Person.persons().get(0);
        assertEquals("converted to lastname", person.getLastName(), converter.toString(person));
    }
    
    @Test
    public void testConstructorTwoParam() {
        FunctionalConverter<Person> converter = new FunctionalConverter<>(
                person -> person.getLastName(), s -> new Person(null, s));
        Person person = Person.persons().get(0);
        assertEquals("converted to lastname", person.getLastName(), converter.toString(person));
        String name = "dummy";
        assertNotNull(converter.fromString(name));
        assertEquals(name, converter.fromString(name).getLastName());
    }
    
    @Test
    public void testConstructorOneParam() {
        FunctionalConverter<Person> converter = new FunctionalConverter<>(person -> person.getLastName());
        Person person = Person.persons().get(0);
        assertEquals("converted to lastname", person.getLastName(), converter.toString(person));
    }
    
    @Test (expected= NullPointerException.class)
    public void testConstructorTwoParamFirstMustNotBeNull() {
        FunctionalConverter<Person> converter = new FunctionalConverter<>(null, null);
    }
    
    @Test (expected= NullPointerException.class)
    public void testConstructorOneParamMustNotBeNull() {
        FunctionalConverter<Person> converter = new FunctionalConverter<>(null);
    }
}
