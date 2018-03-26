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
import javafx.stage.Stage;

/**
 * not yet in 8u40b12 - could use for regression testing in custom ListViewSelection
 */
public class ListViewSelectionBugMinimal_38884 extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<String> items = FXCollections.observableArrayList();
//        ListView<String> table = new ListView<>(items);
        ListViewAnchored<String> table = new ListViewAnchored<>(items);
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
