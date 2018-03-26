/*
 * Created on 27.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-17697
 * The behaviour there:
 * 
 * - initially, the choicebox has the same width as the reset button
 * - after clicking add, the width remains the same
 * - after moving mouse over the box, the width shrinks
 * 
 * Now the behaviour is:
 * 
 * - initially the box has the width of the reset button
 * - after clicking add, the width shrinks 
 * - open dropdown (without selecting anything)
 * - click add again (or reset): the width expands shortly, then shrinks again
 * 
 * Newer issue: https://javafx-jira.kenai.com/browse/RT-20472
 * didn't test that - but its still open, Jonathan evaluated to 
 * calling choiceBox.autoresize in reaction to isShowing property
 * was hack around 9071.
 */
public class ChoiceBoxResizingRT17697 extends Application {

    VBox root;

    @Override
    public void start(Stage stage) {
        stage.setTitle(this.getClass().getSimpleName());
        stage.setScene(new MainScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(ChoiceBoxResizingRT17697.class, args);
    }

    public final class MainScene extends Scene {
        ObservableList list = FXCollections.observableArrayList();
        ChoiceBox cb = new ChoiceBox(list);
        ChoiceBoxX cbx = new ChoiceBoxX(list);

        
        public MainScene() {
            super(root = new VBox(10), 300, 300);

            reset();

            Button reset = new Button("reset");
            reset.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    reset();
                }
            });

            Button clear = new Button("add");
            clear.setTooltip(new Tooltip("add"));
            clear.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    cb.getItems().add("New Item");
                }
            });

            root.getChildren().add(clear);
            root.getChildren().add(reset);
            root.getChildren().addAll(cb, cbx);
        }

        protected void reset() {
            cb.getItems().clear();
            for (int i = 0; i < 10; i++) {
                cb.getItems().add(i);
            }
        }
    }
}