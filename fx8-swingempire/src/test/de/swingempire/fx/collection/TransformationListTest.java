/*
 * Created on 05.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;
import com.sun.javafx.collections.ObservableListWrapper;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.property.PropertyIgnores.IgnoreReported;
import de.swingempire.fx.util.ListChangeReport;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.util.Callback;

/**
 * Tests around TansformationLists - trying to understand.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TransformationListTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    
    
    
    //-------------- decorate with transform
    
    @Test
    public void testChangeDecoratorNotify() {
        ObservableList<Person> source = Person.persons();
        ChangeDecorator<Person> decorator = new ChangeDecorator<>(source);
        ListChangeReport sourceReport = new ListChangeReport(source);
        ListChangeReport decoratorReport = new ListChangeReport(decorator);
        source.retainAll(source.get(0), source.get(3));
        assertEquals(sourceReport.getEventCount(), decoratorReport.getEventCount());
        Change sourceChange = sourceReport.getLastChange();
        Change decoratorChange = decoratorReport.getLastChange();
        assertChangeEquals(sourceChange, decoratorChange);
    }

    /**
     * @param s
     * @param t
     */
    protected void assertChangeEquals(Change s, Change t) {
        prettyPrint(s, true);
        prettyPrint(t, true);
        assertEquals(s.getList(), t.getList());
        assertTrue(getChangeCount(s) > 0);
        assertEquals(getChangeCount(s), getChangeCount(t));
        s.reset();
        t.reset();
        while(s.next() && t.next()) {
            assertEquals(s.getAddedSize(), t.getAddedSize());
            assertEquals(s.getAddedSubList(), t.getAddedSubList());
            assertEquals(s.getFrom(), t.getFrom());
            assertEquals(s.getRemoved(), t.getRemoved());
            assertEquals(s.getRemovedSize(), t.getRemovedSize());
            assertEquals(s.getTo(), t.getTo());
            assertEquals(s.wasAdded(), t.wasAdded());
            assertEquals(s.wasRemoved(), t.wasRemoved());
            assertEquals(s.wasReplaced(), t.wasReplaced());
            assertEquals(s.wasPermutated(), t.wasPermutated());
        }
    }
    
    @Test
    public void testChangeDecoratorSanity() {
        ObservableList<Person> source = Person.persons();
        ChangeDecorator<Person> decorator = new ChangeDecorator<>(source);
        assertEquals(source.size(), decorator.size());
        for (int i = 0; i < source.size(); i++) {
            assertEquals(i, decorator.getSourceIndex(i));
            assertEquals(i, decorator.getViewIndex(i));
            assertSame(source.get(i), decorator.get(i));
        }
    }
    
    

    // ----------- check what happens with add/remove

    @Test
    public void testReplace() {
        List backing = createPersons();
        MyObservableList list = new MyObservableList(backing);
        ListChangeReport report = new ListChangeReport(list);
        Person p = new Person("replaced-first", "replaced-last", "none");
        list.replace(2, p);
//        report.prettyPrintAll();
    }
    
    public static class MyObservableList<E> extends ObservableListWrapper<E> {

        public E replace(int index, E value) {
            beginChange();
            E old = remove(index);
            add(index, value);
            endChange();
            return old;
        }
        
        /**
         * @param list
         */
        public MyObservableList(List<E> list) {
            super(list);
        }

    }
    
