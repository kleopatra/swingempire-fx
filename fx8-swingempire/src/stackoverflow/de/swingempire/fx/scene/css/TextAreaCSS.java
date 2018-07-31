/*
 * Created on 31.07.2018
 *
 */
package de.swingempire.fx.scene.css;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Remove inner border of textArea when focused
 * https://stackoverflow.com/q/51594560/203657
 * 
 * it's set on the content
 * 
 * .text-area:focused .content {
 *      -fx-background-color:  white;
 *  }
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextAreaCSS extends Application {

    private Parent createContent() {
        TextArea textArea = new TextArea();
        
        BorderPane pane = new BorderPane(textArea);
        pane.setBottom(new TextField("something in here"));
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.getStylesheets().add(getClass().getResource("textarea.css").toExternalForm());
        stage.setScene(scene);

        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextAreaCSS.class.getName());

}
