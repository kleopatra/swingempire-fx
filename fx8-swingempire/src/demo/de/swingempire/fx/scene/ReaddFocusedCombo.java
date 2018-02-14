/*
 * Created on 31.01.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import de.swingempire.fx.scene.ReaddFocusedComboBug.YComboBoxListViewSkin;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/48538763/203657
 * re-added combo is focused but not clickable
 */
public class ReaddFocusedCombo extends Application {

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();

        final ComboBox<String> choices = new ComboBox<>() {

            // working but feels intrusive
            // maybe fix the other way round?
            @Override
            protected Skin<?> createDefaultSkin() {
                return new YComboBoxListViewSkin<>(this);
            }
            
        };
        choices.getItems().add("Test1");
        choices.getItems().add("Test2");
        root.getChildren().add(choices);

        choices.focusedProperty().addListener(e -> LOG.info("focused: " + choices.isFocused()));
        // adding listener after skin is attached has no effect
        choices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // guess by sillyfly: combo gets confused if popup still open 
//            choices.hide();
            root.getChildren().clear();
            root.getChildren().add(choices);
            // suggested in answer: working but then the choice isn't focused
            //root.requestFocus();
            // doesn't work
            //  choices.requestFocus();
        });
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.focusOwnerProperty().addListener(e -> LOG.info("focused: " + scene.getFocusOwner()));
        stage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ReaddFocusedCombo.class.getName());
    
}

