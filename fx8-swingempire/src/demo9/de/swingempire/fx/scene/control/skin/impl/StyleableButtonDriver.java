/*
 * Created on 10.01.2019
 *
 */
package de.swingempire.fx.scene.control.skin.impl;


import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.css.StyleManager;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * problem to set a property that's initialized via css: it's overwritten to initial
 * on the next call to applyCss().
 * 
 */
public class StyleableButtonDriver extends Application {

    /**
     * example code from class doc of StyleablePropertyFactory.
     */
    private static class MyButton extends Button {

        private static final StyleablePropertyFactory<MyButton> FACTORY 
            = new StyleablePropertyFactory<>(Button.getClassCssMetaData());

        MyButton(String labelText) {
            super(labelText);
            getStyleClass().add("my-button");
//            setStyle("-my-selected: true");
        }

        // Typical JavaFX property implementation
        public ObservableValue<Boolean> selectedProperty() { return (ObservableValue<Boolean>)selected; }
        public final boolean isSelected() { return selected.getValue(); }
        public final void setSelected(boolean isSelected) { selected.setValue(isSelected); }

        // StyleableProperty implementation reduced to one line
        private final StyleableProperty<Boolean> selected =
                FACTORY.createStyleableBooleanProperty(
                        this, "selected", "-my-selected", s -> s.selected);

        @Override
        public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
            return FACTORY.getCssMetaData();
        }

        public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
            return FACTORY.getCssMetaData();
        }

    }
    private Parent createContent() {
        MyButton button = new MyButton("styleable button");
        button.setOnAction(e ->  {
            // does not work: reset on applyCss
            boolean isSelected = button.isSelected();
            button.setSelected(!isSelected);
        });

        CheckBox box = new CheckBox("button selected");
        box.selectedProperty().bind(button.selectedProperty());

        Button toggle = new Button("toggle button");
        toggle.setOnAction(e -> {
            boolean isSelected = button.isSelected();
            button.setSelected(!isSelected);
        });


        BorderPane content = new BorderPane(button);
        content.setBottom(new HBox(10, box, toggle));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 300, 200));
        //same behavior as setting the style directly
                URL uri = getClass().getResource("xstyleable.css");
//                stage.getScene().getStylesheets().add(uri.toExternalForm());
//         not useful: would have to override all, but not needed, bug is fixed
//        Application.setUserAgentStylesheet(null);
        StyleManager.getInstance().addUserAgentStylesheet(uri.toExternalForm());        
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
    .getLogger(StyleableButtonDriver.class.getName());

}
