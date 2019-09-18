/*
 * Created on 18.09.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import static javafx.scene.input.KeyEvent.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Quick check: sibling handlers are notified
 * 
 * but: again difference between register before/after showing
 * before: action not triggered if one of the handlers consumes the event
 * after: action triggered always, independent of consumed or not
 * 
 * behavior different from fx8: action never triggered if keyEvent
 *    is consumed, doesn't matter if registered before/after
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AddEventHandler extends Application {

    private Button button;
    private boolean registerBeforeShowing;
    
    private Parent createContent() {
        // some simple control that's focusable
        button = new Button("just to have a simple control");
        if (registerBeforeShowing) {
            registerHandlers();  
        }
        VBox content = new VBox(10, button, new Button("dummy"), 
                new Label("before showing: " + registerBeforeShowing));
        return content;
    }

    protected void registerHandlers() {
        button.addEventHandler(KEY_PRESSED, e -> {
            System.out.println("receiving in first");
            e.consume();
        });
        button.addEventHandler(KEY_PRESSED, e -> {
            System.out.println("receiving in second");
        });
        button.setOnKeyPressed(e -> {
            System.out.println("singleton");
        });
        button.setOnAction(a -> {
            System.out.println("action");
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
        if (!registerBeforeShowing) {
            registerHandlers();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AddEventHandler.class.getName());

}
