/*
 * Created on 18.04.2020
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/61277373/203657 KeyHandler on scene not notified
 * for navigation keys.
 * 
 * suspected: focustraversal interfers .. looks like: adding another button (or
 * focusTraversable node) transfers focus on left/right
 * 
 * 
 */
public class SceneKeyNavigation extends Application {

    @Override
    public void start(Stage mainStage) {
        VBox vbox = new VBox();
        Label welcome = new Label("Welcome to CST Man Adventures");
        Label survive = new Label("See how many rounds you can survive!");

        vbox.setAlignment(Pos.BASELINE_CENTER);
        vbox.getChildren().add(welcome);
        vbox.getChildren().add(survive);
        vbox.getChildren().addAll(new Button("yet another - do we get here?"));

        Button start = new Button("Start Round");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(start, new Button("dummy"));
        hbox.getChildren().add(new Label("Current round: 1"));
        vbox.getChildren().add(hbox);
        hbox.setTranslateY(100);

        Scene scene = new Scene(vbox, 400, 400);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP) {
                    System.out.println();
                } else if (event.getCode() == KeyCode.LEFT) {
                    System.out.println();
                } else if (event.getCode() == KeyCode.RIGHT) {
                    System.out.println();
                } else if (event.getCode() == KeyCode.DOWN) {
                    System.out.println();
                }
                event.consume();
            }
        });
        mainStage.setScene(scene);
        mainStage.setTitle("CST Man Adventures");
        mainStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}