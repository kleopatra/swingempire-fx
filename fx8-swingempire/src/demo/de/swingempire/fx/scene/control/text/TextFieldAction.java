/*
 * Created on 03.03.2016
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static javafx.scene.control.TextFormatter.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Issue: text not yet committed when receiving the actionEvent.
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8152557
 * 
 * fixed in fx9.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldAction extends Application {

    private Parent getContent() {
        // ComboBox behaves as expected
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
//        ComboBoxX<String> comboBox = new ComboBoxX<>(items);
        ComboBox<String> comboBox = new ComboBox<String>(items);
        comboBox.setEditable(true);
        comboBox.setValue(items.get(0));
        comboBox.setOnAction(e -> {
            System.out.println("combo action: " + 
                    comboBox.getEditor().getText() + "=" + comboBox.getValue());
        });

        // compare to TextField with TextFormatter: value not yet committed
        TextField textField = new TextField();
//        textField.setSkin(new TextFieldSkin(textField));
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
        // fixed!
//        textField.skinProperty().addListener((src, ov, nv) -> {
//            replaceEnter(textField);
//            
//        });
        textField.setTextFormatter(formatter);
        textField.setOnAction(e -> {
            System.out.println("textfield action: " + 
                    textField.getText() + "=" + formatter.getValue());
        });
        // compare Spinner
        Spinner spinner = new Spinner();
        // normal setup of spinner
        SpinnerValueFactory factory = new IntegerSpinnerValueFactory(0, 10000, 0);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        // spinner has no action (should it?
        // spinner.setOnAction(e -> {
        // using action of editor
        spinner.getEditor().setOnAction(e -> {
            System.out.println("spinner-editor action: " + 
                    spinner.getEditor().getText() + "=" + spinner.getValue());
            
        });
        
        VBox box = new VBox(10, textField, comboBox, spinner);
        return box;
    }

    /** 
     * Hack-around: text not committed on receiving action
     * https://bugs.openjdk.java.net/browse/JDK-8152557
     * fixed as of somewhere in 9
     * 
     * A - reflectively replace the field's keyBinding to ENTER, must
     * be called after the skin is installed.
     * 
     * B - eventhandler that commits before firing the action
     * <p>
     * Note: we can't extend/change the skin's behavior, it's
     * final!
     * 
     * @param field
     */
    protected void replaceEnter(TextField field) {
        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, field.getSkin(), "behavior");
        InputMap inputMap = behavior.getInputMap();
        KeyBinding binding = new KeyBinding(KeyCode.ENTER);
        
        KeyMapping keyMapping = new KeyMapping(binding, e -> {
            e.consume();
            fire(field);
        });
        // note: this fails prior to 9-ea-108
        // due to https://bugs.openjdk.java.net/browse/JDK-8150636
        inputMap.getMappings().remove(keyMapping); 
        inputMap.getMappings().add(keyMapping);
    }
    
    protected void fire(KeyEvent event) {
        
    }
    protected void fire(TextField textField) {
        EventHandler<ActionEvent> onAction = textField.getOnAction();
        ActionEvent actionEvent = new ActionEvent(textField, null);
        // first commit, then fire
        textField.commitValue();
        textField.fireEvent(actionEvent);
        // PENDING JW: missing forwardToParent
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldAction.class.getName());
}
