/*
 * Created on 21.08.2019
 *
 */
package de.swingempire.fx.collection;

import java.util.Iterator;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static org.junit.Assert.*;

import de.swingempire.fx.GlobalIgnores.IgnoreDebug;
import de.swingempire.fx.util.ListChangeReport;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Tests around ObservableMap - trying to understand.
 * <p>
 * https://stackoverflow.com/q/57572988/203657 
 * goal: wrap an observableList
 * around the map values such that the list is updated when the values change.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MapTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private ObservableMap<Integer, String> map;
    private ObservableList<String> list;

    private MapBackedObservableList backedList;
    
//------------------------- map-backed list
    
    @Test
    public void testMapBackedNotification() {
        ListChangeReport report = new ListChangeReport(backedList);
        map.put(50, "very well");
        assertMapBackedListContent();
        assertEquals("backed list must have fired", 1, report.getEventCount());
    }
    @Test
    public void testMapBackedGet() {
        assertMapBackedListContent();
    }

    protected void assertMapBackedListContent() {
        Iterator<?> it = map.values().iterator();
        int index = 0;
        while (it.hasNext()) {
            Object next = it.next();
            assertEquals("get at index: " + index, next, backedList.get(index));
            index++;
        }
    }
    
    @Test
    public void testMapBackedSize() {
        assertEquals("size: ", map.size(), backedList.size());
    }
//-------------------------- manual listening    
    /**
     * Use listener on Map to manually update list on change.
     */
    @Test
    public void testSyncEmpty() {
        // single value
        map.remove(1);
        list.setAll(map.values());
        map.addListener((MapChangeListener) c -> {
            list.setAll(map.values());
        });
        // remove last
        map.remove(2);
        assertTrue(map.isEmpty());
        assertContainAll();
        assertSameSequenceStream();
    }
    
    /**
     * Use listener on Map to manually update list on change.
     */
    @Test
    public void testPutToMap() {
        map.addListener((MapChangeListener) c -> {
            list.setAll(map.values());
        });
        map.put(-1, "x");
        assertContainAll();
        assertSameSequenceStream();
    }
    /**
     * for each over map.values.stream must have same sequence as 
     * list values
     */
    @Test
    public void testSequenceSameStream() {
        assertSameSequenceStream();
    }

    protected void assertSameSequenceStream() {
        IntegerProperty count = new SimpleIntegerProperty(0);
        //LOG.info("list?" + list + "values: " + map.values());
        map.values().forEach(next -> {
            int index = count.get();
            assertEquals("value must be same at " + index + " " + next , next, list.get(index));
            count.set(++index);
        });
    }
    
    /**
     * iterator over map.values must have same sequence as 
     * list values
     */
    @Test
    public void testSequenceSame() {
        Iterator it = map.values().iterator();
        IntegerProperty count = new SimpleIntegerProperty(0);
        while (it.hasNext()) {
            int index = count.get();
            Object next = it.next();
            assertEquals("value must be same at " + index + " " + next , next, list.get(index));
            count.set(++index);
        }
    }
    
    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testNaivePutUpdateList() {
        map.put(3, "c");
        assertContainAll();
    }
    @Test
    public void testContainAllInitial() {
        assertContainAll();
    }

    protected void assertContainAll() {
        assertContainAll(map, list);
    }
    
    protected void assertContainAll(ObservableMap<Integer, String> map,
            ObservableList<String> list) {
        map.values().forEach(v -> assertTrue("map value must be contained in list: " + v, list.contains(v)));
        list.forEach(v -> assertTrue("list value must be contained in map: " + v, map.values().contains(v)));
    }

    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testSequence() {
        map.put(-1, "some");
        LOG.info("" + map);
    }

    /**
     * test state of list after updating map - naive expectation will fail
     * actually, it will fail already in naive sanity test of initial state: 
     * values is-a collection, equals will fail.
     */
    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testNaiveSync() {
        LOG.info("map: " + map);
        LOG.info("list: " + list);
        assertEquals("list initial", list, map.values());
        assertEquals("list initial", map.values(), list);
        //map = {1=a, 2=b}
        //list = [a, b]
        // ok

        map.put(3, "c");
        LOG.info("map: " + map);
        LOG.info("list: " + list);

        assertEquals("list after map update", map.values(), list);
        //map = {1=a, 2=b, 3=c}
        //list = [a, b]
        // no update    
    }
    
    /**
     * Test that map values and list are equals initially.
     * failing .. why? map.values is a collection, not a list ..
     */
    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testInitialEquals() {
        assertEquals("list initial", map.values(), list);
    }
    
    /**
     * Debug: why are those two lists not equal?
     * values is-a collection, not a list ...
     */
    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testEqualsDebug() {
        map.values().equals(list);
    }
    
    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testValueContained() {
        String value = map.get(1);
        assertTrue(list.contains(value));
    }
    
    /**
     * toString implemented in AbstractCollection: uses iterator
     * to walk the collection.
     */
    @Test
    @ConditionalIgnore(condition=IgnoreDebug.class)
    public void testToStringDebug() {
        String to = map.values().toString();
        LOG.info(to);
    }
    @Before
    public void setup() {
        map = FXCollections.observableHashMap();
        map.put(1, "a");
        map.put(2, "b");

        list = FXCollections.observableArrayList(map.values());
        backedList = new MapBackedObservableList<>(map);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(MapTest.class.getName());
}
