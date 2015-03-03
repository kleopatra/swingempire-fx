/*
 * Created on 03.03.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class LabeledFocus extends Application {

    private Labeled createLabel(String text) {
        Labeled label = new Label(text);
        label.setFocusTraversable(true);
        label.setContextMenu(new ContextMenu(new MenuItem(text)));
        label.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            label.requestFocus();
        });
        return label;
    }
    private Parent getContent() {
        HBox pane = new HBox(10, createLabel("first label"), createLabel("secondLabel"));
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
//      scene.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> e.consume());
//      scene.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> e.consume());
      scene.focusOwnerProperty().addListener((source, old, value) -> {
          String change = "focusOwner old/new: " 
                  + "\n old: " + old   
                  + "\n new: " + value;
          LOG.info(change);
      });
      primaryStage.setScene(scene);
      primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(LabeledFocus.class
            .getName());
}
