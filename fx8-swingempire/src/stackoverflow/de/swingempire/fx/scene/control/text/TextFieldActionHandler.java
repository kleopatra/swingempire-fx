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
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldActionHandler extends Application {

    private TextField textField;

    private Parent createContent() {
        textField = new TextField("added handler: ");
        textField.skinProperty().addListener((src, ov, nv) -> {
            replaceEnter(textField);
            
        });
        textField.addEventHandler(ActionEvent.ACTION, e -> {
            System.out.println("in added: " + e);
            e.consume();
        });
        
        VBox pane = new VBox(10, textField);
        
        pane.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            System.out.println("in parent: " + e);
        });
        
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.getAccelerators().put(KeyCombination.keyCombination("ENTER"),
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
        InputMap inputMap = behavior.getInputMap();
        KeyBinding binding = new KeyBinding(KeyCode.ENTER);
        
        KeyMapping keyMapping = new KeyMapping(binding, this::fire);
//                e -> {
////            e.consume();
//            fire(e);
//        });
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
//      TextField textField = getNode();
        EventHandler<ActionEvent> onAction = textField.getOnAction();
        ActionEvent actionEvent = new ActionEvent(textField, textField);

        textField.commitValue();
        textField.fireEvent(actionEvent);

        if (onAction == null && !actionEvent.isConsumed()) {
            forwardToParent(event);
        }
        LOG.info("event: " + event);
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
