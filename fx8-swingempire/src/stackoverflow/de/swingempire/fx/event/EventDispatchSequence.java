/*
 * Created on 15.04.2019
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55545537/203657
 * Change of event delivery between fx8 and fx11
 * <p>
 * Tutorial states that handlers registered with convenience methods 
 * (== setOnXX) are called last, see
 * https://docs.oracle.com/javafx/2/events/processing.htm#CEGJAAFD
 * <p>
 * The order in which two handlers at the same level are executed is not specified, 
 * with the exception that handlers that are registered by the convenience methods 
 * described in Working with Convenience Methods are executed last. 
 * <p>
 * 
 * Might imply so much, f.i. from node.setOnKeyPressed doc:
 * <p>
 * Defines a function to be called when this Node or its child Node has input focus 
 * and a key has been pressed. The function is called only if the event hasn't been 
 * already consumed during its capturing or bubbling phase.
 * <p>
 * Slaw comments that the impl is not following the spec: setOnKeyPressed handler sees event 
 * even if consumed by a handler.
 * 
 * <p>
 * All handlers registered on a 
 * <p>
 * looks like a bug, but too complex .. giving up.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class EventDispatchSequence extends Application {

    private Parent createContent() {
        Button dummy =  new Button("dummy - just need something focused");
        
        
        
        registerKeyPressed(dummy);
        
        Rectangle r = new Rectangle(100, 100, Color.RED);
        r.setFocusTraversable(true);
        r.setOnMousePressed(e -> {
            r.requestFocus();
        });
        
        registerKeyPressed(r);
        BorderPane content = new BorderPane(dummy);
        content.setTop(r);
        return content;
    }

    /**
     * @param dummy
     */
    protected void registerKeyPressed(Node dummy) {
        dummy.setOnKeyPressed(e -> LOG.info("convenienceHandler: " + e));
        
        dummy.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("1. consuming handler: " + e);
            e.consume();
        });
        
        dummy.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("2. consuming handler: " + e);
            e.consume();
        });
        
        dummy.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("1. filter: " + e);
            e.consume();
        });
        dummy.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            LOG.info("2. filter: " + e);
//            e.consume();
        });
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
            .getLogger(EventDispatchSequence.class.getName());

}
