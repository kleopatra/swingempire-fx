/*
 * Created on 17.07.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.function.UnaryOperator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57076752/203657
 * position caret to custom location
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextCaretCustomLocation extends Application {


    public static void main(String[] args) {
        launch();
    }

    @SuppressWarnings("static-access")
    public void start(Stage stage) {
        stage.setTitle("Hello World!");
        TextField field = new TextField("hello world");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (change.isContentChange() && change.getControlNewText().length() > 5) {
                change.setCaretPosition(5);
            }
            return change;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
//        field.textProperty()
//                .addListener((ObservableValue<? extends String> observable,
//                        String oldValue, String newValue) -> {
//                    System.out.println(field.getCaretPosition());
//                    // we are in the middle of the internal update
//                    // so have to wait until the internals are stable again
//                    // but: don't - there's a better option with TextFormatter
//                    Platform.runLater(() -> {
//                        field.positionCaret(5);
//                    });
//                });

        StackPane root = new StackPane();
        root.getChildren().addAll(field);
        stage.setScene(new Scene(root, 250, 250));
        stage.show();

    }

}