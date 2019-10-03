/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
 * unrelated to TestFx: happens whenever an eventFilter of (same or super type of the fired event)
 * is registered anywhere in
 * the parent hierarchy (which TestFx does in FiredEvents of FxTooltipContext, for EventType.ROOT)
 * 
 * might be related to rfe https://bugs.openjdk.java.net/browse/JDK-8091151 (need different dispatch
 * for actionEvent) ... or not even a bug?
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8092352: fixme - do not dispatch if there are not filters
 * 
 * reported https://bugs.openjdk.java.net/browse/JDK-8229467 (against fx11)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ActionDispatchBug extends Application {
    
    public Parent createContent() {
        TextField field = new TextField();
        // some handler to fire an actionEvent
        field.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.A) {
                ActionEvent action = new ActionEvent(field, field);
                field.fireEvent(action);
                LOG.info("action fired "  + action.isConsumed() + " @" + action.hashCode());
            }
        });
        // another handler to consume the fired action
        field.addEventHandler(ActionEvent.ACTION, action -> {
            action.consume();
            LOG.info("action consumed: " + " @" + action.hashCode() );
        });
        
        VBox actionUI = new VBox(field);
        return actionUI;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        stage.setScene(scene);
        
        // add/remove an eventFilter 
//        EventHandler filter = e -> {};
//        stage.addEventFilter(EventType.ROOT, filter);
//        stage.removeEventFilter(EventType.ROOT, filter);
        
        //stage.setTitle(FXUtils.version());
        stage.setX(100);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ActionDispatchBug.class.getName());

}
