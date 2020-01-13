/*
 * Created on 13.01.2020
 *
 */
package de.swingempire.fx.scene.css;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59710978/203657
 * check mark has whitish outline, how to get rid off it?
 * 
 * reproducible .. no idea, though
 */
public class CheckBoxMark extends Application{

    private Parent createContent() {
        CheckBox box = new CheckBox("nothing, really");
        
        box.setPrefHeight(100);
        box.setPrefWidth(100);
        BorderPane content =  new BorderPane(box);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 300, 100));
        URL uri = getClass().getResource("checkmark.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CheckBoxMark.class.getName());

}
