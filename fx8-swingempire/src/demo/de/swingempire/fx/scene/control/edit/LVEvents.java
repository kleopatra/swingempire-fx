/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8124615
 * https://bugs.openjdk.java.net/browse/JDK-8123783
 * 
 * Modified to handle ListView and test EditEvent.getIndex()
 */
public class LVEvents extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ObservableList<String> children = FXCollections.observableArrayList("item1", "item2");
        ListView<String> listView = new ListView<>(children);
        listView.setEditable(true);
        listView.setCellFactory(TextFieldListCell.forListView());

        final Label labelOnEditStartCounter = new Label("On edit start: ");
        final Label labelOnEditCancelCounter = new Label("On edit cancel: ");
        final Label labelOnEditCommitCounter = new Label("On edit commit: ");

        listView.setOnEditStart(t -> 
            labelOnEditStartCounter.setText("On edit start: " + String.valueOf(t.getIndex())));
                
        listView.setOnEditCommit(t -> 
                labelOnEditCommitCounter.setText("On edit commit: " + String.valueOf(t.getIndex())));

        listView.setOnEditCancel(t->
                labelOnEditCancelCounter.setText("On edit cancel: " + String.valueOf(t.getIndex())));
 
        VBox vBox = new VBox(10d);
        vBox.getChildren().addAll(labelOnEditStartCounter, labelOnEditCancelCounter, labelOnEditCommitCounter);

        HBox hBox = new HBox(15d);
        hBox.getChildren().addAll(listView, vBox);
        Scene scene = new Scene(hBox, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
}