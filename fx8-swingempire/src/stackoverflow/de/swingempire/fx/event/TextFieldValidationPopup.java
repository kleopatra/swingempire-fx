/*
 * Created on 18.03.2020
 *
 */
package de.swingempire.fx.event;

import java.text.NumberFormat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * https://stackoverflow.com/q/60728966/203657 
 * disable auto-hiding of popup if
 * input != number
 */
public class TextFieldValidationPopup extends Application {

    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox();

        TextField textField = new TextField();
        Popup popup = new Popup();

        // solution by @adsl
        // listener on textProperty that toggles auto-hide
        textField.textProperty().addListener((src, ov, nv) -> {
            if (isNumber(nv)) {
                popup.setAutoHide(true);
                textField.setStyle(null);
            } else {
                popup.setAutoHide(false);
                textField.setStyle(
                        "-fx-border-color: red ; -fx-border-width: 1px ;");
            }
        });
        
//        popup.setAutoHide(true);
//        popup.getContent().clear();
        popup.getContent().addAll(textField);

        // not called at all ..
        // auto-hide not considered external close request?
        popup.setOnCloseRequest((WindowEvent event) -> {
            if (isNumber(textField.getText())) {
                System.out.println("is a number");
                textField.setStyle(null);
            } else {
                System.out.println("enter a number");
                textField.setStyle(
                        "-fx-border-color: red ; -fx-border-width: 1px ;");
                event.consume();
            }
        });

        Label label = new Label("click here");
        StackPane labelPane = new StackPane(label);

        label.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                if (!popup.isShowing()) {

                    popup.show(labelPane, event.getScreenX() + 10,
                            event.getScreenY());
                }
            }
        });

        root.getChildren().addAll(labelPane, new Label("nothing serious"));
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean isNumber(String text) {
        NumberFormat format = NumberFormat.getNumberInstance();
        boolean result = false;
        try {
            format.parse(text);
            result = true;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

