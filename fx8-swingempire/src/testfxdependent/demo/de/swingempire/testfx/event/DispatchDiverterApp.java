/*
 * Created on 18.10.2019
 *
 */
package de.swingempire.testfx.event;

import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.event.BasicEventDispatcher;
import com.sun.javafx.event.CompositeEventDispatcher;
import com.sun.javafx.scene.NodeEventDispatcher;
import com.sun.javafx.scene.control.FakeFocusTextField;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import de.swingempire.testfx.textinput.EventHandlerReport;
import javafx.application.Application;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
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
    
    public static class DispatchButton extends Label {
        
        private Node target;
        CompositeEventDispatcher composite;

        private EventDispatcher retargeter;
        
        public DispatchButton(String text, Node target) {
            super(text, target);
            setFocusTraversable(true);
            this.target = target;
            if (target instanceof FakeFocusTextField) {
                focusedProperty().addListener((src, ov, focused) -> {
                    ((FakeFocusTextField) target).setFakeFocus(focused);
                });
            }
            // just an example 
            addEventFilter(ANY, this::interceptPressed);
            
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
            if (ev.getTarget() == target) return;
            target.fireEvent(ev.copyFor(target, target));
            ev.consume();
            if (ev.getEventType() == KEY_PRESSED) {
            }
//            if (ev.getCode() == ENTER) {
//                if (getOnAction() != null) {
//                    // prevents all dispatch - our own actionHandler is not triggered 
////                    ev.consume();
//                }
//            }
        }
        
//        @Override
//        public EventDispatchChain buildEventDispatchChain(
//                EventDispatchChain tail) {
//            if (target != null && target.getEventDispatcher() != null) {
//                tail = tail.append(target.getEventDispatcher());
//            }
//            tail = super.buildEventDispatchChain(tail);
//            return tail;
//        }

    }
    private Parent createContent() {
        
        target = new FakeFocusTextField();
        target.setPrefColumnCount(30);
//        target.setOnAction(e -> System.out.println("from target textField: " + e ));
        
        dispatcher = new DispatchButton("dispatcher", target);
        
        okButton = new Button("ok");
        okButton.setOnAction(e -> { 
            System.out.println("okbutton ... ");
            report.logAll();
            report.clear();
            
        });
        
        VBox content = new VBox(10, dispatcher, okButton);
        
        return content;
    }

    
    @Override
    public void start(Stage stage) throws Exception {
        Parent content = createContent();
        scene = new Scene(content);
        okButton.setDefaultButton(true);
        scene.getAccelerators().put(KeyCombination.keyCombination("A"), () -> System.out.println("accelerator"));
        List<EventHandler<KeyEvent>> handlers = List.of(
                report.addEventFilter(scene, KEY_PRESSED)
                , report.addEventFilter(content, KEY_PRESSED)
                , report.addEventFilter(dispatcher, KEY_PRESSED)
                , report.addEventFilter(target, KEY_PRESSED)
                , report.addEventHandler(target, KEY_PRESSED)
                , report.setOnKeyPressed(target)
                , report.addEventHandler(dispatcher, KEY_PRESSED)
                , report.addEventHandler(content, KEY_PRESSED)
                , report.addEventHandler(scene, KEY_PRESSED)
        );        
//        target.addEventHandler(KEY_PRESSED, e-> {
//            if (e.getCode() == ENTER) {
//                System.out.println("from consuming handler");
//                e.consume();
//            }
//        });
//        target.setOnKeyPressed(e-> {
//            if (e.getCode() == ENTER) {
//                System.out.println("from consuming handler");
//                e.consume();
//            }
//        });
        stage.setScene(scene);
        stage.show();
        stage.setX(100);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DispatchDiverterApp.class.getName());
    private TextField target;
    private Node dispatcher;
    private Button okButton;
    private Scene scene;

}
