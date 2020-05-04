/*
 * Created on 24.04.2020
 *
 */
package control;


import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8117826
 * 
 * old id: RT-22386
 * 
 * With a editable ComboBox :
 * - select the first item from the menu button
 * - enter a value in the ComboBox text field
 * Try to select again the same item : this has no effect
 * 
 * As long as the value is not committed, there is no way to revert it
 * to the old selectedItem
 * 
 * fixed against fx2.2 (no test?)
 * Regression in fx11, fx14, fx8 .. or misunderstanding the report? fix is
 * doing in listener to valueProperty, so without commit it can't do anything.
 */
public class ComboSelectOldBug extends Application {

    private final ObservableList<String> strings = FXCollections.observableArrayList(
            "Option 1", "Option 2", "Option 3");
    private int nb = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("ComboBox");

        final TextField textField = new TextField();
        textField.setEditable(false);
        final ComboBox<String> comboBox1 = new ComboBox<String>();
        comboBox1.setId("comboBox-editable");
        comboBox1.setItems(strings);
        comboBox1.setEditable(true);
        comboBox1.setPrefWidth(100);
        comboBox1.setPromptText("Empty");
        TextField comboBoxEditor = comboBox1.getEditor();
        comboBoxEditor.setId("editor");
        comboBoxEditor.setText("Init value");
        comboBoxEditor.setPromptText("Empty");
        comboBox1.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                System.out.println("new value: " + t1);
                textField.setText(t1);
            }
        });

        // programmatically change combo value to uncontained
        Button changeButton = new Button("Change value");
        changeButton.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent t) {
                comboBox1.setValue("" + nb++);
            }
        });

        VBox vbox = new VBox(20);
        vbox.setLayoutX(40);
        vbox.setLayoutY(25);

        vbox.getChildren().addAll(comboBox1, textField, changeButton);
        Scene scene = new Scene(new Group(vbox), 620, 190);

        stage.setScene(scene);
        stage.show();

    }
}
