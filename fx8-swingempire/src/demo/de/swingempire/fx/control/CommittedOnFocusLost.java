/*
 * Created on 03.03.2016
 *
 */
package de.swingempire.fx.control;

import static javafx.scene.control.TextFormatter.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class CommittedOnFocusLost extends Application {

    private Parent getContent() {
        // TextField with TextFormatter: behaves as expected:
        TextField textField = new TextField();
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
        textField.setTextFormatter(formatter);
        textField.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                System.out.println("textfield committed: " + 
                     textField.getText() + "=" + formatter.getValue());
            }
        });

        
        // compare to combo
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
        ComboBox<String> comboBox = new ComboBox<>(items);
        comboBox.setEditable(true);
        comboBox.setValue(items.get(0));
        comboBox.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                System.out.println("combo committed: " + 
                     comboBox.getEditor().getText() + "=" + comboBox.getValue());
            }
        });
        VBox box = new VBox(10, textField, comboBox);
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
