/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.control;

import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * WeekOfDay from DatePicker.
 * http://stackoverflow.com/q/25912455/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DatePickerExample extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        DatePicker picker = new DatePicker();
        picker.valueProperty().addListener((p, oldValue, newValue) -> {
            if (newValue == null) return;
            WeekFields fields = WeekFields.of(Locale.getDefault());
            // # range 1 ... 53 with overlapping
            int week = newValue.get(fields.weekOfYear());
            // # range 0 ... 52 without overlapping
            int weekBased = newValue.get(fields.weekOfWeekBasedYear());
            LOG.info("week/Based " + week + "/" + weekBased);
        });
        Pane pane = new BorderPane(picker);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        scene.getStylesheets().add(getClass().getResource("comboboxsize.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DatePickerExample.class
            .getName());
}
