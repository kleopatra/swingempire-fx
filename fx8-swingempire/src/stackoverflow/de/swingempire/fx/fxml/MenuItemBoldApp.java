/*
 * Created on 25.08.2019
 *
 */
package de.swingempire.fx.fxml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuItemBoldApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("menuitembold.fxml"));

        Scene scene = new Scene(root, 850.0, 650.0);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
