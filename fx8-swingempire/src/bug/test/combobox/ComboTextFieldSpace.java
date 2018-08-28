/*
 * Created on 21.08.2018
 *
 */
package test.combobox;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Typing space in textfield hides popup
 * 
 * 
 */
public class ComboTextFieldSpace extends Application {
    
    @Override
    public void start(Stage stage) {
        HBox root = new HBox();

        ObservableList<String> items = FXCollections.observableArrayList(
                "One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten");
        ComboBox<String> cb = new ComboBox<String>(items);
        cb.setEditable(true);
        
        root.getChildren().addAll(cb);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboTextFieldSpace.class.getName());
}

