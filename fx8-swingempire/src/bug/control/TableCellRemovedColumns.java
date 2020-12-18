/*
 * Created 18.12.2020 
 */

package control;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8258663
 * cells not removed when deleting columns
 * 
 * - happens if fixedCellSize is set (not without)
 * - most cells (in no longer available columns) are empty, some contain
 *   outdated content
 * - horizontal scrollBar is 
 *    - adjusted as expected (that is not showing if width >
 *      sum of still available columns) if the not existing columns are
 *      off at the time of removal
 *    - not adjusted if they are visible  
 */
public class TableCellRemovedColumns extends Application {

    private static final int ITEM_COUNT = 100;

    private static final int COLUMN_COUNT = 100;

    private static final int TAIL_LENGTH = 90;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        TableView<String> table = new TableView<>();
        table.getColumns().setAll(createColumns());
        table.setItems(FXCollections.observableArrayList());
        table.getItems().addAll(createItems());
        table.setFixedCellSize(24);

        Button buttonExecuteTestCase = new Button("Execute Test-Case");
        buttonExecuteTestCase.setOnAction(event -> {
            removeTailColumns(table);
        });

        ToolBar toolbar = new ToolBar(buttonExecuteTestCase);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(table);

        stage.setTitle("Hello World!");
        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }

    private static List<TableColumn<String, ?>> createColumns() {
        List<TableColumn<String, ?>> columns = new ArrayList<>(COLUMN_COUNT);
        for (int i = 1; i <= COLUMN_COUNT; i++) {
            String name = "C" + i;
            TableColumn<String, String> column = new TableColumn<>(name);
            column.setCellValueFactory(
                    cellDataFeatrues -> new SimpleStringProperty(
                            name + " " + cellDataFeatrues.getValue()));

            columns.add(column);
        }
        return columns;
    }

    private static List<String> createItems() {
        List<String> items = new ArrayList<>(ITEM_COUNT);
        for (int i = 1; i <= ITEM_COUNT; i++) {
            items.add("R" + i);
        }
        return items;
    }

    private static void removeTailColumns(TableView<String> table) {
        int from = table.getColumns().size() - TAIL_LENGTH;
        int to = table.getColumns().size();
        table.getColumns().remove(from, to);
    }

}