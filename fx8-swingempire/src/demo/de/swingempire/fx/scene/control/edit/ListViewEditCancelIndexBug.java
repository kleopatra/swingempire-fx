/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * ListView: EditEvent on cancel has incorrect index
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8187226
 */
public class ListViewEditCancelIndexBug extends Application {

    private int editIndex;

    @Override
    public void start(Stage primaryStage) {
        ListView<String> simpleList = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        simpleList.setEditable(true);
        simpleList.setCellFactory(TextFieldListCell.forListView());

        simpleList.setOnEditStart(t -> editIndex = t.getIndex());

        simpleList.setOnEditCancel(t -> {

            if (editIndex != t.getIndex())
                System.out.println("expected index: " + editIndex + " actual: "
                        + t.getIndex());
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
