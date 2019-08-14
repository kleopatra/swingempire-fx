/*
 * Created on 07.08.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.text.DecimalFormat;
import java.text.ParsePosition;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/50970411/203657
 * focused textfield with textformatter does not trigger default cancel button
 * 
 * 
 * To reproduce:
 * - focus id (that's the textField with a formatter)
 * - press esc 
 * - expected: dialog closed
 * - actual: nothing happens
 * 
 * This is the original question: when replacing default skin with XTextfieldSkin,
 * the problem is solved.
 * 
 * @see de.swingempire.fx.scene.control.skin.XTextFieldSkin
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldWithFormatterAndDefaultButton extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            TextField name = new TextField();
            HBox hb1 = new HBox();
            hb1.getChildren().addAll(new Label("Name: "), name);

            TextField id = new TextField() {

                @Override
                protected Skin<?> createDefaultSkin() {
                    return new XTextFieldSkin(this);
                }
                
            };
            id.setTextFormatter(getNumberFormatter()); // numbers only
            HBox hb2 = new HBox();
            hb2.getChildren().addAll(new Label("ID: "), id);

            VBox vbox = new VBox();
            vbox.getChildren().addAll(hb1, hb2);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Number Escape");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(vbox);

            Platform.runLater(() -> name.requestFocus());

            if (dialog.showAndWait().get() == ButtonType.OK) {
                System.out.println("OK: " + name.getText() + id.getText());
                
            } else {
                System.out.println("Cancel");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    TextFormatter<Number> getNumberFormatter() {
        // from https://stackoverflow.com/a/31043122
        DecimalFormat format = new DecimalFormat("#");
        TextFormatter<Number> tf = new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }
            ParsePosition parsePosition = new ParsePosition(0);
            Object object = format.parse(c.getControlNewText(), parsePosition);
            if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                return null;
            } else {
                return c;
            }
        });

        return tf;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

