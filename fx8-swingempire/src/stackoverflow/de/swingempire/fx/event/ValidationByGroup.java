/*
 * Created on 29.04.2019
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55892008/203657
 */
public class ValidationByGroup extends Application {   
    private static final PseudoClass PRESSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("pressed");

    private Button button;
    
    private boolean valid = false;
    
    @Override public void start(Stage primaryStage) {
        TextField textField = new TextField("Test");
        textField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                valid = false;
            }
            if (!nv) {
                System.out.println("TextField: focus lost");
//                showAlert("from focusLost");
            }        
        });        

        button = new Button("Button 1");
        button.setStyle("-fx-pressed-base: red;"); // to visually demonstrate "pressed" pseudocl. lock (in Modena)
        // to debug MouseEvents:
        button.addEventHandler(MouseEvent.ANY, e -> {
            if (e.getEventType() != MouseEvent.MOUSE_MOVED) {   // MOUSE_MOVED events too frequent and irrelevant
                System.out.println("Button 1 - MouseEvent handled: " + e.getEventType());
                if (e.getEventType() == MouseEvent.MOUSE_EXITED)  {
                    PseudoClass pseudo = PseudoClass.getPseudoClass("pressed");
                    button.pseudoClassStateChanged(pseudo, false);
                    button.disarm();
//                    LOG.info("in mouseHandler: " + button.getPseudoClassStates());
                }
            }
        });
        button.setOnAction(e -> {
            System.out.println("Button 1 action");
//            showAlert("from button");
        });

        VBox root = new VBox(5);
        root.setPadding(new Insets(5));
        root.getChildren().addAll(textField, button, new Button("Button 2"));
        Scene scene = new Scene(root, 300, 250);
        scene.focusOwnerProperty().addListener((src, ov, nv) -> {
            if (ov == textField && !valid) {
                Platform.runLater(() -> {
                    System.out.println("focusOwnerChanged from textField - new: " + nv);
                    showAlert("from focusLost");
                    textField.requestFocus();
                    valid = true;
//                PseudoClass pseudo = PseudoClass.getPseudoClass("pressed");
//              button.pseudoClassStateChanged(pseudo, false);
                    
                });
                
            }
        });
        
        primaryStage.setTitle("Test App");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
    }

    /**
     * @param text
     */
    protected void showAlert(String text) {
        Alert alert = new Alert(AlertType.INFORMATION, "Focus lost = validation simulation", ButtonType.OK);
        String old = "before " + button.getPseudoClassStates()
           + "\n " + text + alert.showAndWait();
        
        LOG.info(old + "\n pseudo: " + button.getPseudoClassStates());
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ValidationByGroup.class.getName());
}

