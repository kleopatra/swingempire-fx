/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.control;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
        DatePicker picker = new DatePicker(LocalDate.now());
        picker.valueProperty().addListener((p, oldValue, newValue) -> {
            if (newValue == null) return;
            WeekFields fields = WeekFields.of(Locale.getDefault());
            // # range 1 ... 53 with overlapping
            int week = newValue.get(fields.weekOfYear());
            // # range 0 ... 52 without overlapping
            int weekBased = newValue.get(fields.weekOfWeekBasedYear());
            LOG.info("week/Based " + week + "/" + weekBased);
        });
        // quick try: select text on focusGained
        // http://stackoverflow.com/q/32349533/203657
        picker.focusedProperty().addListener((source, ov, nv) -> 
            {
                // requires wrapper, otherwise nothing selected
                // note: this differs from plain textField behaviour
                // that's all selected automatically
                Platform.runLater(() -> {
                    picker.getEditor().selectAll();
                });
            });
        TextField field = new TextField("domething to focus");
//        BorderPane pane = new BorderPane();
//        pane.setTop(field);
//        pane.setBottom(picker);
        VBox pane = new VBox(10, picker, field);
        // quick check for menubar: how to get to menubar from menuItem/menu?
//        Menu menu = new Menu("dummy menu");
//        MenuItem menuItem = new MenuItem("dummy");
//        menuItem.setOnAction(e -> {
//            // both are null for a menu?
//            LOG.info("got item: " + menu.getParentPopup() + menu.getParentMenu() + menu.getProperties());
//            }
//        );
//        menu.getItems().addAll(menuItem, new MenuItem("dummy2"));
//        MenuBar bar = new MenuBar(menu);
//        pane.setTop(bar);
        // end menubar check
        return pane;
    }

    public void initialize(URL arg0, ResourceBundle arg1) {
        
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        scene.getStylesheets().add(getClass().getResource("comboboxsize.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DatePickerExample.class
            .getName());
}
