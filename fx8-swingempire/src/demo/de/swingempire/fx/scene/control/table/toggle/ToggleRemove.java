/*
 * Created on 11.08.2015
 *
 */
package de.swingempire.fx.scene.control.table.toggle;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ToggleButton keeps its state (and still related to group) even if hidden
 * or removed.
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class ToggleRemove extends Application {

    private ToggleGroup group;
    private ToggleButton lastHidden;
    private ToggleButton lastRemoved;
    
    private Parent getContent() {
        group = new ToggleGroup();
        HBox toggles = new HBox();
        for (int i = 0; i < 10; i++) {
            ToggleButton button = new ToggleButton("toggle " + i);
            button.setToggleGroup(group);
            toggles.getChildren().add(button);
        }
        
        Button hide = new Button("Hide");
        hide.setOnAction(e -> {
            Toggle toggle = group.getSelectedToggle();
            if (toggle instanceof ToggleButton) {
                ((ToggleButton) toggle).setVisible(false);
                lastHidden = (ToggleButton) toggle;
                LOG.info(((Labeled) toggle).getText() + toggle.isSelected());
            }
        });
        
        Button log = new Button("Log");
        log.setOnAction(e -> {
            String hidden = lastHidden != null ? lastHidden.getText() + lastHidden.isSelected() : "none";
            String removed = lastRemoved != null ? lastRemoved.getText() + lastRemoved.isSelected() : "none";
            LOG.info("hidden " + hidden + " removed " + removed);
        });
        
        Button remove = new Button("Remove");
        remove.setOnAction(e -> {
            Toggle toggle = group.getSelectedToggle();
            if (toggle instanceof ToggleButton) {
                toggles.getChildren().remove(toggle);
                lastRemoved = (ToggleButton) toggle;
                LOG.info(((Labeled) toggle).getText() + toggle.isSelected());
            }
             
        });
        HBox actions = new HBox(10, log, hide, remove);
        
        VBox content = new VBox(10, toggles, actions);
        return content;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ToggleRemove.class
            .getName());
}
