/*
 * Created on 02.03.2020
 *
 */
package de.swingempire.fx.collection;

import org.junit.Test;

import javafx.collections.ObservableList;
import static javafx.collections.FXCollections.*;
import static org.junit.Assert.*;

import de.swingempire.fx.util.ListChangeReport;

import static de.swingempire.fx.util.FXUtils.*;
 
/**
 * Testing plain observableCollections, for comparison only.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ObservableCollectionsTest {

    /**
     * spec of change: in order to maintain correct 
     * indexes of the separate add/remove changes, 
     * these changes must be sorted by their from index.
     * 
     * trying to understand ..
     */
    @Test
    public void testMultipleChangesFrom() {
        ObservableList<String> data = observableArrayList("one", "two", "other");
        ListChangeReport report =  new ListChangeReport(data);
        data.add(0, "added");
        prettyPrint(report.getLastChange());
    }
    
    /**
     * Looking at indices in notifcation - getFrom is the index at the time of the modification, 
     * that is with the ordering constraints of the spec. F.i. for additions, 
     * assertSame(c.getAddedSubList().get(0), c.getList().get(c.getFrom())) 
     */
    @Test
    public void testRemoved() {
        ObservableList<Integer> data =  observableArrayList(1, 2, 3, 4, 5, 6);
        ListChangeReport report = new ListChangeReport(data);
        data.retainAll(5);
        prettyPrint(report.getLastChange());
    }
}
