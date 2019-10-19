/*
 * Created on 18.10.2019
 *
 */
package de.swingempire.testfx.event;

import java.util.List;
import java.util.logging.Logger;

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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DispatchDiverterApp extends Application {

    EventHandlerReport report = new EventHandlerReport();
    
    
    public static class DispatchButton extends Button {
        
        private Node target;

        private EventDispatcher retargeter;
        
        public DispatchButton(String text, Node target) {
            super(text);
            this.target = target;
            // just an example (
            addEventFilter(KEY_PRESSED, this::interceptPressed);
        }
        /**
         * Here we handle the keys the dispatcher is interested in.
         * @param ev
         */
        private void interceptPressed(KeyEvent ev) {
            if (ev.getCode() == ENTER) {
                if (getOnAction() != null) {
                    ev.consume();
                }
            }
        }
        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            EventDispatchChain original = super.buildEventDispatchChain(tail);
            if (target != null && target.getEventDispatcher() != null) {
//                if (retargeter == null) {
//                    retargeter = (ev, tail1) -> {
//                        if (ev instanceof KeyEvent) {
//                            System.out.println("before ev: " + ev);
//                            ev = ev.copyFor(ev.getSource(), target);
//                            System.out.println("changed ev: " + ev);
//                            ev = tail1.dispatchEvent(ev);
//                        }
//                        return ev;
//                    };
//                }
                original = original.append(target.getEventDispatcher());
//                original = original.append(retargeter);
            }
            return original;
        }

    }
    private Parent createContent() {
        
        TextField target = new TextField();
        target.setPrefColumnCount(30);
        target.setOnAction(e -> System.out.println("from target"));
        
        Button dispatcher = new DispatchButton("dispatcher", target);
//        Button dispatcher = new Button("no dispatcher");
        
//        dispatcher.setOnAction(e -> System.out.println("dispatcher"));
//        Event.fireEvent(eventTarget, event);
        dispatcher.setOnAction(e -> {
            report.logAll();
            report.getEvents().forEach(ev -> {
                
            });
            report.clear();
        });
        
        Button okButton = new Button("ok");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> System.out.println("okbutton"));
        
        VBox content = new VBox(10, dispatcher, target, okButton);
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
