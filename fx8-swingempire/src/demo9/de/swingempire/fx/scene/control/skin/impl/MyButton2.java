/*
 * Created on 10.01.2019
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Button;

/**
 * Example from StyleablePropertyFactory: doesn't compile
 * @author Jeanette Winzenburg, Berlin
 */
public final class MyButton2 extends Button {

    private static final StyleablePropertyFactory<MyButton> FACTORY =
        new StyleablePropertyFactory<>(Button.getClassCssMetaData()) {
        {
            createBooleanCssMetaData("-my-selected", s -> s.selected, false, false);
        }
    };


    MyButton2(String labelText) {
        super(labelText);
        getStyleClass().add("my-button");
    }

    // Typical JavaFX property implementation
    public ObservableValue<Boolean> selectedProperty() { return (ObservableValue<Boolean>)selected; }
    public final boolean isSelected() { return selected.getValue(); }
    public final void setSelected(boolean isSelected) { selected.setValue(isSelected); }

    // StyleableProperty implementation reduced to one line
    private final StyleableProperty<Boolean> selected =
            // original line, doesn't compile
        new SimpleStyleableBooleanProperty(this, "selected", "my-selected");

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return FACTORY.getCssMetaData();
    }
}
