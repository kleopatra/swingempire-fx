/*
 * Created on 23.09.2014
 *
 */
package de.swingempire.fx.scene.control.rt38724;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;

/**
 * Fix for RT-38724: update value on change of selectionModel.
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
