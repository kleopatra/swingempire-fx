/*
 * Created on 16.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test https://bugs.openjdk.java.net/browse/JDK-8207385
 * action of menuItem above custom item containing a textfield is triggered 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldInContextMenuTest extends ApplicationTest {

    protected TextFieldInContextMenuPane root;
  
    @Test
    public void testEnterActionHandlerConsume() {
        root.textField.addEventHandler(ActionEvent.ACTION, action -> action.consume());
        doOpenAndFocus();
        press(ENTER);
        assertNoActions();
    }
    
    /**
     * Workaround: disable forwarding
     * passing for core, failing for x
     */
    @Test
    public void testEnterDisableForward() {
        root.textField.getProperties().put("TextInputControlBehavior.disableForwardToParent", true);
        doOpenAndFocus();
        press(ENTER);
        assertNoActions();
    }
    
    @Test
    public void testEnter() {
        doOpenAndFocus();
        press(ENTER);
        assertNoActions();
    }
    
    /**
     * asserts that no actions on the other menuItems are triggered.
     */
    protected void assertNoActions() {
        assertEquals("hello actions must not be triggered", 0, root.helloActions.size());
        assertEquals("world actions must not be triggered", 0, root.worldActions.size());
    }
    
    @Test
    public void testOpenAndFocus() {
        doOpenAndFocus();
        verifyThat(root.textField, NodeMatchers.isFocused());
    }

    /**
     * 
     */
    protected void doOpenAndFocus() {
        clickOn(root.menu);
        assertTrue(root.menu.isShowing());
//        Runnable r = (() -> root.textField.requestFocus());
//        runAndWaitForFx(r);
        clickOn(root.textField);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        // PENDING move to XTest methods where needed
        //TestFXUtils.stopStoringFiredEvents(stage);
        root = new TextFieldInContextMenuPane(getSkinProvider()); // createContent();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return TextFieldSkin::new;
    }


    public static class TextFieldInContextMenuPane extends VBox {
        protected MenuButton menu;
        protected TextField textField;
        
        protected List<ActionEvent> helloActions;
        protected List<ActionEvent> worldActions;
        public TextFieldInContextMenuPane() {
            this(null);
        }    
            
            
        public TextFieldInContextMenuPane(Function<TextField, TextFieldSkin> skinProvider) {
            helloActions = new ArrayList<>();
            worldActions = new ArrayList<>();
            menu = new MenuButton("Fancy Menu...");

            MenuItem hello = new MenuItem("Hello");
            hello.setOnAction(helloActions::add);
//            hello.setOnAction(event -> System.out
//                    .println("Hello | " + event.getSource()));

            MenuItem world = new MenuItem("World!");
            world.setOnAction(worldActions::add);
//            world.setOnAction(event -> System.out
//                    .println("World! | " + event.getSource()));

            /*
             * Set the cursor into the TextField, maybe type something, and hit
             * enter. --> Expected: "ADD: <Text you typed> ..." --> Actual:
             * "ADD: <Text you typed> ..." AND "World! ..." - so the button
             * above gets triggered as well. If I activate listener (II) or
             * (III), everything works fine - even the empty action in (III)
             * does is job, but this is ugly... (And I can't use (II), because I
             * need (I).
             */
            textField = new TextField() {
                @Override
                protected Skin<?> createDefaultSkin() {
                    return skinProvider == null ? super.createDefaultSkin() : skinProvider.apply(this);
                }

            };
//            /* I */ textField.addEventHandler(ActionEvent.ACTION,
//                    event -> System.out.println("ADD: " + textField.getText()
//                            + " | " + event.getSource()));
            /* II */ // textField.setOnAction(event -> System.out.println("SET:
                     // " + textField.getText() + " | " + event.getSource()));
            /* III */ // textField.setOnAction(__ -> {/* do nothing */});

            CustomMenuItem custom = new CustomMenuItem(textField, false);

            menu.getItems().addAll(hello, world, custom);
            getChildren().addAll(menu);
            
        }
    }
}
