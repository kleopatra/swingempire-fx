/*
 * Created on 31.01.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import de.swingempire.fx.scene.ComboSkinFactory.YComboBoxListViewSkin;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/48538763/203657
 * re-added combo is focused but not clickable
 */
public class ReaddFocusedCombo extends Application {

    // toggle to add/remove either on action or on selection
    private final static boolean ON_ACTION = false;
    // toggle to show between add/remove
    private final static boolean SHOW_IN_BETWEEN = true;
    // toggle to show initially
    private final static boolean SHOW_INITIALLY = false;
    
    @Override
    public void start(Stage stage) {
        final ComboBox<String> choices = new ComboBox<>() {

//            @Override
//            protected Skin<?> createDefaultSkin() {
//                return new YComboBoxListViewSkin<>(this);
//            }

        };
        choices.getItems().add("Test1");
        choices.getItems().add("Test2");
        if (SHOW_INITIALLY) {
            choices.show();
        }
        VBox root = new VBox();

        root.getChildren().add(choices);

        choices.focusedProperty()
                .addListener(e -> LOG.info("Choices focused: " + choices.isFocused()));
        // adding listener after skin is attached has no effect
        if (ON_ACTION) {
            choices.setOnAction(e -> {
                root.getChildren().clear();
                LOG.info("action: " + choices.getScene());
                if (SHOW_IN_BETWEEN) {
                    choices.show();
                }
                root.getChildren().add(choices);
            });

        } else {
            choices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // guess by sillyfly: combo gets confused if popup still
                // open
//                choices.hide();
                root.getChildren().clear();
                LOG.info("selection: " + choices.getScene());
                if (SHOW_IN_BETWEEN) {
                    choices.show();
                }
                root.getChildren().add(choices);
                // suggested in answer: working but then the choice
                // isn't focused
                // root.requestFocus();
                // doesn't work
                // choices.requestFocus();
            });

        }
        
        Label state = new Label(
                " action: " + ON_ACTION + " show in between:  " + SHOW_IN_BETWEEN + " initial " + SHOW_INITIALLY);
        Scene scene = new Scene(root, 700, 100);
        stage.setScene(scene);
        scene.focusOwnerProperty().addListener(
                e -> LOG.info("focusOwner: " + scene.getFocusOwner()));
        stage.show();
        stage.setTitle(choices.getSkin().getClass().getSimpleName() + state.getText());

    }
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ReaddFocusedCombo.class.getName());
    
}

