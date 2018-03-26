/*
 * Created on 10.10.2017
 *
 */
package de.swingempire.fx.scene.control;

import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListXDecoratedTest extends ListXTest {

    
    @Override
    protected void assertBaseCellType(ListCell cell) {
        assertTrue(cell instanceof ListXDecoratedCell);
    }

    /**
     * @return
     */
    @Override
    protected Callback<ListView<Person>, ListCell<Person>> createCellFactory(boolean editable) {
        return editable? 
                p -> new TextFieldListXCell(new DefaultStringConverter())
                : p -> new ListXDecoratedCell();
    }

}
