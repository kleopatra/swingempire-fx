/*
 * Created on 05.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * same as 
 */
public class ComboSelectBugRT_38886 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<StringBuilder> list = FXCollections.observableArrayList();
        list.add(new StringBuilder("0"));
        list.add(new StringBuilder("1"));
        list.add(new StringBuilder("2"));
        list.add(new StringBuilder("3"));
        list.add(new StringBuilder("4"));
        list.add(new StringBuilder("5"));
        list.add(new StringBuilder("6"));
        list.add(new StringBuilder("7"));
        list.add(new StringBuilder("8"));
        list.add(new StringBuilder("9"));

        ComboBox<StringBuilder> combo = new ComboBox<>(list);
        Button change = new Button("change");
        change.setOnAction((event) -> {
            list.set(5, new StringBuilder("-"));
        });

        Button remove = new Button("remove");
        remove.setOnAction(e -> {
            Object item = combo.getValue();
            list.remove(item);
            
        });
        Pane borderPane = new VBox(combo, change, remove);
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
