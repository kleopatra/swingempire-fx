/*
 * Created on 10.10.2017
 *
 */
package test.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * IOOB when re-selecting old index
 * https://bugs.openjdk.java.net/browse/JDK-8188899
 * 
 * happens in recent 131/144/152 versions of fx8, not in fx9
 */
public class ComboBoxTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ComboBox<Integer> integerComboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
                integerComboBox.getSelectionModel().select(0); 
                integerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> { 
                        if (newValue == 2) 
                                integerComboBox.getSelectionModel().select(oldValue); 
                });             
                
        stage.setScene(new Scene(integerComboBox));
        stage.show();
    }

}