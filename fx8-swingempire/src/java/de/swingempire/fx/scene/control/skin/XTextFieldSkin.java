/*
 * Created on 08.08.2019
 *
 */
package de.swingempire.fx.scene.control.skin;

import java.util.Objects;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.behavior.TextInputControlBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import de.swingempire.fx.util.FXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

/**
 * Distill inputmap tweaks into custom skin.
 * 
 * @see de.swingempire.fx.scene.control.text.TextFieldValueAndDefaultCancel
 * @see de.swingempire.fx.scene.control.text.TextFieldCancelSO
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldSkin extends TextFieldSkin {

    private TextFieldBehavior behavior;
    
    /**
     * @param control
     */
    public XTextFieldSkin(TextField control) {
        super(control);
        behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, this, "behavior");
        
        installCancel();
        installEnter();
    }

    // note: this differs from experiment in TextFieldValueAndDefaultCancel
    protected void fire(KeyEvent e) {
        TextField textField = getSkinnable();
        EventHandler<ActionEvent> onAction = textField.getOnAction();
        ActionEvent actionEvent = new ActionEvent(textField, textField);
        // first commit, then fire
        boolean dirty = isDirty(textField);

        textField.commitValue();
        textField.fireEvent(actionEvent);
        // nothing more to do, consume
        if (dirty || onAction != null || actionEvent.isConsumed()) {
            e.consume();
        }
        // original
//        if (onAction == null && !actionEvent.isConsumed()) {
//            forwardToParent(e);
//        }
    }

    protected void forwardToParent(KeyEvent event) {
        // fix for JDK-8145515
        if (getNode().getProperties().containsKey(TextInputControlBehavior.DISABLE_FORWARD_TO_PARENT)) {
            return;
        }

        if (getNode().getParent() != null) {
            getNode().getParent().fireEvent(event);
        }
    }


    /**
     * Custom EventHandler that's mapped to ESCAPE.
     * 
     * @param field the field to handle a cancel for
     * @param ev the received keyEvent 
     */
    protected void cancelEdit(KeyEvent ev) {
        TextField field = getSkinnable();
        boolean dirty = isDirty(field);
        field.cancelEdit();
        if (dirty) {
           ev.consume();
        }
    }

    protected void installEnter() {
        InputMap inputMap = behavior.getInputMap();
        KeyBinding binding = new KeyBinding(KeyCode.ENTER);

        KeyMapping keyMapping = new KeyMapping(binding, e -> {
//            e.consume();
            fire(e);
        });
        // by default, mappings consume the event - configure not to
        keyMapping.setAutoConsume(false);
        // note: this fails prior to 9-ea-108
        // due to https://bugs.openjdk.java.net/browse/JDK-8150636
        inputMap.getMappings().remove(keyMapping); 
        inputMap.getMappings().add(keyMapping);
    }

    /**
     * Install a custom keyMapping for ESCAPE in the inputMap of the given field. 
     * @param field the textField to configure
     */
    protected void installCancel() {
//        // Dirty: reflectively access the behavior
//        // needs --add-exports at compile- and runtime! 
//        // note: FXUtils is a custom helper class not contained in core fx, use your own 
//        // helper or write the field access code as needed.
//        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
//                TextFieldSkin.class, this, "behavior");
        // Dirty: internal api/classes
        InputMap inputMap = behavior.getInputMap();
        KeyBinding binding = new KeyBinding(KeyCode.ESCAPE);
        // custom mapping that delegates to helper method
        KeyMapping keyMapping = new KeyMapping(binding, e ->  {
            cancelEdit(e);
        });
        // by default, mappings consume the event - configure not to
        keyMapping.setAutoConsume(false);
        // remove old
        inputMap.getMappings().remove(keyMapping);
        // add new
        inputMap.getMappings().add(keyMapping);
    }
    
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
    
    public static class XActionEvent extends ActionEvent {

        public XActionEvent() {
            super();
        }

        public XActionEvent(Object source, EventTarget target) {
            super(source, target);
        }

        @Override
        public ActionEvent copyFor(Object newSource, EventTarget newTarget) {
            XActionEvent copy =  (XActionEvent) super.copyFor(newSource, newTarget);
            if (isConsumed()) copy.consume();
            return copy;
        }
        
        
    }
    


}
