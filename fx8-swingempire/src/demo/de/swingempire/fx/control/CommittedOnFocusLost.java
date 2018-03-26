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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8151129
 * Combo commits edited text too late
 * Spinner might be similar, can't tell without bug missing commitOnFocusLost fixed
 * https://bugs.openjdk.java.net/browse/JDK-8150946
 * 
 * <p>
 * experimenting after Jonathan's comment (and lowering priority), marked 
 * by "experimenting".
 * 
 * <p>
 * An idea that seems to be working is to let a custom Combo (Spinner, Picker)
 * register a listener to the focusedProperty and commit on lost. That's
 * what core TextInputControl does.
 * 
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

        textField.setOnAction(e -> {
            System.out.println("textfield action: " + 
                    textField.getText() + "=" + formatter.getValue());
        });
        // compare to combo
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
        ComboBox<String> comboBox = new ComboBox<String>(items) {
            {
                focusedProperty().addListener((src, ov, nv) -> {
                    if (!nv && isEditable()) {
                        setValue(getEditor().getText());
                        System.out.println("focused in combo constructor");
                    }
                });
            }

        };
        comboBox.getEditor().focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                System.out.println("editor focused " + comboBox.getEditor().getText() + " = " + comboBox.getValue());
            }
        });
//        ComboBoxX<String> comboBox = new ComboBoxX<>(items);
        comboBox.setEditable(true);
        comboBox.setValue(items.get(0));
        // experimenting
//        TextFormatter<String> formatterC = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
//        comboBox.getEditor().setTextFormatter(formatterC);
//        formatterC.valueProperty().addListener((src, ov, nv) -> {
////            comboBox.setValue(nv);
//            System.out.println("formatter committed: " + nv + "=" + comboBox.getValue());
//        });
        // end of experimenting
        comboBox.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                System.out.println("combo committed: " + 
                     comboBox.getEditor().getText() + "=" + comboBox.getValue());
            }
        });

        // compare Spinner
        Spinner spinner = new Spinner();
        // normal setup of spinner
        SpinnerValueFactory factory = new IntegerSpinnerValueFactory(0, 10000, 0);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        // hook in a formatter with the same properties as the factory
//        TextFormatter sFormatter = new TextFormatter(factory.getConverter(), factory.getValue());
//        control.getEditor().setTextFormatter(sFormatter);
//        // bidi-bind the values
//        if (bind)
//            factory.valueProperty().bindBidirectional(formatter.valueProperty());
        spinner.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                System.out.println("spinner committed: " + 
                     spinner.getEditor().getText() + "=" + spinner.getValue());
            }
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
