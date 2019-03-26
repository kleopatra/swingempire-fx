/*
 * Created on 26.03.2019
 *
 */
package de.swingempire.testfx.table;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import static de.swingempire.testfx.table.TableEditFactory.*;
import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.cell.TableViewAlwaysEditing.LineItem;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableEditTest extends ApplicationTest {

    TableView<EditLineItem> table;
    
    @Test
    public void testEditFirstCell() {
        
    }
    
    @Test
    public void testInitialTableConfig() {
        assertFalse("table must have columns", table.getColumns().isEmpty());
        assertFalse("table must have items", table.getItems().isEmpty());
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        table = createEditLineItemTable();
        table.setEditable(true);
        List<EditLineItem> items = Stream.generate(EditLineItem::new).limit(10).collect(Collectors.toList());
        // extractor on items
        ObservableList<EditLineItem> data = FXCollections.observableList(items, 
//                item -> new Observable[] {item.getString1Property()});
                item -> new Observable[] {item.textProperty()});
//        table.getItems().addAll(items);
        table.setItems(data);

        Pane root = new BorderPane(table);
        stage.setScene(new Scene(root));
        stage.show();
    }

    
}
