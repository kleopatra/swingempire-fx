/*
 * Created on 16.01.2020
 *
 */
package de.swingempire.fx.fxml;

import java.io.IOException;
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
public class TreeViewApp extends Application {

    private Parent createContent() throws IOException {
        return FXMLLoader.load(getClass().getResource("treeviewwithitems.fxml"));
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
            .getLogger(TreeViewApp.class.getName());

}
