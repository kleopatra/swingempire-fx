/*
 * Created 06.01.2021
 */
package de.swingempire.fx.scene.control.table;

import java.util.Locale;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * problem: empty window when calling stage set/Max/Width
 */
public class TableViewMaxWidth extends Application {
    public static final int NUMBER_OF_COLUMNS = 30;

    private Parent createContent() {
        final TableView<Locale> tableView = new TableView<>();
        for (int i=0; i<NUMBER_OF_COLUMNS; i++) {
            tableView.getColumns().add(new TableColumn<>("Column " + i));
        }

        BorderPane content = new BorderPane(tableView);
        return content;
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
        stage.setMaxWidth(400);
//        stage.setWidth(400);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
