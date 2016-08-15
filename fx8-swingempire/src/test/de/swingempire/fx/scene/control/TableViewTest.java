/*
 * Created on 27.06.2016
 *
 */
package de.swingempire.fx.scene.control;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TableViewTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    /**
     * Issue: leaf column notification before the added column is fully updated.
     */
    @Test
    public void testLeafColumnNotification() {
        TableView table = new TableView(); //Person.persons());
        TableColumn first = new TableColumn("First Name");
        table.getColumns().add(first);
        table.getVisibleLeafColumns().addListener((ListChangeListener) c -> {
            c.next();
            assertTrue(c.wasAdded());
            assertSame(table, ((TableColumn) c.getAddedSubList().get(0)).getTableView());
        });
        TableColumn last = new TableColumn("Last Name");
        table.getColumns().add(0, last);
    }
    
    protected TableView<Person> createTableWithColumns() {
        // filtered lists cannot be sorted (by TableView)
        FilteredList<Person> filtered = new FilteredList(Person.persons());
        SortedList<Person> sorted = new SortedList(filtered);
        TableView table = new TableView(sorted);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        
        TableColumn first = new TableColumn("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        table.getColumns().addAll(first);
        
      
        TableColumn emailHeader = new TableColumn("Emails");
        table.getColumns().addAll(emailHeader);
        
        TableColumn email = new TableColumn("Primary");
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn secondary = new TableColumn("Secondary");
        secondary.setCellValueFactory(new PropertyValueFactory<>("secondaryMail"));
        
        emailHeader.getColumns().addAll(email, secondary);
//        table.getColumns().addAll(email, secondary);
        
        // not filterable column
        javafx.scene.control.TableColumn last = new javafx.scene.control.TableColumn("Last Name");
        last.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        table.getColumns().addAll(last);
        return table;
    }

}
