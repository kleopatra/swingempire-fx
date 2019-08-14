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

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
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
 * <p>
 * This is an answer to https://stackoverflow.com/q/50970411/203657
 * there the context was textField with formatter and cancel button in a dialog.
 * Here it's only a plain pane with cancel button.
 *  
 * <p>
 * Expected behavior:
 * <ul>
 * <li> esc/enter must be consumed if they triggered a "real" cancel/commit 
 *   in the value/text
 * <li> esc/enter must not be consumed if they did not trigger a "real" cancel/commit
 * <ul>
 * 
 * needed: a dirty property in textfield and/or formatter?   
 * textInputControl.commit/cancel are final, can't hook into
 * decided on a utility method to check for dirtyness
 * 
 * @see de.swingempire.fx.scene.control.skin.XTextFieldSkin
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldCancelSO extends Application {

    
    /**
     * Returns a boolean to indicate whether the given field has uncommitted
     * changes.
     * 
     * @param <T> the type of the formatter's value
     * @param field the field to analyse
     * @return true if the field has a textFormatter with converter and
     *    uncommitted changes, false otherwise
     */
    public static <T> boolean isDirty(TextField field) {
        TextFormatter<T> textFormatter = (TextFormatter<T>) field.getTextFormatter();
        if (textFormatter == null || textFormatter.getValueConverter() == null) return false;
        String fieldText = field.getText();
        StringConverter<T> valueConverter = textFormatter.getValueConverter();
        String formatterText = valueConverter.toString(textFormatter.getValue());
        // todo: handle empty string vs. null value
        return !Objects.equals(fieldText, formatterText);
    }
    
    /**
     * Install a custom keyMapping for ESCAPE in the inputMap of the given field. 
     * @param field the textField to configure
     */
    protected void installCancel(TextField field) {
        // Dirty: reflectively access the behavior
        // needs --add-exports at compile- and runtime! 
        // note: FXUtils is a custom helper class not contained in core fx, use your own 
        // helper or write the field access code as needed.
        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, field.getSkin(), "behavior");
        // Dirty: internal api/classes
        InputMap inputMap = behavior.getInputMap();
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
     * Custom EventHandler that's mapped to ESCAPE.
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

    private Parent createContent() {
        TextFormatter<String> fieldFormatter = new TextFormatter<>(
                TextFormatter.IDENTITY_STRING_CONVERTER, "textField ...");
        TextField field = new TextField();
        field.setTextFormatter(fieldFormatter);
        // listen to skin: behavior is available only after it's set
        field.skinProperty().addListener((src, ov, nv) -> {
            installCancel(field);
        });
        // just to see the state of the formatter
        Label fieldValue = new Label();
        fieldValue.textProperty().bind(fieldFormatter.valueProperty());
        
        // add cancel button
        Button cancel = new Button("I'm the cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> LOG.info("triggered: " + cancel.getText()));
        
        HBox fields = new HBox(100, field, fieldValue);
        BorderPane content = new BorderPane(fields);
        content.setBottom(cancel);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldCancelSO.class.getName());

}
