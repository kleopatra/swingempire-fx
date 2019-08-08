/*
 * Created on 07.08.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.Objects;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * TextField with TextFormatter has unexpected behavior on cancel/commit
 * (shows when having cancel/default buttons)
 * 
 * Expected behavior:
 * - esc/enter must be consumed if they triggered a "real" cancel/commit 
 *   in the value/text
 * - esc/enter must not be consumed if they did not trigger a "real" cancel/commit
 * 
 * needed: a dirty property in textfield and/or formatter?   
 * textInputControl.commit/cancel are final, can't hook into
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldValueAndDefaultCancel extends Application {

    public static <T> boolean isDirty(TextField field) {
        TextFormatter<T> textFormatter = (TextFormatter<T>) field.getTextFormatter();
        if (textFormatter == null || textFormatter.getValueConverter() == null) return false;
        String fieldText = field.getText();
        StringConverter<T> valueConverter = textFormatter.getValueConverter();
        String formatterText = valueConverter.toString(textFormatter.getValue());
        // todo: handle empty string vs. null value
        return !Objects.equals(fieldText, formatterText);
    }
    
    
    protected void replaceCancel(TextField field) {
        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, field.getSkin(), "behavior");
        InputMap inputMap = behavior.getInputMap();
        FXUtils.prettyPrintMappings(inputMap);
        KeyBinding binding = new KeyBinding(KeyCode.ESCAPE);
        // custom mapping that delegates to helper method
        KeyMapping keyMapping = new KeyMapping(binding, e ->  {
            cancelEdit(field, e);
        });
        // by default, mappings consume the event - configure not to
        keyMapping.setAutoConsume(false);
        // remove old
        inputMap.getMappings().remove(keyMapping);
        // add new
        inputMap.getMappings().add(keyMapping);
    }
    
    /**
     * 
     * @param field the field to handle a cancel for
     * @param ev the received keyEvent 
     */
    protected void cancelEdit(TextField field, KeyEvent ev) {
        boolean dirty = isDirty(field);
        field.cancelEdit();
        if (dirty) {
           ev.consume();
        }
    }
    
//    protected void replaceEnter(TextField field) {
//        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
//                TextFieldSkin.class, field.getSkin(), "behavior");
//        InputMap inputMap = behavior.getInputMap();
//        KeyBinding binding = new KeyBinding(KeyCode.ENTER);
//        
//        KeyMapping keyMapping = new KeyMapping(binding, e -> {
//            e.consume();
//            fire(field);
//        });
//        // note: this fails prior to 9-ea-108
//        // due to https://bugs.openjdk.java.net/browse/JDK-8150636
//        inputMap.getMappings().remove(keyMapping); 
//        inputMap.getMappings().add(keyMapping);
//    }
//    
//    protected void fire(TextField textField) {
//        EventHandler<ActionEvent> onAction = textField.getOnAction();
//        ActionEvent actionEvent = new ActionEvent(textField, null);
//        // first commit, then fire
//        textField.commitValue();
//        textField.fireEvent(actionEvent);
//        // PENDING JW: missing forwardToParent
//    }

    ChangeListener skinListener;
    
    private Parent createTextContent() {
        TextFormatter<String> fieldFormatter = new TextFormatter<>(
                TextFormatter.IDENTITY_STRING_CONVERTER, "textField ...");
        TextField field = new TextField();
        field.setTextFormatter(fieldFormatter);
        skinListener = (src, ov, nv) -> {
            replaceCancel(field);
            src.removeListener(skinListener);
        };
        field.skinProperty().addListener(skinListener);
        Label fieldValue = new Label();
        fieldValue.textProperty().bind(fieldFormatter.valueProperty());
        
        HBox fields = new HBox(100, field, fieldValue);
        BorderPane content = new BorderPane(fields);
        
        return content;
    }

    private Parent createContent() {
        Button button = new Button("I'm the default");
        button.setDefaultButton(true);
        button.setOnAction(e -> LOG.info("triggered: " + button.getText()));
        Button cancel = new Button("I'm the cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> LOG.info("triggered: " + button.getText()));
        BorderPane content = new BorderPane( createTextContent());
        content.setBottom(new HBox(10, button, cancel));
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
            .getLogger(TextFieldValueAndDefaultCancel.class.getName());

}
