/*
 * Created on 17.02.2015
 *
 */
package de.swingempire.fx.control;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Tooltip flickering (showing/hiding) when setting text in onShowing.
 *  
 * Reported: https://javafx-jira.kenai.com/browse/RT-40080
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class DynamicTooltipRT_40080 extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Button button = new Button(FXUtils.version());
        Tooltip t = new Tooltip("button");
        button.setTooltip(t);
        t.setOnShowing(e -> {
            // side-effect: tooltip hidden/shown
//            t.setText("x/y: " + t.getX() + "/" + t.getY());
            // here we get a stable tooltip
             t.textProperty().set("x/y: " + t.getX() + "/" + t.getY() + "\n" +
             "ax/y: " + t.getAnchorX() + "/" + t.getAnchorY());
        });
        VBox pane = new VBox(button);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(FXUtils.version());
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
