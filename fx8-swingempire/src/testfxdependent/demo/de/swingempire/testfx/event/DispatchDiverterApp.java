/*
 * Created on 18.10.2019
 *
 */
package de.swingempire.testfx.event;

import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.event.BasicEventDispatcher;
import com.sun.javafx.event.CompositeEventDispatcher;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.EnteredExitedHandler;
import com.sun.javafx.scene.NodeEventDispatcher;
import com.sun.javafx.scene.control.FakeFocusTextField;

import static javafx.scene.input.KeyEvent.*;
import static javafx.scene.input.KeyCode.*;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.testfx.textinput.EventHandlerReport;
import javafx.application.Application;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DispatchDiverterApp extends Application {

    EventHandlerReport report = new EventHandlerReport();
    
    public static class RetargetEventDispatcher extends CompositeEventDispatcher {

        private final NodeEventDispatcher targetHandler;
       
        private final NodeEventDispatcher original;
        
        public RetargetEventDispatcher(NodeEventDispatcher original, NodeEventDispatcher last) {
            super();
            this.original = original;
            this.targetHandler = last;
            insertNextDispatcher(original);
            insertNextDispatcher(last);
            
        }

        @Override
        public BasicEventDispatcher getFirstDispatcher() {
            return original.getFirstDispatcher();
        }

        @Override
        public BasicEventDispatcher getLastDispatcher() {
            return targetHandler.getLastDispatcher();
        }
        
    }
    
    public static class DispatchButton extends Button {
        
        private Node target;
        CompositeEventDispatcher composite;

        private EventDispatcher retargeter;
        
        public DispatchButton(String text, Node target) {
            super(text, target);
            this.target = target;
            if (target instanceof FakeFocusTextField) {
                focusedProperty().addListener((src, ov, focused) -> {
                    ((FakeFocusTextField) target).setFakeFocus(focused);
                });
            }
            // just an example 
            addEventFilter(KEY_PRESSED, this::interceptPressed);
            
//            installCustomNodeEventDispatcher();
        }
        
        /**
         * 
         */
        private void installCustomNodeEventDispatcher() {
            original = (NodeEventDispatcher) getEventDispatcher();
            NodeEventDispatcher targetDispatcher = (NodeEventDispatcher) target.getEventDispatcher();
            setEventDispatcher(new RetargetEventDispatcher(original, targetDispatcher));
            
        }

        
        NodeEventDispatcher original;
//        /**
//         * 
//         */
//        private void installCustomEventDispatcher() {
//            original = getEventDispatcher();
//            EventDispatcher diverter = (event, tail) -> {
//                event = original.dispatchEvent(event, tail);
//                if (event instanceof KeyEvent) {
//                    event = event.copyFor(event.getSource(), target);
//                    event = target.getEventDispatcher().dispatchEvent(event, tail);
////                    tail = tail.prepend(target.getEventDispatcher());
//                } //else {
//                if (event != null) {
//                }
//                return event;
//            };
//            setEventDispatcher(diverter);
//            
//        }
        /**
         * Here we handle the keys the dispatcher is interested in.
         * @param ev
         */
        private void interceptPressed(KeyEvent ev) {
            if (ev.getCode() == ENTER) {
                if (getOnAction() != null) {
                    // prevents all dispatch - our own actionHandler is not triggered 
//                    ev.consume();
                }
            }
        }
        
        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            if (target != null && target.getEventDispatcher() != null) {
                tail = tail.append(target.getEventDispatcher());
            }
            tail = super.buildEventDispatchChain(tail);
            return tail;
        }

    }
    private Parent createContent() {
        
        TextField target = new FakeFocusTextField();
        target.setPrefColumnCount(30);
        target.setOnAction(e -> System.out.println("from target textField: " + e ));
        
        Button dispatcher = new DispatchButton("dispatcher", target);
        dispatcher.setOnAction(e -> {
            LOG.info("in dispatcher ..." + e);
        });
        
        Button okButton = new Button("ok");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> { 
            System.out.println("okbutton ... ");
            report.logAll();
            report.clear();
            
        });
        
        VBox content = new VBox(10, dispatcher, okButton);
        List<EventHandler<KeyEvent>> handlers = List.of(
                report.addEventFilter(content, KEY_PRESSED)
                , report.addEventFilter(dispatcher, KEY_PRESSED)
                , report.addEventFilter(target, KEY_PRESSED)
                , report.addEventHandler(target, KEY_PRESSED)
                , report.addEventHandler(dispatcher, KEY_PRESSED)
                , report.addEventHandler(content, KEY_PRESSED)
        );        
        
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
            .getLogger(DispatchDiverterApp.class.getName());

}
