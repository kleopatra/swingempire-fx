/*
 * Created on 09.07.2019
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class IncludeApp extends Application {

    private Parent createContent() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("includemain.fxml"));
        Parent root = loader.load();

        return root;
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
            .getLogger(IncludeApp.class.getName());

}
