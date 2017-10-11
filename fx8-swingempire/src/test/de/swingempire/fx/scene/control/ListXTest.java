/*
 * Created on 10.10.2017
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

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.StageLoader;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

/**
 * Testing ListXView and collaborators.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ListXTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    @Test
    public void testCellValueUpdateEditable() {
        ListXView<Person, String> list = createListXView(true);
        int index = 1;
        Person person = list.getItems().get(index);
        new StageLoader(list);
        String replaced = "replaced";
        person.setLastName(replaced);
        Cell cell = getCell(list, index);
        // real update happens during layout pass
        list.layout();
        assertEquals(person.getLastName(), cell.getText());
    }
    
    /**
     * Note: need to force a layout pass before asserting the 
     * update of item. Might get away without once we use
     * CellDecorator to access super.updateItem(-1)?
     * 
     */
    @Test
    public void testCellValueUpdate() {
        ListXView<Person, String> list = createListXView(false);
        int index = 1;
        Person person = list.getItems().get(index);
        new StageLoader(list);
        String replaced = "replaced";
        person.setLastName(replaced);
        Cell cell = getCell(list, index);
        // real update happens during layout pass, so need
        // to trigger before asserting the update
        // doesn't matter if before or after grabbing the cell
        list.layout();
        assertEquals(person.getLastName(), cell.getText());
    }
    
    @Test
    public void testCellValue() {
        ListXView<Person, String> list = createListXView(false);
        int index = 1;
        Person person = list.getItems().get(index);
        new StageLoader(list);
        Cell cell = getCell(list, index);
        assertEquals(person.getLastName(), cell.getText());
    }
    
    @Test
    public void testCellValueFactoryInstalled() {
        ListXView<Person, String> list = createListXView(false);
        int index = 1;
        Person person = list.getItems().get(index);
        ObservableValue ov = list.getCellValueFactory().call(person);
        assertEquals(person.lastNameProperty(), ov);
    }
    /**
     * Verify that factory methods are creating test lists as expected.
     */
    @Test
    public void testSetupNotEditable() {
        boolean editable = false;
        ListXView<Person, String> list = createListXView(editable);
        assertEquals(editable, list.isEditable());
        ListCell cell = list.getCellFactory().call(null);
        assertCellType(cell, editable);
        
    }

    /**
     * @param cell
     */
    protected void assertCellType(ListCell cell, boolean editable) {
        assertBaseCellType(cell);
        if (editable) {
          assertEditableCellType(cell);
        }
    }

    /**
     * @param cell
     */
    protected void assertEditableCellType(ListCell cell) {
        assertTrue(cell instanceof TextFieldListXCell);
    }
    
    protected void assertBaseCellType(ListCell cell) {
        assertTrue(cell instanceof ListXCell);
    }
    /**
     * Verify that factory methods are creating test lists as expected.
     */
    @Test
    public void testSetupEditable() {
        boolean editable = true;
        ListXView<Person, String> list = createListXView(editable);
        assertEquals(editable, list.isEditable());
        ListCell cell = list.getCellFactory().call(null);
        assertCellType(cell, editable);
    }

    protected ListXView<Person, String> createListXView(boolean editable) {
        ListXView<Person, String> listView = new ListXView<Person, String>();
        listView.setCellValueFactory(p -> p.lastNameProperty());
        listView.setCellFactory(createCellFactory(editable));
        listView.setEditable(editable);
        listView.setItems(Person.persons());
        return listView;
    }

    /**
     * @return
     */
    protected Callback<ListView<Person>, ListCell<Person>> createCellFactory(boolean editable) {
        return editable? 
                p -> new TextFieldListXCell(new DefaultStringConverter())
                : p -> new ListXCell();
    }
}
