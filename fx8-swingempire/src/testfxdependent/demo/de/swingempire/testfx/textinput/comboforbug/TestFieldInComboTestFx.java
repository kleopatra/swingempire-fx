/*
 * Created on 06.09.2019
 *
 */
package de.swingempire.testfx.textinput.comboforbug;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.*;
import static javafx.scene.input.KeyCode.*;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8229914
 * regression: eventfilter for keyPressed of ENTER not notified
 * 
 * TestFX test.
 */
public class TestFieldInComboTestFx extends ApplicationTest {

    protected ComboBox<String> comboBox;

    @Test
    public void testEnterFilter() {
        List<KeyEvent> keyEvents = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, keyEvents::add);
        press(ENTER);
        assertEquals(1, keyEvents.size());
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = new TextFieldInComboBug().createContent();
        comboBox = (ComboBox<String>) root.lookup("ComboBox");
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

}
