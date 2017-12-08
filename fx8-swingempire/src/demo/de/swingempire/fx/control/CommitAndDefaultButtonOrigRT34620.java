/*
 * Created on 08.12.2017
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * [ComboBox, DatePicker] Buttons set to default/cancel are not reacting to ComboBox enter/esc keys
 * https://bugs.openjdk.java.net/browse/JDK-8096725
 * 
 * Original example of bug report (used by Leif for complete testing)
 */
public class CommitAndDefaultButtonOrigRT34620 extends Application {
    
    @Override public void start(Stage stage) {
        stage.setWidth(450);
        stage.setHeight(550);

        VBox vbox = new VBox(10);

        // MenuBar mb =
        //     new MenuBar(new Menu("_File", null,
        //                          new MenuItem("E_xit") { { setOnAction(e -> { System.exit(0); }); } }
        //                          ));

        ComboBox<String> box = new ComboBox<>();
        box.getItems().add("test");
        box.setEditable(true);

        DatePicker dp = new DatePicker();

        TextField textfield = new TextField();

        Button setActionButton = new Button("Set Action Handlers");
        setActionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                box.setOnAction(e -> {
                    System.err.println("ComboBox Action");
                });
                dp.setOnAction(e -> {
                    System.err.println("DatePicker Action");
                });
                textfield.setOnAction(e -> {
                    System.err.println("TextField Action");
                });
            }
        });

        Button defaultButton = new Button("OK");
        defaultButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println("OK");
            }
        });
        defaultButton.setDefaultButton(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println("Cancel");
            }
        });
        cancelButton.setCancelButton(true);

// Comment out this line for the second test
// box.onActionProperty().bind(defaultButton.onActionProperty());
// dp.onActionProperty().bind(defaultButton.onActionProperty());

        vbox.getChildren().addAll(/*mb, */box, dp, textfield, setActionButton, defaultButton, cancelButton);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
