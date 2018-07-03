/*
 * Created on 03.07.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Bug: Picker with showing property true doesn't open popup initially.
 * 
 * Plus similar misbehaviour as https://bugs.openjdk.java.net/browse/JDK-8197846
 * that is clicking the calendar button has no effect.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DatePickerShowingInitially extends Application {

    private Parent createContent() {
        DatePicker picker = new DatePicker();
        picker.show();
        return new BorderPane(picker);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 100, 100));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DatePickerShowingInitially.class.getName());

}
