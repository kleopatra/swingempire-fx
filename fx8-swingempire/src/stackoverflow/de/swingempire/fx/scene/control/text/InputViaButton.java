/*
 * Created on 14.06.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/50847111/203657
 * add text to focused textField. Note: focusTraversable really means that
 * even clicking doesn't transfer the focus into the button.
 * 
 * @author fabian
 */
public class InputViaButton extends Application {

    private Parent createContent() {
        GridPane grid = new GridPane();

        for (int i = 0; i < 4; i++) {
            grid.add(new TextField(), 0, i);

            final String buttonValue = Character.toString((char) ('a'+i));
            Button button = new Button(buttonValue);
            button.setFocusTraversable(false); // prevent buttons from stealing focus
            button.setOnAction(evt -> {
                
                Node fo = grid.getScene().getFocusOwner();
                if (fo instanceof TextInputControl) {
                    ((TextInputControl) fo).replaceSelection(buttonValue);
                }
                // not what we want here, but this is transfering the focus to
                // the button (even when not traversable)
                // button.requestFocus();
            });
            grid.add(button, 1, i);
        }
        return grid;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(InputViaButton.class.getName());

}
