/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-24898
 * 
 * slightly different than the selectFirst memory
 * content of second cb depends on first
 * - select fruits in first
 * - select first fruit in second
 * - select cars in first
 * - expected: selection in second cleared
 * - actual reported: selectedIndex in second kept, that is 
 *      selectedItem/value updated to first car in second
 *      (that's the memory bug, maybe)
 * - actual 8u20: selectedItem in second kept at fruit
 * clearSelection doesn't make a difference!
 * 
 * Note: comboBoxX behaves as expected
 */
public class ComboSelectedItemRT_24898 extends Application {

    private ComboBoxX<String> comboBoxA = new ComboBoxX();

    private ComboBoxX<String> comboBoxB = new ComboBoxX();
//    private ComboBox<String> comboBoxA = new ComboBox();
//
//    private ComboBox<String> comboBoxB = new ComboBox();

    @Override
    public void start(Stage stage) throws Exception {

        HBox hbox = new HBox();
        hbox.getChildren().addAll(comboBoxA, comboBoxB);

        comboBoxA.getItems().addAll("Fruits", "Cars");
        comboBoxA.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov,
                            String t, String newValue) {
                        if ("Fruits".equals(newValue))
                            comboBoxB.getItems().setAll("Apple", "Orange");
                        else if ("Cars".equals(newValue))
                            comboBoxB.getItems().setAll("Volkswagen", "Volvo");
//                         comboBoxB.getSelectionModel().clearSelection();
                    }
                });
        stage.setWidth(800);
        stage.setScene(new Scene(hbox));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
