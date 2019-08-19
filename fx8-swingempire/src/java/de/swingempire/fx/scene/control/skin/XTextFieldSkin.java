/*
 * Created on 08.08.2019
 *
 */
package de.swingempire.fx.scene.control.skin;

import java.util.Objects;
import java.util.logging.Logger;

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
 * 
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
 * the question is reported by OP as https://bugs.openjdk.java.net/browse/JDK-8207759,
 * example from question (and bug report) is copied here as TextInputWithDefaultButton
 * 
 * TODO: my answer on that demonstrates that forwardToParent is evil - stricter analysis is needed! 
 * 
 * 
 * @see de.swingempire.fx.scene.control.text.TextFieldValueAndDefaultCancel
 * @see de.swingempire.fx.scene.control.text.TextFieldCancelSO
 * @see de.swingempire.fx.scene.control.text.TextFieldActionHandler
 * @see de.swingempire.fx.scene.control.text.TextFieldAction
 * @see de.swingempire.fx.scene.control.text.TextInputWithDefaultButton
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldSkin extends TextFieldSkin {

    private TextFieldBehavior behavior;
    
    /**
     * Instantiates a XTextFieldSkin for the control.
     * @param control the textfield to skin.
     */
    public XTextFieldSkin(TextField control) {
        super(control);
        behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
                TextFieldSkin.class, this, "behavior");
        
        installCancel();
        installEnter();
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

//------------------ hack around interfering eventFilter   
    /**
     * Custom key to store the action fired by enter mapping. The value is
     * valid only during the dispatch.
     */
    public static String TEXT_FIELD_FIRED_ACTION = "textfield.firedAction";
    /**
     * Core key to block behavior's forwardToParent method. Introduced
     * to fix https://bugs.openjdk.java.net/browse/JDK-8145515
     * textField in editable combo: custom enter filter not invoked.
     * <p>
     * This also helps in https://bugs.openjdk.java.net/browse/JDK-8207385 
     * Wrong menuItem action triggered
     */
    public static String TEXT_FIELD_DISABLED_FORWARD_TO_PARENT = "TextInputControlBehavior.disableForwardToParent";
    /**
     * Hack around https://bugs.openjdk.java.net/browse/JDK-8229467
     * Having an eventFilter in the dispatch chain consumes prevents
     * consuming the fired event.
     * 
     * The idea here: the fire method stores the fired action in the
     * the field's properties, this utility method consumes both the
     * received and the stored action, if available. Then fire can
     * check if any is consumed.
     * 
     * @param received the action received by a handler
     * @param textField the textField on which a handler might be added/set
     * 
     * @throws NullPointerException if either the action or textField is null
     */
    public static void consumeAction(ActionEvent received, TextField textField) {
        Objects.requireNonNull(received, "action must not be null");
        Objects.requireNonNull(textField, "textField must not be null");
        received.consume();
        ActionEvent firedAction = (ActionEvent) textField.getProperties().get(TEXT_FIELD_FIRED_ACTION);
        if (firedAction != null) {
            firedAction.consume();
        }
    }
    
    /**
     * Hack around interfering eventFilter.
     * This implementation stores the fired action in the fields properties map
     * before firing.
     * 
     * @param action the action to fire on the textField
     */
    protected void fireAction(ActionEvent action) {
        TextField textField = getSkinnable();
        textField.getProperties().put(TEXT_FIELD_FIRED_ACTION, action);
        textField.fireEvent(action);
    }
    
    /**
     * Hack around interfering eventFilter.
     * 
     * Checks and returns the consumed state of the given action, removed the stored
     * property
     * 
     * @param action the action that had been fired on the textfield
     * @return
     */
    protected boolean isConsumed(ActionEvent action) {
        getSkinnable().getProperties().remove(TEXT_FIELD_FIRED_ACTION);
        return action.isConsumed();
    }

//-------------- end hack around interfering eventFilter

    /**
     * Commits the text, fires an actionEvent and consumes the keyEvent if it
     * was "used by the textField" in any way, otherwise let it pass through the
     * normal dispatch.
     * <p>
     * "Used" by the textField is true if
     * <ul>
     * <li>text is dirty or
     * <li>the actionEvent is consumed
     * <li>the textField has an onAction handler
     * </ul>
     * 
     * <p>
     * <b>Note</b>: action firing and checking for being consumed is
     * encapsulated into separate methods that are aware of the hack around
     * interfering eventFilters. For the hack to be effective, the textfield must
     * have this skin installed and client code must
     * cooperate by calling the static utility method to consume the action.
     * 
     * <code><pre>
     * textField.addEventHandler(action -> {
     *     doStuff();
     *     XTextFieldSkin.conumeAction(action, textField)); 
     * });
     * </pre></code>
     *
     * PENDING JW: must respect TextInputControlBehavior.disableForwardToParent, if/how?
     * 
     * @param e the keyEvent that triggered the mapping.
     */
    protected void fire(KeyEvent e) {
        /*
         * PENDING JW: really want the last bullet? Why should the onAction be
         * treated differently from the others? Maybe because they typically don't
         * consume in "normal" client code?
         * 
         */
        // note: this differs from experiment in TextFieldValueAndDefaultCancel
        TextField textField = getSkinnable();
        EventHandler<ActionEvent> onAction = textField.getOnAction();
        ActionEvent actionEvent = new ActionEvent(textField, textField);
        boolean dirty = isDirty(textField);

        // first commit, then fire
        textField.commitValue();
        fireAction(actionEvent);
        // key is really "used", consume
        if (dirty || isConsumed(actionEvent) || onAction != null) {
            e.consume();
        }
        // original
//        if (onAction == null && !actionEvent.isConsumed()) {
//            forwardToParent(e);
//        }
    }

    /**
     * Cancels the field's edit and consumes the keyEvent if it
     * was "used by the textField", otherwise let it pass through the
     * normal dispatch.
     * <p>
     * "Used" by the textField is true if text is dirty.
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

        KeyMapping keyMapping = new KeyMapping(binding, this::fire);
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
    
//------------------------ unused debugging help    
    /**
     * Copy from XTextInputControlBehavior. Not used here.
     * @param event
     */
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
     * ActionEvent that keeps the consumed flag on copyFor.
     * 
     * Not used here.
     * @author Jeanette Winzenburg, Berlin
     */
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
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XTextFieldSkin.class.getName());

}
