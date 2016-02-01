/*
 * Created on 22.09.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import de.swingempire.fx.util.FXUtils;
import static javafx.beans.binding.StringExpression.*;
import static javafx.scene.control.TextFormatter.*;

/**
 * Commit values in editable Spinner/ComboBox/TextField via TextFormatter.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommitOnFocusLost extends Application {

    private void addSpinner(GridPane grid, int row, boolean bind) {
        Spinner control = new Spinner();
        // normal setup of spinner
        SpinnerValueFactory factory = new IntegerSpinnerValueFactory(0, 10000, 0);
        control.setValueFactory(factory);
        control.setEditable(true);
        // hook in a formatter with the same properties as the factory
        TextFormatter formatter = new TextFormatter(factory.getConverter(), factory.getValue());
        control.getEditor().setTextFormatter(formatter);
        // bidi-bind the values
        if (bind)
            factory.valueProperty().bindBidirectional(formatter.valueProperty());
        
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(stringExpression(factory.valueProperty()));
        
        String bound = bind ? " (bound): " : ": ";
        String labelText = "Spinner" + bound;
        addRow(grid, row, labelText, control, valueLabel);
    }

    private void addTextField(GridPane grid, int row) {
        TextField control = new TextField();
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
        control.setTextFormatter(formatter);
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(stringExpression(formatter.valueProperty()));

        String labelText = "TextField: ";
        addRow(grid, row, labelText, control, valueLabel);

    }
    
    private void addComboBox(GridPane grid, int row, boolean bind) {
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
        ComboBox<String> comboBox = new ComboBox(items);
        comboBox.setConverter(IDENTITY_STRING_CONVERTER);
        comboBox.setEditable(true);
        // don't need in jdk9b99 JDK-8120120 (aka RT-21454) and JDK-8136838 are fixed
        TextFormatter<String> formatter = new TextFormatter<>(comboBox.getConverter());
        comboBox.getEditor().setTextFormatter(formatter);
        if (bind) {
          comboBox.valueProperty().bindBidirectional(formatter.valueProperty());
        }
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(stringExpression(comboBox.valueProperty()));
        
        String bound = bind ? " (bound): " : ": ";
        String labelText = "ComboBox" + bound;

        addRow(grid, row, labelText, comboBox, valueLabel);
    }
    
    private void addRow(GridPane grid, int row, String labelText, Node control, Node valueLabel) {
        int col = 0;
        grid.add(new Label(labelText), col++, row);
        grid.add(control, col++, row);
        grid.add(new Label("value: "), col++, row);
        grid.add(valueLabel, col++, row);
        
    }
    /**
     * @return
     */
    private Parent getContent() {
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;
        addTextField(grid, row++);
        addSpinner(grid, row++, true);
        addComboBox(grid, row++, true);
        return grid;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 600));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
