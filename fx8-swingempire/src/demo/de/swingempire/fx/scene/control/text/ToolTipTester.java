/*
 * Created on 14.04.2016
 *
 */
package de.swingempire.fx.scene.control.text;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/q/36614281/203657
 * cab't set font of tooltip
 * 
 * worksforme
 */
public class ToolTipTester extends Application {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        Group root = new Group();

        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.setTitle(FXUtils.version());

        Button button = new Button("Button");
        root.getChildren().add(button);

        Tooltip tooltip = new Tooltip("My name is the Ghost");
        tooltip.setFont(new Font(50));

        button.setTooltip(tooltip);
        primaryStage.show();

    }
}