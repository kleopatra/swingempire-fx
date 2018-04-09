/*
 * Created on 23.01.2018
 *
 */
package de.swingempire.fx.fxml;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SampleFxml extends Application
{
        public static void main(String[] args) 
        {
                Application.launch(args);
        }
        
        @Override
        public void start(Stage stage) throws IOException
        {
                // Create the FXMLLoader 
                FXMLLoader loader = new FXMLLoader();
                // Path to the FXML File
//                String fxmlDocPath = "sample.fxml";
//                FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);

                // Create the Pane and all Details
//                VBox root = (VBox) loader.load(fxmlStream);

                VBox root = FXMLLoader.load(getClass().getResource("sample.fxml")
                        , ResourceBundle.getBundle("de.swingempire.fx.fxml.sample"));
                // Create the Scene
                Scene scene = new Scene(root);
                // Set the Scene to the Stage
                stage.setScene(scene);
                // Set the Title to the Stage
                stage.setTitle("A simple FXML Example");
                // Display the Stage
                stage.show();
        }
}
