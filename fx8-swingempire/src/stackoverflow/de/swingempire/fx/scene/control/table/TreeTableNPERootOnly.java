/*
 * Created on 05.04.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.cell.DefaultTreeTableCell;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * NPE on doubleClick on empty row: culprit is TreeTableRowBehavior that can't handle 
 * empty cells
 * https://stackoverflow.com/q/55530779/203657
 * 
 * Bug: reported by ??
 * https://bugs.openjdk.java.net/browse/JDK-8222454
 * Issue is 
 * 
 * answered by slaw (expanded on my comment ;)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableNPERootOnly extends Application {

    private TreeTableView<Model> treeTable;
    private TreeTableColumn<Model, String> column;
    
    private class Model {
        private StringProperty text;

        Model(String text) {
            this.text = new SimpleStringProperty(text);
        }

        public StringProperty textProperty() {
            return text;
        }

        public String getText() {
            return text.get();
        }

        @Override
        public String toString() {
            return getText();
        }
    }

    /**
     * TreeTableRow that hacks around the bug by consuming double-clicks (actually, pressed)
     * on empty cells.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class CanEmptyTreeTableRow<T> extends TreeTableRow<T> {
        
        public CanEmptyTreeTableRow() {
            addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (e.getClickCount() == 2 && isEmpty()) {
                    e.consume();
                }
            });
        }
    }
    
    private Parent createContent() {
        treeTable = new TreeTableView<>();
        TreeItem<Model> root = new TreeItem<>(new Model("Root"));
        treeTable.setRoot(root);
        
        column = new TreeTableColumn<>("Test");
        column.setCellValueFactory(value -> value.getValue().getValue().textProperty());

        treeTable.getColumns().addAll(column);
        
        // hack factory
        treeTable.setRowFactory(value -> new CanEmptyTreeTableRow<>());
        // if hacked, verify that custom empty cells receive events
        column.setCellFactory(value -> {
            TreeTableCell<Model, String> cell = new DefaultTreeTableCell<>() {
                {
                    addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                        LOG.info("receiving in cell: " + e.getClickCount());
                    });
                }
            };
            return cell;
        });
        
        BorderPane content = new BorderPane(treeTable);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableNPERootOnly.class.getName());

}
