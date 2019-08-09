/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Different event dispatch in Application vs. ApplicationTest.
 * 
 * The setup simulates dispatching actionEvent in TextFieldBehavior.fire:
 * 
 * - a handler receives a keyEvent that makes the field fire an ActionEvent
 * - an actionHandler receives the fired action and consumes it
 * - in the first handler, check whether the ActionEvent is consumed (here
 *   simple logging, in real context the control flow should be changed)
 * 
 * in Application: the action is consumed
 * in ApplicationTest: the action is not consumed
 * 
 * environment: fx11, testfx Oct 2018 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ActionApp extends Application {

    // create a simple ui - static because must be same for ActionTest
    public static Parent createContent() {
        TextField field = new TextField();
        // some handler to fire an actionEvent
        field.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.A) {
                ActionEvent action = new ActionEvent(field, field);
                field.fireEvent(action);
                LOG.info("action/consumed? " + action + action.isConsumed());
            }
        });
        // another handler to consume the fired action
        field.addEventHandler(ActionEvent.ACTION, e -> {
            e.consume();
            LOG.info("action received " + e + e.isConsumed());
        });
        
        VBox actionUI = new VBox(field);
        return actionUI;
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
            .getLogger(ActionApp.class.getName());

}
