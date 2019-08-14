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
 * <p>
 * Expected behavior:
 * <ul>
 * <li> esc/enter must be consumed if they triggered a "real" cancel/commit 
 *   in the value/text
 * <li> esc/enter must not be consumed if they did not trigger a "real" cancel/commit
 * </ul>
 * 
 * Definition of a "real" cancel/commit: there are uncommitted changes in the 
 * input control. For a TextField it's not entirely straightforward, because
 * a bare TextField has no notion of commit - it's only introduced with a 
 * TextFormatter. In the latter case, we can compare the TextField's text (converted
 * with the formatter's converter) with the value of its formatter: if they are not equal,
 * the there are uncommitted changes and the enter/escape should be used to commit/cancel
 * the change and be consumed. Otherwise do nothing and let the event pass without consuming.
 * <p>
 * TextFieldBehavior implements the enter/escape (fx11)
 * <p> enter: the keyMapping autoConsumes -  this sequence was introduced to fix 
 *    https://bugs.openjdk.java.net/browse/JDK-8152557 where the formatters value was
 *    not yet updated on receiving the actionEvent
 * <ul> 
 * <li> commit
 * <li> fire actionEvent 
 * <li> if there's neither an onAction handler nor the fired action consumed, forward the enter
 *   to parent
 * </ul>  
 * <p> escape: the keyMapping does not autoConsume -  was introduced when fixing 
 * https://bugs.openjdk.java.net/browse/JDK-8090230, where a popup  hideOnEscape wasn't closed when it
 * contained a focused textField (which still is virulent if the textField has a formatter)
 * <ul>
 * <li> if there is a formatter, cancel and consume 
 * <li>  otherwise call super (which effectively forwards to parent)
 * </ul>
 * 
 * Bug: enter/consume are always consumed if the field has a formatter - the implementation
 * isn't good enough because it doesn't compare the formatter's value against the textfield's
 * property. Doing so resolves issues with default/cancel buttons and hiding popup/closing
 * dialogs containing focused textfields
 * 
 * <p>
 * 
 * TODO test against https://stackoverflow.com/q/51388408/203657 and the bugs
 * referenced there. 
 * 
 * Note: my answer on that demonstrates that forwardToParent is evil - stricter analysis is needed! 
 * 
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
