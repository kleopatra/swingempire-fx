/*
 * Created on 03.03.2016
 *
 */
package de.swingempire.fx.scene.control.text;

import static javafx.scene.control.TextFormatter.*;

import de.swingempire.fx.scene.control.comboboxx.ComboBoxX;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Issue: text not yet committed when receiving the actionEvent.
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8152557
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldAction extends Application {

    private Parent getContent() {
        // ComboBox behaves as expected
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
//        ComboBoxX<String> comboBox = new ComboBoxX<>(items);
        ComboBox<String> comboBox = new ComboBox<String>(items);
        comboBox.setEditable(true);
        comboBox.setValue(items.get(0));
        comboBox.setOnAction(e -> {
            System.out.println("combo action: " + 
                    comboBox.getEditor().getText() + "=" + comboBox.getValue());
        });

        // compare to TextField with TextFormatter: value not yet committed
        TextField textField = new TextField();
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
        
        textField.setTextFormatter(formatter);
        textField.setOnAction(e -> {
            System.out.println("textfield action: " + 
                    textField.getText() + "=" + formatter.getValue());
        });
        // compare Spinner
        Spinner spinner = new Spinner();
        // normal setup of spinner
        SpinnerValueFactory factory = new IntegerSpinnerValueFactory(0, 10000, 0);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        // spinner has no action (should it?
        // spinner.setOnAction(e -> {
        // using action of editor
        spinner.getEditor().setOnAction(e -> {
            System.out.println("spinner-editor action: " + 
                    spinner.getEditor().getText() + "=" + spinner.getValue());
            
        });
        
        VBox box = new VBox(10, textField, comboBox, spinner);
        return box;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
