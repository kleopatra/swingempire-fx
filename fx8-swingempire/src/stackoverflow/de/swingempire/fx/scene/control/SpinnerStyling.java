/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SpinnerStyling extends Application {
    

    private Parent createContent() {
        Spinner<Integer> spinner = new Spinner<>(0, 100, 50);
        spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        VBox content = new VBox(spinner);
        return content;
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
            .getLogger(SpinnerStyling.class.getName());

}
