/*
 * Created on 05.09.2019
 *
 */
package de.swingempire.testfx.textinput;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8229914
 * regression: event filter on combo's editor not reached
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboEnterEditorFilter9914 extends Application {

    private Parent createContent() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.getItems().addAll("something to choose", "another thingy to have");
        // regression of https://bugs.openjdk.java.net/browse/JDK-8145515
        // ENTER not received
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            System.out.println("got pressed in editor filter: " + e);
        });
        
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            System.out.println("got released in editor filter: " + e);
        });

        VBox content = new VBox(10, comboBox);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
