/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * ListView: receives both editCommit (expected) and 
 * editCancel (unexpected) when edit committed
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8187307
 */
public class ListViewEditCancelOnCommitBug extends Application {

    @Override
    public void start(Stage primaryStage) {
        ListView<String> simpleList = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        simpleList.setEditable(true);
        simpleList.setCellFactory(TextFieldListCell.forListView());

        simpleList.addEventHandler(ListView.editStartEvent(), t -> 
            System.out.println(t.getEventType() + " on " + t.getIndex()));
        simpleList.addEventHandler(ListView.editCommitEvent(), t -> 
            System.out.println(t.getEventType() + " on " + t.getIndex()));
        simpleList.addEventHandler(ListView.editCancelEvent(), t -> 
            System.out.println(t.getEventType() + " on " + t.getIndex()));
 
        BorderPane root = new BorderPane(simpleList);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
