/*
 * Created on 25.05.2020
 *
 */
package de.swingempire.testfx.event;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.Assert.*;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/61970432/203657
 * working for TextField, not for VBox: latter needs explicit requestFocus
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class OnClickToRequestFocus extends ApplicationTest {

    
    Node toTest;
    boolean pressed = false;

    @Override
    public void start(Stage stage) {
//        toTest= new TextField();
        toTest= new VBox(new Label("we are doing quite right or not"));  //new Canvas(800,800));
        toTest.setOnKeyPressed(e -> {
                     System.out.println("Pressed");
                     pressed = true;
        });
        stage.setScene(new Scene((Parent) toTest));
        stage.show();
        stage.toFront();
    }

    @Test
    public void test_keyPressed_D() {
        clickOn(toTest);
        press(KeyCode.D);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(pressed);
    }
}