//-------------- filteredListX
    @Test
    public void testFilteredXEmptyList() {
        ObservableList list = createObservableList(false);
        FilteredListX filtered = new FilteredListX(list, p -> true);
        filtered.setPredicate(p -> false);
    }
    
    @Test
    public void testFilteredXOne() {
        ObservableList<String> list = createObservableList(true);
        FilteredListX filtered = new FilteredListX(list, p -> true);
        ListChangeReport report = new ListChangeReport(filtered);
        // keep all except the third, equivalent to removing one item
        filtered.setPredicate(p-> p != list.get(2));
        Change c = report.getLastChange();
        c.next();
        assertTrue("expected: single remove but was: " + c, wasSingleRemoved(c));
    }
    
    @Test
    public void testFilteredListX() {
        ObservableList<String> list = createObservableList(true);
        FilteredListX filtered = new FilteredListX(list);
        List added = new ArrayList();
        for (int i = 1; i < list.size(); i +=2) {
            added.add(list.get(i));
        }
        ListChangeReport report = new ListChangeReport(filtered);
        filtered.setPredicate(p -> !added.contains(p));
//        prettyPrint(report.getLastChange());
        assertEquals(1, report.getEventCount());
        // unexpected in core: filtering fires a single replaced
        assertEquals("disjoint removes", added.size(), getChangeCount(report.getLastChange()));
        report.clear();
        filtered.setPredicate(null);
        assertEquals(1, report.getEventCount());
        assertEquals("disjoint adds", added.size(), getChangeCount(report.getLastChange()));
//        prettyPrint(report.getLastChange());
    }
    
    /**
     * Test that filtered accepts null.
     */
    @Test
    public void testFilteredListXNullPredicate() {
        ObservableList<String> list = createObservableList(true);
        FilteredListX filtered = new FilteredListX(list, p -> true) ;
        filtered.setPredicate(null);
        assertEquals(null, filtered.getPredicate());
    }
    
    @Test
    public void testFilteredXInitialPredicate() {
        ObservableList<String> list = createObservableList(true);
        Predicate predicate = p -> false;
        FilteredListX filtered = new FilteredListX(list, predicate);
        assertSame(predicate, filtered.getPredicate());
    }
    
    @Test
    public void testFilteredXInitialDefaultPredicateIsNull() {
        ObservableList<String> list = createObservableList(true);
        FilteredListX filtered = new FilteredListX(list);
        assertSame(null, filtered.getPredicate());
    }
    
    /**
     * Basically trying to test that all notification paths can cope with
     * null, so need a failing test ;)
     * This isn't good: filtered has no need to test again, simply removes.
     */
    @Test
    public void testFilteredXAddRemoveNullPredicate() {
        ObservableList list = createObservableList(true);
        List added = new ArrayList();
        for (int i = 1; i < list.size(); i +=2) {
            added.add(list.get(i));
        }
        FilteredListX filtered = new FilteredListX(list);
        assertEquals(list.size(), filtered.size());
        ListChangeReport report = new ListChangeReport(filtered);
        list.removeAll(added);
        assertEquals(list.size(), filtered.size());
        assertEquals(1, report.getEventCount());
        assertEquals("disjoint remove " , added.size(), getChangeCount(report.getLastChange()));
    }
    
    /**
     * Basically trying to test that all notification paths can cope with
     * null, so need a failing test ;)
     * This isn't good: filtered has no need to test again, simply removes.
     */
    @Test
    public void testFilteredXAddRemoveNullPredicateConsecuitive() {
        ObservableList list = createObservableList(true);
        List added = new ArrayList(list.subList(2, 5));
        FilteredListX filtered = new FilteredListX(list);
        assertEquals(list.size(), filtered.size());
        ListChangeReport report = new ListChangeReport(filtered);
        list.removeAll(added);
        assertEquals(list.size(), filtered.size());
        assertEquals(1, report.getEventCount());
        assertTrue("single remove " , wasSingleRemoved(report.getLastChange()));
    }
    
    /**
     * Testing removal of filtered items. Note: predicate != 0
     */
    @Test
    public void testFilteredXAddRemoveChange() {
        ObservableList list = createObservableList(true);
        List added = new ArrayList();
        for (int i = 1; i < list.size(); i +=2) {
            added.add(list.get(i));
        }
        Predicate contains = p -> added.contains(p);
        FilteredListX filtered = new FilteredListX(list, contains);
        assertEquals(added.size(), filtered.size());
        ListChangeReport report = new ListChangeReport(filtered);
        list.removeAll(added);
        assertEquals(0, filtered.size());
        assertEquals(1, report.getEventCount());
        assertTrue("single remove but was " + report.getLastChange(), wasSingleRemoved(report.getLastChange()));
//        prettyPrint(report.getLastChange());
    }
    
    @Test
    public void testFilteredXPredicate() {
        FilteredListX filtered = new FilteredListX(createObservableList(true));
        assertNull(filtered.getPredicate());
        assertSame(filtered, filtered.predicateProperty().getBean());
        assertEquals("predicate", filtered.predicateProperty().getName());
    }
    
    
