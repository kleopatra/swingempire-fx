/*
 * Created on 20.08.2019
 *
 */
package de.swingempire.testfx.textinput.comboforbug;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8229914
 * regression: eventfilter for keyPressed of ENTER not notified
 * 
 * Plain application.
 */
public class TextFieldInComboBug extends Application {

    public Parent createContent() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.getItems().addAll("something to choose", "another thingy to have");
        // regression of https://bugs.openjdk.java.net/browse/JDK-8145515
        // ENTER pressed not received
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("got key in editor filter: " + e);
        });
        
        VBox content = new VBox(10, comboBox);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldInComboBug.class.getName());

}
