/*
 * Created on 17.10.2019
 *
 */
package de.swingempire.testfx.event;

import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.event.EventRedirector;
import com.sun.javafx.scene.control.FakeFocusTextField;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import de.swingempire.testfx.textinput.EventHandlerReport;

import static javafx.event.ActionEvent.*;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
/**
 * Trying an example of how/if/when building a nested dispatch chain and 
 * delivering the received event while it is delivered is evil.
 * 
 * sent to openjfx mailing list
 * 
 * Note: here we re-fire down into a child. That's okay (except for double-notification
 * in the upper part of the chain)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class NestedEventDispatchChainDown extends Application {
    private boolean useDispatch = false;
    private boolean tweak = false;
    
    private EventHandlerReport report = new EventHandlerReport();

    private KeyCode key = DIGIT1;

    private boolean useDispatch() {
        return tweak && useDispatch;
    }
    
    private boolean useRefire() {
        return  tweak && !useDispatch;
    }
    
    public static class KeyEventChildRedirector extends EventRedirector {

        /**
         * @param eventSource
         */
        public KeyEventChildRedirector(Node eventSource) {
            super(eventSource);
        }
        
    }
    private Parent createContent() {
        Label control = new Label("the evil button!") {

            @Override
            public EventDispatchChain buildEventDispatchChain(
                    EventDispatchChain tail) {
                if (getGraphic() != null && useDispatch()) {
                    // this has same effect as refire: events are delivered twice to filters
//                    tail = getGraphic().buildEventDispatchChain(tail);
                    // this looks correct in that filters are notified once
                    // remaining problems: target not updated, all events are dispatched
                    // for action: filter on scene/this parent are notified twice, the second time
                    // after all others 
                    // this behavior is same if refired
                    tail = tail.append(getGraphic().getEventDispatcher());
                }
                return super.buildEventDispatchChain(tail);
            }
            
        };
//        TextField target = new FakeFocusTextField();
        TextField target = new TextField();
//        target.setOnAction(e -> {
//            e.consume();
//            System.out.println("getting action in target" + e);
//        });
        control.setGraphic(target);
        control.setFocusTraversable(true);
        control.focusedProperty().addListener((src, ov, nv) -> {
            if (control.getGraphic() instanceof FakeFocusTextField)
            ((FakeFocusTextField) control.getGraphic()).setFakeFocus(nv);
        });
        if (useRefire()) {
            
            // re-firing handler
            // note: need to refire all pressed/typed/released to really trigger
            // input in target!
            control.addEventFilter(KeyEvent.ANY, e -> {
                if (e.getTarget() == control.getGraphic()) return;
                System.out.println("before refire " + e);
                control.getGraphic().fireEvent(
                        e.copyFor(control.getGraphic(), control.getGraphic()));
                System.out.println("after refire " + e);
                e.consume();
                if (key == e.getCode() || e.getCode() == UNDEFINED) {
                }
            });
        }

        // consuming singleton handler
        control.setOnKeyPressed(e -> {
            if (key == e.getCode()) {
                e.consume();
                System.out.println("consumed in singleton " + e.getCode());
            }
        });

        BorderPane content = new BorderPane(control);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane content = (BorderPane) createContent();
        Scene scene = new Scene(content);
        
        // accelerator that shouldn't be triggered because singleton handler consumed
        scene.getAccelerators().put(KeyCombination.keyCombination(key.getName()), () -> {
            System.out.println("accelerator triggered for " + key);
        });
        Button ok = new Button("default ");
        ok.setDefaultButton(true);
        ok.setOnAction(e -> { 
            System.out.println("okbutton ... ");
            report.logAll();
            report.clear();
            
        });
        
        content.setBottom(new HBox(10, ok));
        Node target = ((Labeled) content.getCenter()).getGraphic();
        @SuppressWarnings("unchecked")
        List<EventHandler<KeyEvent>> keyHandlers = List.of(
//                report.addEventFilter(scene, KEY_PRESSED)
//                , report.addEventFilter(content, KEY_PRESSED)
//                , report.addEventFilter(content.getCenter(), KEY_PRESSED)
//                , report.addEventFilter(target, KEY_PRESSED)
//                , report.addEventHandler(target, KEY_PRESSED, e -> {
//                    // consuming here (or in setOn) is fine if xx7759 is fixed
//                    // accelerator/default button not triggered
////                        if (e.getCode() == ENTER) {
////                            System.out.println("from consuming handler");
////                            e.consume();
////                        }
//                    
//                    })
//                , report.setOnKeyPressed(target, e -> {
//                    })
//                , report.addEventHandler(content.getCenter(), KEY_PRESSED)
//                , report.addEventHandler(content, KEY_PRESSED)
//                , report.addEventHandler(scene, KEY_PRESSED)
        );        

        List<EventHandler<ActionEvent>> actionHandlers = List.of(
                report.addEventFilter(scene, ACTION)
                , report.addEventFilter(content, ACTION)
                , report.addEventFilter(content.getCenter(), ACTION)
                , report.addEventFilter(target, ACTION)
                , report.addEventHandler(target, ACTION, e -> {
                    // consuming here (or in setOn) is fine if xx7759 is fixed
                    // accelerator/default button not triggered
//                        if (e.getCode() == ENTER) {
//                            System.out.println("from consuming handler");
//                            e.consume();
//                        }
                    
                    })
//                , report.setOnKeyPressed(target, e -> {
//                    })
                , report.addEventHandler(content.getCenter(), ACTION)
                , report.addEventHandler(content, ACTION)
                , report.addEventHandler(scene, ACTION)
               );
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(NestedEventDispatchChainDown.class.getName());

}
