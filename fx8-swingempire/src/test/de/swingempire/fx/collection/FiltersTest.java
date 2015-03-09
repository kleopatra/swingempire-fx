/*
 * Created on 12.02.2015
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import de.swingempire.fx.collection.Filters.EqualsPredicate;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FiltersTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    private ObservableList<String> items;
    private FilteredList filteredItems;

    @Test
    public void testIntStreamIntegerList() {
        ObservableList<Integer> intList = FXCollections.observableArrayList(1, 4, 5, 6);
        int[] intArray = new int[] {5, 7};
        // want an intArray that contains all of intList that's not in intArray
        // {1, 4, 6}
        ObservableList<Integer> expected = FXCollections.observableArrayList(1, 4, 6);
        int[] expectedArray = new int[] {1, 4, 6};
        List<Integer> toSet = Arrays.stream(intArray).boxed().collect(Collectors.toList());
        int[] rest = intList.stream().filter(p -> !toSet.contains(p)).mapToInt(v -> v.intValue()).toArray();
        
        List<Integer> result = intList.stream().filter(p -> !toSet.contains(p)).collect(Collectors.toList());
        
        
        assertEquals("expected list", expected, result);
//        assertEquals("expected array", expectedArray, rest);
    }
    
    @Test
    public void testPrimitiveToArrayStream() {
        ObservableList<Integer> intList = FXCollections.observableArrayList(1, 4, 5, 6);
        int[] toArray = intList.stream().mapToInt(Integer::intValue).toArray();
        assertEquals(intList.size(), toArray.length);
        for (int i = 0; i < toArray.length; i++) {
            assertEquals((int)intList.get(i), toArray[i]);
        }
    }
    
    @Test
    public void testPrimitiveToArray() {
        ObservableList<Integer> intList = FXCollections.observableArrayList(1, 4, 5, 6);
        int[] toArray = createIntArray(intList);
        assertEquals(intList.size(), toArray.length);
        for (int i = 0; i < toArray.length; i++) {
            assertEquals((int)intList.get(i), toArray[i]);
        }
    }
    
 
    private int[] createIntArray(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
            
        }
        return result;
    }
    
    @Test
    public void testPrimitiveToListStream() {
        int[] intArray = new int[] {1, 4, 5, 6};
        List<Integer> result = Arrays.stream(intArray).boxed().collect(Collectors.toList());
        assertEquals(intArray.length, result.size());
        for (int i = 0; i < intArray.length; i++) {
            assertEquals(intArray[i], (int) result.get(i));
        }
    }
    @Test
    public void testPrimitiveToList() {
        int[] intArray = new int[] {1, 4, 5, 6};
        List<Integer> result = createIntList(intArray);
        assertEquals(intArray.length, result.size());
        for (int i = 0; i < intArray.length; i++) {
            assertEquals(intArray[i], (int) result.get(i));
        }
    }
    private List<Integer> createIntList(int[] array) {
        List<Integer> result = new ArrayList<>();
        for (int i : array) {
            result.add(i);
        }
        return result;
    }
    @Test
    public void testEqualsPredicate() {
        String first = items.get(0);
        filteredItems.setPredicate(new EqualsPredicate<>(first));
        assertEquals(1, filteredItems.size());
    }

    @Before
    public void setup() {
        items = createObservableList(true);
        filteredItems = new FilteredList(items);
    }

    static final String[] DATA = {
        "9-item", "8-item", "7-item", "6-item", 
        "5-item", "4-item", "3-item", "2-item", "1-item"}; 

    protected ObservableList<String> createObservableList(boolean withData) {
        return withData ? FXCollections.observableArrayList(DATA)
                : FXCollections.observableArrayList();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FiltersTest.class
            .getName());
}
