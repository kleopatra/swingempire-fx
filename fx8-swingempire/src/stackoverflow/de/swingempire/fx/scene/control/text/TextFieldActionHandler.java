/*
 * Created on 17.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private KeyCode actor = KeyCode.ENTER;
    //    private KeyCode actor = KeyCode.F5;
    private Parent createContent() {
        textField = new TextField("just some text");
//        textField.skinProperty().addListener((src, ov, nv) -> {
//            replaceEnter(textField);
//
//        });
        // only this here is in the bug report, with consume
        // https://bugs.openjdk.java.net/browse/JDK-8207774
        textField.addEventHandler(ActionEvent.ACTION, e -> {
            System.out.println("action added: " + e);
                        e.consume();
        });

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

        return pane;
    }

    private void logEvent(String message, KeyEvent event) {
        logEvent(message, event, false);
    }

    private void logEvent(String message, KeyEvent event, boolean consume) {
        if (event.getCode() == actor) {
            System.out.println(message + " source: " + event.getSource().getClass().getSimpleName() 
                    + " target: " + event.getTarget().getClass().getSimpleName());
            if (consume)
                event.consume();    
        }

    }
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.getAccelerators().put(KeyCombination.keyCombination(actor.getName()),
                () -> System.out.println("in accelerator"));
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
