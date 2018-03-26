/*
 * Created on 11.08.2017
 *
 */
package test.selection;

import java.util.Locale;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * TableView: Multiple selection lost on set item
 * 
 * reported as
 * https://bugs.openjdk.java.net/browse/JDK-8186904 
 * 
 * Here check ListView: okay
 * 
 */
public class ListViewMultipleSelectionLostOnSet extends Application {

    private int counter;

    private Parent getContent() {
        ListView<Locale> table = new ListView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales()));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Button set = new Button("Set");
        set.setOnAction(e -> table.getItems().set(3, new Locale("dummy " + counter++)));
        HBox buttons = new HBox(10, set);
        BorderPane pane = new BorderPane(table);
        pane.setBottom(buttons);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 800, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
