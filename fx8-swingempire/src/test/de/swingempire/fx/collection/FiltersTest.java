/*
 * Created on 12.02.2015
 *
 */
package de.swingempire.fx.collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.collection.Filters.EqualsPredicate;

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

}
