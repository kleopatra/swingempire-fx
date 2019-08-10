/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
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
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ActionApp extends Application {

    /**
     * Trying to hack: use an event that sync's the consumed flag on copyFor.
     * Doesn't help: the copy will be consumed, the original is unchanged.
     * @author Jeanette Winzenburg, Berlin
     */
    public static class XActionEvent extends ActionEvent {

        public XActionEvent() {
            super();
        }

        public XActionEvent(Object source, EventTarget target) {
            super(source, target);
        }

        @Override
        public ActionEvent copyFor(Object newSource, EventTarget newTarget) {
            XActionEvent copy =  (XActionEvent) super.copyFor(newSource, newTarget);
            if (isConsumed()) copy.consume();
            LOG.info("copy: " + copy.isConsumed() + copy);
            return copy;
        }

        @Override
        public String toString() {
            return "@" + Integer.toHexString(hashCode()) + super.toString();
        }
        
    }
    
    // create a simple ui - static because must be same for ActionTest
    public static Parent createContent() {
        TextField field = new TextField();
        // some handler to fire an actionEvent
        field.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.A) {
                ActionEvent action = new XActionEvent(field, field);
                field.fireEvent(action);
                LOG.info("action/consumed? "  + action.isConsumed()+ action);
            }
        });
        // another handler to consume the fired action
        field.addEventHandler(ActionEvent.ACTION, e -> {
            e.consume();
            LOG.info("action received " + e.isConsumed()+ e );
            
        });
        
        Button dummy = new Button("do nothing");
        dummy.addEventFilter(ActionEvent.ACTION, e -> LOG.info("in filter on scene"));
        VBox actionUI = new VBox(field, dummy);
        actionUI.addEventFilter(KeyEvent.KEY_PRESSED, e -> LOG.info("in filter on scene"));
        return actionUI;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
//        scene.getRoot().addEventFilter(ActionEvent.ACTION, e -> LOG.info("in filter on scene"));
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.setX(100);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ActionApp.class.getName());

}
