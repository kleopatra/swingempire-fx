/*
 * Created on 11.02.2018
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
      Parent root = FXMLLoader.load(getClass().getResource("userui.fxml"));
      Scene scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
    }

    public static void main(String[] args) {
      launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(UserApp.class.getName());
  }

