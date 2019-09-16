/*
 * Created on 17.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8207774
 * TextField: behaviour must not forward ENTER if consumed by actionHandler
 * 
 * 
 * Changed to log the notification of handlers/filters in the chain:
 * <ul>
 * <li> for actors like F5: 
 *   filter.parent -> filter-field -> handler-field -> onKeyPressed-field -> handler-parent -> acc
 * <li> for ENTER:
 *   filter.parent -> filter-field -> handler-field -> action -> filter-parent -> handler-parent
 *   -> acc -> onKeyPressed-field  
 * </ul>  
 * normal keys are dispatched as expected, sequence for enter is broken ..
 * <p>
 * digging into
 * https://stackoverflow.com/q/51388408/203657 <br>
 * accelerator triggered even if ENTER consumed in keyPressedHandler <br>
 * reported as bug: https://bugs.openjdk.java.net/browse/JDK-8207759
 * reason seems to be the manual event dispatch .. 
 * this example is used for an answer on SO
 *  
 * 
 * @author Jeanette Winzenburg, Berlin
 * @see de.swingempire.fx.scene.control.text.TextInputWithDefaultButton
 */
public class TextFieldActionHandler extends Application {

    private TextField textField;

    private KeyCode enter = KeyCode.ENTER;
    private KeyCode normal = KeyCode.F5;
    
    private KeyCode actor = normal;
    
    private KeyCode getActor() {
        return actor;
    }
    
    private void toggleActor() {
        if (actor == normal) {
            actor = enter;
        } else {
            actor = enter;
        }
        updateAccelerator(textField.getScene());
    }
    
    private void updateAccelerator(Scene scene) {
       if (scene == null) return;
       scene.getAccelerators().put(KeyCombination.keyCombination(getActor().getName()),
               () -> logEvent("in accelerator", null));
//               () -> System.out.println("in accelerator"));
    }

    private Parent createContent() {
        textField = new TextField("just some text");
//        textField.skinProperty().addListener((src, ov, nv) -> {
//            replaceEnter(textField);
//
//        });
        // only this here is in the bug report, with consume
        // https://bugs.openjdk.java.net/browse/JDK-8207774
//        textField.addEventHandler(ActionEvent.ACTION, e -> {
//            System.out.println("action added: " + e);
//                        e.consume();
//        });

        //everything else is digging around
        textField.setOnKeyPressed(event -> {
            logEvent("-> onKeyPressed on field ",  event);
        });

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            logEvent("-> filter on field ", event);
        });

        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            logEvent("-> handler on field ", event);
        });

        VBox pane = new VBox(10, textField);

        pane.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            logEvent("-> handler on parent: ", e);
        });

        pane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            logEvent("-> filter on parent: ", e);
        });

        //everything else is digging around
        pane.setOnKeyPressed(event -> {
            logEvent("-> onKeyPressed on parent ",  event);
        });

        Button log = new Button("log");
        log.setOnAction(e -> printTraces());
        Button toggleActor = new Button("toggle");
        toggleActor.setOnAction(e -> toggleActor());
        pane.getChildren().addAll(log, toggleActor);
        return pane;
    }

    private void logEvent(String message, KeyEvent event) {
        printWithStackTrace(message, event);
        //logEvent(message, event, false);
    }

    private void logEvent(String message, KeyEvent event, boolean consume) {
        if (event.getCode() == getActor()) {
            System.out.println(event.getCode()  + message  
                    + " source: " + event.getSource().getClass().getSimpleName() 
                    + " target: " + event.getTarget().getClass().getSimpleName());
            
            if (consume)
                event.consume();    
        }

    }
    
    private void printTraces() {
        traces.forEach(s -> System.out.println(s));
        traces.clear();
    }
    List<String> traces = new ArrayList<>();
    
    private void printWithStackTrace(String message, KeyEvent event) {
        if (event != null && event.getCode() != getActor())return;
        String eventText = "no event ";
        if (event != null) {
            eventText = "source: " + event.getSource() + " target: " + event.getTarget();
        }
        traces.add(getActor() + " on " + message + "\n    " + eventText);
        Arrays.stream(new RuntimeException().getStackTrace()).forEach(s -> traces.add(s.toString()));
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        updateAccelerator(scene);
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /** 
     * fishy code snippet from TextFieldBehaviour: 
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8207774
     * during fire, the actionEvent without target is copied - such that
     * the check for being consumed of the original has no effect
     */
    //    @Override protected void fire(KeyEvent event) {
    //        TextField textField = getNode();
    //        EventHandler<ActionEvent> onAction = textField.getOnAction();
    //        ActionEvent actionEvent = new ActionEvent(textField, null);
    //
    //        textField.commitValue();
    //        textField.fireEvent(actionEvent);
    //
    //        if (onAction == null && !actionEvent.isConsumed()) {
    //            forwardToParent(event);
    //        }
    //    }


    // dirty patching
    protected void replaceEnter(TextField field) {
        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, field.getSkin(), "behavior");
        InputMap<TextField> inputMap = behavior.getInputMap();
        KeyBinding binding = new KeyBinding(KeyCode.ENTER);

        KeyMapping keyMapping = new KeyMapping(binding, this::fire);
        keyMapping.setAutoConsume(false);
        // note: this fails prior to 9-ea-108
        // due to https://bugs.openjdk.java.net/browse/JDK-8150636
        inputMap.getMappings().remove(keyMapping); 
        inputMap.getMappings().add(keyMapping);
    }

    /**
     * Copy from TextFieldBehaviour, changed to set the field as
     * both source and target of the created ActionEvent.
     * 
     * @param event
     */
    protected void fire(KeyEvent event) {
        EventHandler<ActionEvent> onAction = textField.getOnAction();
        ActionEvent actionEvent = new ActionEvent(textField, textField);

        textField.commitValue();
        textField.fireEvent(actionEvent);
        // remove the manual forwarding, instead consume the keyEvent if
        // the action handler has consumed the actionEvent
        // this way, the normal event dispatch can jump in with the normal
        // sequence
        if (onAction != null || actionEvent.isConsumed()) {
            event.consume();
        }
        // original code
        //        if (onAction == null && !actionEvent.isConsumed()) {
        ////            forwardToParent(event);
        //        }
        logEvent("in fire: " + event.isConsumed(), event);
    }

    protected void forwardToParent(KeyEvent event) {
        if (textField.getParent() !=  null) {
            textField.getParent().fireEvent(event);
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
    .getLogger(TextFieldActionHandler.class.getName());

}
