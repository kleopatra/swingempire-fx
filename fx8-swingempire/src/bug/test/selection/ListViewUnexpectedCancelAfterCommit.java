/*
 * Created on 05.09.2017
 *
 */
package test.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Reason: 
 * ListViewSkin cancels edits on modification of items
 * as "fix" for
 * https://bugs.openjdk.java.net/browse/JDK-8094887
 * 
 * Related issue:
 * https://bugs.openjdk.java.net/browse/JDK-8088542
 */
public class ListViewUnexpectedCancelAfterCommit extends Application {

    @Override
    public void start(Stage primaryStage) {
        ListView<String> simpleList = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        simpleList.setEditable(true);

        simpleList.setCellFactory(TextFieldListCell.forListView());

        simpleList.setOnEditStart(t -> {
            System.out.println(
                    "setOnEditStart " + t.getIndex() + " /" + t.getNewValue());
        });
        simpleList.setOnEditCommit(t -> {
            System.out.println(
                    "setOnEditCommit " + t.getIndex() + " /" + t.getNewValue());
            simpleList.getItems().set(t.getIndex(), t.getNewValue());

        });
        simpleList.setOnEditCancel(t -> {
            System.out.println(
                    "setOnEditCancel " + t.getIndex() + " /" + t.getNewValue());
        });

        BorderPane root = new BorderPane(simpleList);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
