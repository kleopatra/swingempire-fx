/*
 * Created on 09.02.2018
 *
 */
package de.swingempire.fx.scene;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Notification on close of window:
 * - onClose is for external close requests (vetoable by consuming)
 * - onHidden is called always (not vetoable)
 * 
 * https://stackoverflow.com/q/48689985/203657
 */
public class OnCloseHandler extends Application { 
    public static void main(String[] args) {
        launch(args);
    }   
    @Override
    public void start(Stage primaryStage) {     
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {          
            primaryStage.close();
        });     
        primaryStage.setOnHidden(e -> {
            System.out.println("stage hidden");
        });
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("onCloseRequest handler called!");
//            e.consume();
        });

        StackPane rootPane = new StackPane();
        rootPane.getChildren().add(closeButton);        
        primaryStage.setScene(new Scene(rootPane, 300, 250));       
        primaryStage.show();
    }
}

