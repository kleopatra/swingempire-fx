/*
 * Created on 06.05.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Original problem: combo popup closed when adding items
 * http://stackoverflow.com/q/30073165/203657
 * 
 * Real problem: editing canceled when adding items (or other modifications to
 * the list) - see with simple TextFieldTableCell
 * 
 * With combo, as of fx9: 
 * - committed when editing and click add-button: comboCell has focusListener
 *   that tries to commit 
 * - cancelled when editing and thread adds items
 * 
 * with textfield: always cancelled - has no focusListener
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableWithComboCell extends Application implements Runnable {

    private int count;

    @Override
    public void start(Stage stage) {
        final TableView<MenuItem> table = new TableView<>();
        final TableColumn<MenuItem, String> column = new TableColumn<>("Name");
        table.setEditable(true);
        column.setEditable(true);

        table.setItems(FXCollections.observableArrayList(new MenuItem("my"),
                new MenuItem("initial"), new MenuItem("items")));
        column.setCellValueFactory(new PropertyValueFactory("text"));

        // column.setCellFactory(cell -> new EditableComboBoxCell());

        column.setCellFactory(item -> {
            ComboBoxTableCell cell = new ComboBoxTableCell("other", "dummy",
                    "option");
            // new MenuItem("other"), new MenuItem("dummy"), new
            // MenuItem("option"));
            cell.setComboBoxEditable(true);
            return cell;
        });

//        column.setCellFactory(TextFieldTableCell.forTableColumn());
        table.getColumns().add(column);

        Button thread = new Button("start adding thread");
        thread.setOnAction(e -> startAddingThread(table));

        Button add = new Button("Add");
        add.setOnAction(e -> {
            table.getItems().add(new MenuItem("button " + count++));
        });
        HBox box = new HBox(10, add, thread);
        BorderPane pane = new BorderPane(table);
        pane.setBottom(box);
        Scene scene = new Scene(pane);

        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();

    }

    /**
     * @param table
     */
    protected void startAddingThread(final TableView<MenuItem> table) {
        // Simple thread loop to demonstrate adding items

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(5000);
                    Platform.runLater(() -> table.getItems()
                            .add(new MenuItem("thread " + count++)));
                }

            } catch (InterruptedException e) {
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        launch();
    }

    public static class EditableComboBoxCell extends TableCell<String, String> {

        private ComboBox<Object> comboBox;

        @Override
        public void startEdit() {
            super.startEdit();
            comboBox = new ComboBox<>();
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(comboBox);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            comboBox = null;
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setText(getItem());
        }

        @Override
        public void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(comboBox);
                } else {
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setText(getItem());
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

