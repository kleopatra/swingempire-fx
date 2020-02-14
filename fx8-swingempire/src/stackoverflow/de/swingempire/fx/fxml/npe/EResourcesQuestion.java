/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *https://stackoverflow.com/q/60198041/203657
 *NPE deep down in VirtualFlow ..
 * @author blj0011
 */
public class EResourcesQuestion extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader mainAppfXMLLoader = new FXMLLoader(
                    getClass().getResource("MainApp.fxml"));
            VBox root = mainAppfXMLLoader.load();
            // Todo set model
            MainAppController mainAppController = mainAppfXMLLoader
                    .getController();
            DataModel model = new DataModel();
            mainAppController.initModel(model);
            Scene mainScene = new Scene(root);
            stage.setScene(mainScene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

