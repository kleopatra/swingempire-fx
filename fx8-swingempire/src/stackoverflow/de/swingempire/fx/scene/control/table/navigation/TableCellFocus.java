/*
 * Created on 15.08.2018
 *
 */
package de.swingempire.fx.scene.control.table.navigation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/51806252/203657
 * navigate to cell and focus (aka: edit) the control that represents the 
 * editing actor
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellFocus extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("tablecellfocus.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
