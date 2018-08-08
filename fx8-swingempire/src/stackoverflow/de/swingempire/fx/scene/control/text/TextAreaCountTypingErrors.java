/*
 * Created on 08.08.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.text.ParsePosition;
import java.util.function.UnaryOperator;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TextAreaCountTypingErrors extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox(5);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        IntegerProperty errorCount = new SimpleIntegerProperty(0);

        String target = "This is the sample text to be typed correctly.";
        Label instructions = new Label(target);

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);

        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);

        Label errorsLabel = new Label();
        hbox.getChildren().addAll(new Label("Errors:"), errorsLabel);

        // Bind the label to errorcount
        errorsLabel.textProperty().bind(errorCount.asString());

        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isAdded()) {
                int pos = c.getRangeStart();
                if (!c.getText().equals(target.substring(pos, pos + 1))) {
                    errorCount.set(errorCount.get() +1);
                }
            }
            return c;
        };

        textArea.setTextFormatter(new TextFormatter<>(filter));
        // Listen for changes to the textArea text and check again target string
//        textArea.textProperty().addListener((observableValue, s, newValue) -> {
//            if (newValue != null) {
//                errorCount.set(getErrorCount(target, newValue));
//            }
//        });

        root.getChildren().addAll(instructions, textArea, hbox);
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    private int getErrorCount(String target, String entered) {

        int errors = 0;

        // Compare each character in the strings
        char[] targetChars = target.toCharArray();
        char[] enteredChars = entered.toCharArray();

        // Starting at the beginning of the entered text, check that each character, in order, matches the target String
        for (int i = 0; i < enteredChars.length; i++) {
            if (enteredChars[i] != targetChars[i]) {
                errors++;
            }
        }

        return errors;
    }
}

