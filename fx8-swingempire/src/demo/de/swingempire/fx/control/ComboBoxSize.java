/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * combo sizing not fully configurable
 * http://stackoverflow.com/q/24852429/203657
 * 
 * wants to remove the gap at end .. 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxSize extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        ObservableList<String> items = FXCollections.observableArrayList("Some longish item that is the only one");
        ComboBox<String> box = new ComboBox<>(items);
        box.setValue(items.get(0));
        return box;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        scene.getStylesheets().add(getClass().getResource("comboboxsize.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
