/*
 * Created on 02.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-35586
 * actionHandler notified twice
 * 
 * fixed in 8u20
 */
public class EditableComboActionHandlerRT_35586 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final ComboBox<String> cb = new ComboBox<String>();
        cb.setEditable(true);
        cb.setOnAction(new EventHandler<ActionEvent>() {
            int counter = 0;

            @Override
            public void handle(ActionEvent event) {
                System.out.println(counter++);
            }
        });
        primaryStage.setScene(new Scene(cb));
        primaryStage.show();
    }
}
