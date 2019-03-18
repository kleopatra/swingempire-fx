/*
 * Created on 18.03.2019
 *
 */
package de.swingempire.fx.application;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class PreloaderExample extends Application {

    public static class MyPreloader extends Preloader {
        private Stage preloaderStage;
         
            @Override
            public void start(Stage primaryStage) throws Exception {
               this.preloaderStage = primaryStage;
         
               VBox loading = new VBox(20);
               loading.setMaxWidth(Region.USE_PREF_SIZE);
               loading.setMaxHeight(Region.USE_PREF_SIZE);
               loading.getChildren().add(new ProgressBar());
               loading.getChildren().add(new Label("Please wait..."));
         
               BorderPane root = new BorderPane(loading);
               Scene scene = new Scene(root);
         
               primaryStage.setWidth(800);
               primaryStage.setHeight(600);
               primaryStage.setScene(scene);
               primaryStage.show();
           }
         
           @Override
           public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
              if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
                 preloaderStage.hide();
              }
           }
        }

    private Parent createContent() {
        return null;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PreloaderExample.class.getName());


}
