/*
 * Created on 23.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;

/**
 * Quick fix for RT-38724: update value on change of selectionModel.
 * 
 * Has two parts:
 * <ul>
 * <li> install a ChangeListener on selectionModelProperty and let it update the value 
 * <li> install a custom skin that (reflectively) calls updateSelection in addition
 *   to super calling updateSelectionModel
 * </ul>
 * 
 * <p>
 * fixed in core 
 * PENDING JW (at least the listener in ChoiceBox, what role does the skin play?)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxRT38724<T> extends ChoiceBox<T> {

    public ChoiceBoxRT38724(ObservableList<T> items) {
        super(items);
        installListenerFix();
    }
    
    /**
     * Fix for RT38724: listening to selectionModel property and update value.
     * 
     * PENDING JW: what to do if choice' value is bound?
     * - write value back into model?
     * - do nothing? that's what happens here and in core, leaves model and
     *   value out of synch ...
     */
    private void installListenerFix() {
        ChangeListener<SingleSelectionModel<T>> modelListener = (p, old, value) -> {
            updateValueFromSelectionModel(value != null ? value.getSelectedItem() : null);
        };
        selectionModelProperty().addListener(modelListener);
    }

    protected void updateValueFromSelectionModel(T t1) {
        if (! valueProperty().isBound()) {
            setValue(t1);
        }
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChoiceBoxSkinRT38724<>(this);
    }

}
