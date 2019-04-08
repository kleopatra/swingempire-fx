/*
 * Created on 05.04.2019
 *
 */
package de.swingempire.testfx.treetable;

import java.util.Set;
import java.util.logging.Logger;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static de.swingempire.testfx.matcher.CellMatchers.*;
import static de.swingempire.testfx.util.TestFXUtils.*;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;
import static org.testfx.util.DebugUtils.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableNPEOnEmptyRowTest extends ApplicationTest {

    private TreeTableView<Model> treeTable;
    private TreeTableColumn<Model, String> column;
    
    @Test
    public void testDoubleClickNotEmptyHack() {
        runAndWaitForFx(() -> treeTable.setRowFactory(r -> new CanEmptyTreeTableRow<>()));
        Set<TreeTableRow> rows = tableRowsFor(treeTable, false);
        TreeTableRow<Model> row = rows.iterator().next();
        assertEquals("sanity: single not-empty row", 1, rows.size());
        assertTrue("sanity: selection empty", treeTable.getSelectionModel().isEmpty());
        doubleClickOn(row);
        assertEquals(0, treeTable.getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testDoubleClickNotEmpty() {
        Set<TreeTableRow> rows = tableRowsFor(treeTable, false);
        TreeTableRow<Model> row = rows.iterator().next();
        assertEquals("sanity: single not-empty row", 1, rows.size());
        assertTrue("sanity: selection empty", treeTable.getSelectionModel().isEmpty());
        doubleClickOn(row);
        assertEquals(0, treeTable.getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testDoubleClickEmptyHack() {
        runAndWaitForFx(() -> treeTable.setRowFactory(r -> new CanEmptyTreeTableRow<>()));
        Set<TreeTableRow> emptyRows = tableRowsFor(treeTable, r -> r.isEmpty());
        TreeTableRow<Model> row = emptyRows.iterator().next();
        doubleClickOn(row);
    }
    
    @Test
    public void testDoubleClickEmpty() {
        Set<TreeTableRow> emptyRows = tableRowsFor(treeTable, r -> r.isEmpty());
        TreeTableRow<Model> row = emptyRows.iterator().next();
        Point2D point = new Point2D(100, 100);
        LOG.info("row: " + row.getBoundsInLocal());
        doubleClickOn(row);
//        verifyThat(row, r -> !r.isEmpty(), saveScreenshot());
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        treeTable = new TreeTableView<>();
        TreeItem<Model> root = new TreeItem<>(new Model("Root"));
        treeTable.setRoot(root);
        
        column = new TreeTableColumn<>("Test");
        column.setCellValueFactory(value -> value.getValue().getValue().textProperty());

        treeTable.getColumns().addAll(column);
        
        BorderPane content = new BorderPane(treeTable);
        stage.setScene(new Scene(content));
        stage.show();
    }

    private static class CanEmptyTreeTableRow<T> extends TreeTableRow<T> {
        
        public CanEmptyTreeTableRow() {
            addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (e.getClickCount() == 2 && isEmpty()) {
                    e.consume();
                }
            });
        }
    }

    private class Model {
        private StringProperty text;

        Model(String text) {
            this.text = new SimpleStringProperty(text);
        }

        public StringProperty textProperty() {
            return text;
        }
    }   
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableNPEOnEmptyRowTest.class.getName());
}
