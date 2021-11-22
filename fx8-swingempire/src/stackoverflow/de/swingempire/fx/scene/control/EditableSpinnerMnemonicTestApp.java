/*
 * Created 14.10.2021 
 */

package de.swingempire.fx.scene.control;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/69562915/203657
 * mnemonics not working on editable Spinner
 */
public class EditableSpinnerMnemonicTestApp extends Application {

    @Override
    public void start(Stage stage) {

        Spinner<Integer>
                spinner = new Spinner<>(0, 0, 0),
                editableSpinner = new Spinner<>(0, 0, 0);

        spinner.setEditable(false); // (default) Mnemonic mode changes when focused and ALT pressed
        editableSpinner.setEditable(true); // Nothing happens when focused and ALT pressed

        Label label = new Label("_Not Editable");
        label.setMnemonicParsing(true);
        label.setLabelFor(spinner);
        label.setPadding(new Insets(0,0,3,0));
        
        Label labelEdit = new Label("_Editable");
        labelEdit.setMnemonicParsing(true);
        labelEdit.setLabelFor(editableSpinner);
        labelEdit.setPadding(new Insets(0,0,3,0));

        TextField field = new TextField("dummy text - to focus");
        Label labelField = new Label("_Text");
        labelField.setMnemonicParsing(true);
        labelField.setLabelFor(field);
        labelField.setPadding(new Insets(0,0,3,0));
        
        stage.setScene(new Scene(new VBox(label, spinner, labelEdit, editableSpinner, labelField, field)));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

