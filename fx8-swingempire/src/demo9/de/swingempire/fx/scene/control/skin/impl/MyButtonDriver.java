/*
 * Created on 10.01.2019
 *
 */
package de.swingempire.fx.scene.control.skin.impl;


import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

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
public class MyButtonDriver extends Application {
    
    /**
     * example code from StyleablePropertyFactory.
     */
    private static class MyButton extends Button {

        private static final StyleablePropertyFactory<MyButton> FACTORY 
            = new StyleablePropertyFactory<>(Button.getClassCssMetaData());

        MyButton(String labelText) {
            super(labelText);
            getStyleClass().add("my-button");
        }

        // Typical JavaFX property implementation
//        public StyleableProperty<Boolean> selectedProperty() { return selected; }
        public ObservableValue<Boolean> selectedProperty() { return (ObservableValue<Boolean>)selected; }
//        public BooleanProperty selectedProperty() { return selected; }
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
    int count;
    private Parent createContent() {
        MyButton button = new MyButton("styleable button");
        button.setOnAction(e ->  {
            // does not work: reset on applyCss
            boolean isSelected = button.isSelected();
            button.setSelected(!isSelected);
//            LOG.info("old/selected: " + isSelected + " / " + button.isSelected());
        });
        CheckBox box = new CheckBox("button selected");
        box.selectedProperty().bind(button.selectedProperty());
        
        button.selectedProperty().addListener(c -> {
            LOG.info("selected invalidated " + button.selected.getValue());
            });
        
        Button toggle = new Button("toggle button");
        toggle.setOnAction(e -> {
            boolean isSelected = button.isSelected();
            button.setSelected(!isSelected);
            LOG.info("old/selected: " + isSelected + " / "
                    + button.isSelected());
        });
        
        // plain label, plain text-fill property that's applied via css
        Label custom = new Label("just some label with text");
        
        Button toggleLabel = new Button("toggle label");
        toggleLabel.setOnAction(e -> custom.setTextFill(Color.RED));
        
        Button css = new Button("applyCss");
        css.setOnAction(e -> {
            custom.applyCss();
            custom.layout();
        });
        
        BorderPane content = new BorderPane(button);
        content.setBottom(new HBox(10, box, toggle, toggleLabel, css));
        content.setTop(custom);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 300, 200));
        URL uri = getClass().getResource("xstyleable.css");
//        stage.getScene().getStylesheets().add(uri.toExternalForm());
        Application.setUserAgentStylesheet(uri.toExternalForm());

        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MyButtonDriver.class.getName());

}
