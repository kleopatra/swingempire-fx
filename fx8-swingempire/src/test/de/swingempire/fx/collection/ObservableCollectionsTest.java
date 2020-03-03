/*
 * Created on 02.03.2020
 *
 */
package de.swingempire.fx.collection;

import org.junit.Test;

import javafx.collections.ObservableList;
import static javafx.collections.FXCollections.*;

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
}
