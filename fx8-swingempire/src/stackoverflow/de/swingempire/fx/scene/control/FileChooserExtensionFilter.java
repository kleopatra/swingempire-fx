/*
 * Created on 04.02.2020
 *
 */
package de.swingempire.fx.scene.control;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60005768/203657
 * extension filter inconsistently showing with double extensions
 * 
 * it's native, nothing much to do?
 */
public final class FileChooserExtensionFilter extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        String[] initial = {"test.tar", "test", "test.tar.gz"};
        VBox content = new VBox(10);
        for (int i = 0; i < initial.length; i++) {
            int loc = i;
            Button chooser = new Button(initial[i]);
            chooser.setOnAction(e-> showChooser(stage, initial[loc]));
            content.getChildren().add(chooser);
        }
        stage.setScene(new Scene(content));
        stage.show();
    }   

    void showChooser(Stage stage, String initial) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("."));
        chooser.getExtensionFilters().addAll(
            new ExtensionFilter("zipped tar files", "*.tar.gz"));
        chooser.setInitialFileName(initial); // or "test" or "test.tar.gz"
        File result = chooser.showSaveDialog(stage);
        System.out.println("input: " + initial + " output " + (result != null ? result.getName() : result));
    }
}