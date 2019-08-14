/*
 * Created on 18.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * https://stackoverflow.com/q/51388408/203657
 * Default button always triggered, even when consuming the enter.
 * reported by OP https://bugs.openjdk.java.net/browse/JDK-8207759
 * 
 * What to expect? 
 * - node.setEventHandler(xxEvent) is doc'ed to be called as last handler in the chain
 * - all setOnXX delegate to that
 * - where in the dispatch chain do accelerators are triggered
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextInputWithDefaultButton extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Simple UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        
        // TextField
        TextField textField = new TextField();

//        textField.setOnAction(e -> {});
//        textField.getProperties().put("TextInputControlBehavior.disableForwardToParent", true);
        // Capture the [ENTER] key
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("-> Enter in set");
                event.consume();
            }
        });

//        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("-> Enter in add");
//                event.consume();
//            }
//            
//        });
//        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("-> Enter in add");
//                event.consume();
//            }
//            
//        });
        Spinner spinner = new Spinner();
        spinner.setEditable(true);
        spinner.getEditor().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("-> Enter");
                event.consume();
            }
        });

        Label label = new Label("focusable?");
        label.setFocusTraversable(true);
        label.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("-> Enter");
                event.consume();
            }
        });

        
        // Buttons
        Button btnCancel = new Button("Cancel");
        btnCancel.setCancelButton(true);
        btnCancel.setOnAction(e -> {
            System.out.println("-> Cancel");
//            primaryStage.close();
        });

        Button btnSave = new Button("Save");
        btnSave.setDefaultButton(true);
        btnSave.setOnAction(e -> {
            System.out.println("-> Save");
//            primaryStage.close();
        });

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(btnCancel, btnSave);

        root.getChildren().addAll(textField, spinner, label, buttonBar);

        Scene scene = new Scene(root);
        scene.getAccelerators().put(KeyCombination.keyCombination("ENTER"), () -> {
            System.out.println("-> accelerator");
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("Consume Event");
        primaryStage.show();
    }
}

