/*
 * Created on 22.11.2016
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Requirement: unselect on mouse click into selected
 * Idea: dynamic add/remove mouseHandler
 * 
 * - listener must be registered as eventFilter
 * - must consume the event
 * - selectedToggle of group must be nulled (vs unselecting the toggle)
 * - must be done on released
 * 
 * Change to any of those results in weird behavior. Beware: might
 * be OS dependent.
 * 
 * 
 * SO http://stackoverflow.com/q/40722003/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class UnselectableRadioGroup extends Application {

        
    EventHandler<MouseEvent> mouseHandler;

    private void addListeners(RadioButton nv) {
        if (mouseHandler == null) {
            mouseHandler = e -> {
                e.consume();
                nv.getToggleGroup().selectToggle(null);
            };
        }
        nv.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseHandler);
    }

    private void removeListeners(RadioButton ov) {
        ov.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseHandler);
    }
    
    /**
     * Custom radioBox with behaviour of ToggleButton. Advantage (?):
     * keyBindings are working as well.
     */
    public class MyRadioButton extends RadioButton {

        /**
         * Overridden to revert to ToggleButton behaviour.
         */
        @Override
        public void fire() {
            if (!isDisabled()) {
                setSelected(!isSelected());
                fireEvent(new ActionEvent());
            }
        }

        public MyRadioButton(String text) {
            super(text);
        }
        
    }
    private Parent getContent() {
        // dynamic un/register event handlers
        ObservableList<RadioButton> radios = FXCollections.observableArrayList(
                new RadioButton("one"), new RadioButton("other"), new RadioButton("third")
                );
        ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(radios);
        
        group.selectedToggleProperty().addListener((src, ov, nv) -> {
            if (ov instanceof RadioButton) {
                removeListeners((RadioButton) ov);
            }
            if (nv instanceof RadioButton) {
                addListeners((RadioButton) nv);
            }
        });
        
        VBox radioBox = new VBox(10);
        radioBox.getChildren().addAll(radios);
        
        // RadioButton reverted to ToggleButton
        ObservableList<MyRadioButton> toggles = FXCollections.observableArrayList(
                new MyRadioButton("one"), new MyRadioButton("other"), new MyRadioButton("third")
                );
        ToggleGroup myGroup = new ToggleGroup();
        myGroup.getToggles().addAll(toggles);
        VBox myRadioBox = new VBox(10);
        myRadioBox.getChildren().addAll(toggles);
        
        HBox content = new HBox(10, radioBox, myRadioBox);
        BorderPane pane = new BorderPane(content);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(UnselectableRadioGroup.class.getName());
}
