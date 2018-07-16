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
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Bug check: ComboBase with showing property true doesn't open popup initially.
 * 
 * DatePicker/ColorPicker: bug, 
 * Plus similar misbehaviour as https://bugs.openjdk.java.net/browse/JDK-8197846
 * that is clicking the calendar button has no effect.
 * 
 * Combo: okay
 * 
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBaseShowingInitially extends Application {

    private Parent createContent() {
//        ComboBoxBase<?> comboBase = new DatePicker();
       // combo is fine because showing is sync'ed in skin constructor 
        ComboBoxBase<?> comboBase = new ComboBox<>();
//        ComboBoxBase<?> comboBase = new ColorPicker();
        comboBase.show();
        return new BorderPane(comboBase);
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
            .getLogger(ComboBaseShowingInitially.class.getName());

}
