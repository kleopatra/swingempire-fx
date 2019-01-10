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
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class MyButton2Driver extends Application {
    
    /**
     * Example from StyleablePropertyFactory: doesn't compile
     * @author Jeanette Winzenburg, Berlin
     */
    public static class MyButton2 extends Button {

        private static final StyleablePropertyFactory<MyButton2> FACTORY =
            new StyleablePropertyFactory<>(Button.getClassCssMetaData()) {
            {
//                createBooleanCssMetaData("-my-selected", s -> s.selected, false, false);
            }
        };

        private static final CssMetaData CSS_META = FACTORY.createBooleanCssMetaData(
                "-my-selected", s -> s.selected, false, false);
        
        MyButton2(String labelText) {
            super(labelText);
            getStyleClass().add("my-button2");
        }

        // Typical JavaFX property implementation
        public ObservableValue<Boolean> selectedProperty() { return (ObservableValue<Boolean>)selected; }
        public final boolean isSelected() { return selected.getValue(); }
        public final void setSelected(boolean isSelected) { selected.setValue(isSelected); }

        // StyleableProperty implementation reduced to one line
        private final StyleableProperty<Boolean> selected =
                // original line, doesn't compile
//            new SimpleStyleableBooleanProperty(this, "selected", "my-selected");
            // this comiles but is it useful?    
            new SimpleStyleableBooleanProperty(CSS_META) {

                @Override
                protected void invalidated() {
                    LOG.info("getting new value? " + get());
                    new RuntimeException("woh is colling? \n" ).printStackTrace();
                }
        
        };

        public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
            return FACTORY.getCssMetaData();
        }

        @Override
        public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
            return FACTORY.getCssMetaData();
        }
    }

    private Parent createContent() {
        MyButton2 button = new MyButton2("styleable button");
        button.setOnAction(e ->  {
            boolean isSelected = button.isSelected();
            button.setSelected(!isSelected);
            LOG.info("old/selected: " + isSelected + " / " + button.isSelected());
        });
        CheckBox box = new CheckBox("button selected");
        box.selectedProperty().bind(button.selectedProperty());
        BorderPane content = new BorderPane(button);
        content.setBottom(box);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("xstyleable.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());

        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MyButton2Driver.class.getName());

}
