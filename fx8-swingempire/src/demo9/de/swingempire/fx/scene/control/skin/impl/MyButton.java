/*
 * Created on 09.01.2019
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Button;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public final class MyButton extends Button {

    private static final StyleablePropertyFactory<MyButton> FACTORY = new StyleablePropertyFactory<>(Button.getClassCssMetaData());

    MyButton(String labelText) {
        super(labelText);
        getStyleClass().add("my-button");
    }

    // Typical JavaFX property implementation
    public ObservableValue<Boolean> selectedProperty() { return (ObservableValue<Boolean>)selected; }
    public final boolean isSelected() { return selected.getValue(); }
    public final void setSelected(boolean isSelected) { selected.setValue(isSelected); }

    // StyleableProperty implementation reduced to one line
    final StyleableProperty<Boolean> selected =
        FACTORY.createStyleableBooleanProperty(this, "selected", "-my-selected", s -> s.selected);

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return FACTORY.getCssMetaData();
    }

}
