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

/**
 * https://stackoverflow.com/q/52754803/203657
 * bind pref width to a property
 * 
 * answer by fabian:
 * direct binding not supported, way around is to expose the 
 * property on the controller and bind to that.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SizeApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
      Parent root = FXMLLoader.load(getClass().getResource("gridpanesize.fxml"));
      Scene scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
    }

    public static void main(String[] args) {
      launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SizeApp.class.getName());
  }

