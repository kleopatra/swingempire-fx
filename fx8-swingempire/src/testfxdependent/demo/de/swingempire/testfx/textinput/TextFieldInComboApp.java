/*
 * Created on 19.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.testfx.textinput.TextFieldInComboTest.TextFieldInComboPane;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * test fix https://bugs.openjdk.java.net/browse/JDK-8145515
 * textField in editable combo: custom enter filter not invoked.  
 * 
 * editor still (or again?) doesn't receive the ENTER .. or misunderstood
 * bug/fix?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldInComboApp extends Application {

    private Parent createContent() {
        TextFieldInComboPane pane = new TextFieldInComboPane();
        TextField editor = pane.comboBox.getEditor();
        // ENTER not received: ComboBoxPopupSkin registers a filter that blocks
        editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("got key in editor filter: " + e);
        });
        // ENTER received: we are sibling, so getting the it
        pane.comboBox.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("got key in combo filter: " + e);
        });
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 300, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldInComboApp.class.getName());

}
