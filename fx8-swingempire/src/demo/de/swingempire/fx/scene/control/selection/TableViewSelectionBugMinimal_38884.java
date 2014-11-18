/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * not yet in 8u40b12 - could use for regression testing in custom ListViewSelection
 */
public class TableViewSelectionBugMinimal_38884 extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<String> items = FXCollections.observableArrayList();
        TableView<String> table = new TableView<>(items);
        table.getSelectionModel().getSelectedItems().addListener((Change<? extends String> c) -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    System.out.println(c.getRemoved());
                }
            }
        });

        items.add("foo");
        table.getSelectionModel().select(0);
        items.clear();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