//--------- advanced ListChange events (understanding better FilteredList)
    
    @Test
    public void testFilteredUpdateRemoved() {
        ObservableList<Person> persons = createPersons(p ->
            new Observable[]{p.lastNameProperty()});
        FilteredList<Person> filtered = persons.filtered(p -> true);
        Predicate<Person> predicate = p -> p.getLastName().startsWith("J");
        filtered.setPredicate(predicate);
        assertEquals(2, filtered.size());
        ListChangeReport report = new ListChangeReport(filtered);
        filtered.get(1).setLastName("nonJ");
        assertEquals(1, filtered.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed but was: " + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testFilteredUpdateAdded() {
        ObservableList<Person> persons = createPersons(p ->
            new Observable[]{p.lastNameProperty()});
        FilteredList<Person> filtered = persons.filtered(p -> true);
        Predicate<Person> predicate = p -> p.getLastName().startsWith("J");
        filtered.setPredicate(predicate);
        assertEquals(2, filtered.size());
        ListChangeReport report = new ListChangeReport(filtered);
        persons.get(8).setLastName("J");
        assertEquals(3, filtered.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single added but was: " + report.getLastChange(), 
                wasSingleAdded(report.getLastChange()));
    }
    
    @Test
    public void testFilteredUpdateUpdated() {
        ObservableList<Person> persons = createPersons(p ->
            new Observable[]{p.lastNameProperty()});
        FilteredList<Person> filtered = persons.filtered(p -> true);
        Predicate<Person> predicate = p -> p.getLastName().startsWith("J");
        filtered.setPredicate(predicate);
        assertEquals(2, filtered.size());
        ListChangeReport report = new ListChangeReport(filtered);
        filtered.get(1).setLastName("JJ");
        assertEquals(2, filtered.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single update but was: " + report.getLastChange(), 
                wasSingleUpdated(report.getLastChange()));
    }

    /**
     * Sanity: transform should throw IIOB if paramenter passed into
     * getSourceIndex is offRange - does but isn't documented.
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void testFilteredGetSourceIndexOffRange() {
        ObservableList<Person> persons = createPersons();
        FilteredList<Person> filtered = persons.filtered(p -> true);
        Predicate<Person> predicate = p -> p.getLastName().startsWith("J");
        filtered.setPredicate(predicate);
        filtered.getSourceIndex(filtered.size());
    }
    
    protected ObservableList<Person> createPersons(Callback<Person, Observable[]> extractor) {
        return FXCollections.observableList(createPersons(), extractor);
    }
    
    protected ObservableList<Person> createPersons() {
        ObservableList<Person> persons = FXCollections.observableArrayList(
                new Person("Jacob", "Smith", "jacob.smith@example.com"),
                new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                new Person("Ethan", "Williams", "ethan.williams@example.com"),
                new Person("Emma", "Jones", "emma.jones@example.com"),
                new Person("Lucinda", "Micheals", "lucinda.micheals@example.com"),
                new Person("Michael", "Brown", "michael.brown@example.com"),
                new Person("Barbara", "Pope", "barbara.pope@example.com"),
                new Person("Penelope", "Rooster", "penelope.rooster@example.com"),
                new Person("Raphael", "Adamson", "raphael.adamson@example.com")
                );
        return persons;
    }
    
    public static class LastNameComparator implements Comparator<Person> {

        Collator collator = Collator.getInstance();
        @Override
        public int compare(Person o1, Person o2) {
            return collator.compare(o1.getLastName(), o2.getLastName());
        }
        
    }
    
  //-------------- ListChange events
    /**
     * Signature of default method clashes with Collator:
     * 
     * <code><pre>
     * Collator implements Comparable<Object>
     * // default method in ObservableList
     * SortedList<E> default sorted(Comparator<E>)
     * // typing of sortedList
     * void setComparator<Comparator<? super E>> 
     * 
     *  // such that followning doesn't compile
     * SortedList<String> sorted = list.sorted(Collator.getInstance());
     * // compiles
     * SortedList<String> sorted = list.sorted();
     * sorted.setComparator(Collator.getInstance());
     * </pre></code>
     * 
     */
    @Test @Ignore
    public void testCompileListSorted() {
        ObservableList<String> list = createObservableList(true);
        SortedList<String> sorted = list.sorted();
        sorted.setComparator(Collator.getInstance());
    }
    
    /**
     * FilteredList throws on null predicate.
     * https://javafx-jira.kenai.com/browse/RT-39290
     * https://bugs.openjdk.java.net/browse/JDK-8095242
     * 
     * fixed in 8u40
     * http://hg.openjdk.java.net/openjfx/8u-dev/rt/rev/31c6447666a5
     */
    @Test
    public void testFilteredListNullPredicate() {
        ObservableList<String> list = createObservableList(true);
        FilteredList<String> filtered = list.filtered(p -> false);
        filtered.setPredicate(null);
    }
    
    @Test
    public void testFilteredListNullPredicateConstructor() {
        ObservableList<String> list = createObservableList(true);
        FilteredList<String> filtered = list.filtered(null);
    }
    
  
    /**
     * Expecting fine grained notification from filteredList
     * https://javafx-jira.kenai.com/browse/RT-39291
     * https://bugs.openjdk.java.net/browse/JDK-8092288
     * unchanged as of jdk9-ea-171
     */
    @Test
    @ConditionalIgnore (condition = IgnoreReported.class)
    public void testFilteredListOneFiltered() {
        ObservableList<String> list = createObservableList(true);
        FilteredList filtered = list.filtered(p -> true);
        ListChangeReport report = new ListChangeReport(filtered);
        // keep all except the third, equivalent to removing one item
        filtered.setPredicate(p-> p != list.get(2));
        Change c = report.getLastChange();
        c.next();
        assertTrue("expected: single remove but was: " + c, wasSingleRemoved(c));
    }
    
    /**
     * Expecting fine grained notification from filteredList
     * https://javafx-jira.kenai.com/browse/RT-39291
     * https://bugs.openjdk.java.net/browse/JDK-8092288
     * unchanged as of jdk9-ea-171
     */
    @Test
    @ConditionalIgnore (condition = IgnoreReported.class)
    public void testFilteredList() {
        ObservableList<String> list = createObservableList(true);
        FilteredList<String> filtered = list.filtered(null);
        List added = new ArrayList();
        for (int i = 1; i < list.size(); i +=2) {
            added.add(list.get(i));
        }
        
        ListChangeReport report = new ListChangeReport(filtered);
        filtered.setPredicate(p -> !added.contains(p));
        prettyPrint(report.getLastChange());
        assertEquals(1, report.getEventCount());
        // unexpected: filtering fires a single replaced
        assertEquals("disjoint removes", added.size(), getChangeCount(report.getLastChange()));
    }
    
    @Test
    public void testSortedListAdd() {
        ObservableList<String> list = createObservableList(true);
        SortedList<String> sorted = list.sorted();// doesn't compile: (Collator.getInstance());
        sorted.setComparator(Collator.getInstance());
        List added = new ArrayList();
        for (int i = 1; i < list.size(); i +=2) {
            String item = list.get(i);
            added.add(item.charAt(0) + item);
        }
        ListChangeReport report = new ListChangeReport(sorted);
        list.addAll(added);
//        prettyPrint(report.getLastListChange());
        assertEquals(1, report.getEventCount());
        assertEquals("disjoint adds", added.size(), getChangeCount(report.getLastChange()));
    }
    
    @Test
    public void testSortedList() {
        ObservableList list = createObservableList(true);
        SortedList sorted = new SortedList(list);
        ListChangeReport report = new ListChangeReport(sorted);
        sorted.setComparator(Collator.getInstance());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSinglePermutated(report.getLastChange()));
    }

    static final String[] DATA = {
        "9-item", "8-item", "7-item", "6-item", 
        "5-item", "4-item", "3-item", "2-item", "1-item"}; 

    protected ObservableList<String> createObservableList(boolean withData) {
        return withData ? FXCollections.observableArrayList(DATA)
                : FXCollections.observableArrayList();
    }

    @SuppressWarnings("unused")
    static final Logger LOG = Logger
            .getLogger(TransformationListTest.class.getName());
}
