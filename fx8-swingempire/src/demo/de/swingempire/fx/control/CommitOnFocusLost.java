/*
 * Created on 22.09.2015
 *
 */
package de.swingempire.fx.control;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static javafx.beans.binding.StringExpression.*;
import static javafx.scene.control.TextFormatter.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Commit values in editable Spinner/ComboBox/TextField via TextFormatter.
 * 
 * <p>
 * Relevant issues:
 * https://bugs.openjdk.java.net/browse/JDK-8136838
 * https://bugs.openjdk.java.net/browse/JDK-8150946 - marked as
 * fixed for fx9
 * 
 * <p>
 * DatePicker: regression? no longer committed on focusLost
 * was fixed in https://bugs.openjdk.java.net/browse/JDK-8136838, 2015-10-02 05:02
 * the fix was in ComboBoxPopupControl, focusListener that calls 
 * setTextFromTextFieldIntoComboBoxValue() on lost
 * 
 * with the advent of commit/cancel logic on Spinner/ComboBox that line was removed
 * again ...
 * https://bugs.openjdk.java.net/browse/JDK-8150946, 2016-06-21 01:46
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommitOnFocusLost extends Application {

    private boolean hasAction;
    private boolean printFocusChange;
    List<Spinner> spinners = new ArrayList<>();
    List<TextField> textFields = new ArrayList<>();
    List<ComboBox> combos = new ArrayList<>();
    List<DatePicker> pickers = new ArrayList<>();
    
    private void addSpinner(GridPane grid, int row, boolean bind, boolean useFormatter) {
        Spinner control = new Spinner();
        spinners.add(control);
        // normal setup of spinner
        SpinnerValueFactory factory = new IntegerSpinnerValueFactory(0, 10000, 0);
        control.setValueFactory(factory);
        control.setEditable(true);
        Supplier<Object> valueSupplier; 
        
        if (useFormatter) {
            // hook in a formatter with the same properties as the factory
            TextFormatter formatter = new TextFormatter(factory.getConverter(), factory.getValue());
            control.getEditor().setTextFormatter(formatter);
            valueSupplier = () -> formatter.getValue();
          // bidi-bind the values
            if (bind)
                factory.valueProperty().bindBidirectional(formatter.valueProperty());
            
            
        } else {
            valueSupplier = () -> " not available ";
          
        }
        control.focusedProperty().addListener((src, ov, nv) -> {
            if (printFocusChange && !nv) {
                LOG.info("formatter/spinner: " + valueSupplier.get() + " / " + control.getValue());
            }
        });
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(stringExpression(factory.valueProperty()));
        
        String formatted = useFormatter ? " (withFormatter) " : "";
        String bound = bind ? " (bound) " : " ";
        String desc = ": ";
        if (useFormatter) {
            desc = formatted + bound + desc;
        }
        String labelText = "Spinner" + desc;
        addRow(grid, row, labelText, control, valueLabel);
    }

    private void addTextField(GridPane grid, int row, boolean useFormatter) {
        TextField control = new TextField();
        textFields.add(control);
        Supplier<String> valueSupplier; 
        
        TextFormatter<String> formatter;
        if (useFormatter) {
            formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
            control.setTextFormatter(formatter);
            valueSupplier = () -> formatter.getValue();
            
        } else {
            formatter = null;
            valueSupplier = () -> " not available ";
        }
        control.focusedProperty().addListener((src, ov, nv) -> {
            if (printFocusChange && !nv) {
                LOG.info("formatter/textfield: " + valueSupplier.get() + " / " + control.getText());
            }
        });
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(stringExpression(formatter != null ? formatter.valueProperty() : control.textProperty()));
        String formatted = useFormatter ? " (withFormatter) " : "";
        String labelText = "TextField: " + formatted;
        addRow(grid, row, labelText, control, valueLabel);

    }

    private void addComboBox(GridPane grid, int row, boolean bind, boolean useFormatter) {
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
        ComboBox<String> comboBox = new ComboBox(items);
        combos.add(comboBox);
        comboBox.getSelectionModel().selectFirst();
        comboBox.setConverter(IDENTITY_STRING_CONVERTER);
        comboBox.setEditable(true);
        Supplier<String> valueSupplier; 
        if (useFormatter) {
            
            // don't need in jdk9b99 JDK-8120120 (aka RT-21454) and JDK-8136838 are fixed
            TextFormatter<String> formatter = new TextFormatter<>(comboBox.getConverter());
            comboBox.getEditor().setTextFormatter(formatter);
            valueSupplier = () -> formatter.getValue();
            if (bind) {
                comboBox.valueProperty().bindBidirectional(formatter.valueProperty());
            }
        } else {
            valueSupplier = () -> " not available ";
        }
        /* quick check: sequence of listener notification
         * https://bugs.openjdk.java.net/browse/JDK-8151129
         */ 
        comboBox.focusedProperty().addListener((src, ov, nv) -> {
            if (printFocusChange && !nv) {
                LOG.info("formatter/combo: " + valueSupplier.get() + " / " + comboBox.getValue());
            }
        });
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(stringExpression(comboBox.valueProperty()));
        
//        comboBox.getSelectionModel().select(items.get(0));
        String formatted = useFormatter ? " (withFormatter) " : "";
        String bound = bind ? " (bound) " : " ";
        String desc = ": ";
        if (useFormatter) {
            desc = formatted + bound + desc;
        }
        String labelText = "ComboBox" + desc;

        addRow(grid, row, labelText, comboBox, valueLabel);
    }
    
    private void addDatePicker(GridPane grid, int row, boolean bind, boolean useFormatter) {
            
            DatePicker datePicker = new DatePicker(LocalDate.now());
            pickers.add(datePicker);
            datePicker.setEditable(true);
            Supplier<Object> valueSupplier; 
            if (useFormatter) {
                
                // don't need in jdk9b99 JDK-8120120 (aka RT-21454) and JDK-8136838 are fixed
                TextFormatter<LocalDate> formatter = new TextFormatter<>(datePicker.getConverter());
                datePicker.getEditor().setTextFormatter(formatter);
                valueSupplier = () -> formatter.getValue();
                if (bind) {
                    datePicker.valueProperty().bindBidirectional(formatter.valueProperty());
                }
            } else {
                valueSupplier = () -> " not available ";
            }
            /* quick check: sequence of listener notification
             * https://bugs.openjdk.java.net/browse/JDK-8151129
             */ 
            datePicker.focusedProperty().addListener((src, ov, nv) -> {
                if (printFocusChange && !nv) {
                    LOG.info("formatter/datepicker: " + valueSupplier.get() + " / " + datePicker.getValue());
                }
            });
            Label valueLabel = new Label(); 
            valueLabel.textProperty().bind(stringExpression(datePicker.valueProperty()));
            
    //        comboBox.getSelectionModel().select(items.get(0));
            String formatted = useFormatter ? " (withFormatter) " : "";
            String bound = bind ? " (bound) " : " ";
            String desc = ": ";
            if (useFormatter) {
                desc = formatted + bound + desc;
            }
            String labelText = "DatePicker" + desc;
            
            addRow(grid, row, labelText, datePicker, valueLabel);
        }

    private void addRow(GridPane grid, int row, String labelText, Node control, Node valueLabel) {
        int col = 0;
        grid.add(new Label(labelText), col++, row);
        grid.add(control, col++, row);
        grid.add(new Label("value: "), col++, row);
        grid.add(valueLabel, col++, row);
    }
    
    private Parent getContent() {
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;
        // textfields
        addTextField(grid, row++, false);
        addTextField(grid, row++, true);
        // combos
        addComboBox(grid, row++, false, false);
        addComboBox(grid, row++, false, true);
        addComboBox(grid, row++, true, true);
        // pickers
        addDatePicker(grid, row++, false, false);
        // spinners
        addSpinner(grid, row++, false, false);
        addSpinner(grid, row++, false, true);
        addSpinner(grid, row++, true, true);
        
        Button ok = new Button("Default OK");
        ok.setDefaultButton(true);
        ok.setOnAction(e -> LOG.info("OK triggered " + hasAction));
        
        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> LOG.info("Cancel triggered " + hasAction));
        
        Button addActions = new Button("add actionHandler");
        addActions.setOnAction(e -> {
            pickers.forEach(t -> t.setOnAction(a -> LOG.info("action from " + t)));
            combos.forEach(t -> t.setOnAction(a -> LOG.info("action from " + t)));
            textFields.forEach(t -> t.setOnAction(a -> LOG.info("action from " + t)));
            hasAction = true;
            addActions.setDisable(true);
        });
        
        HBox buttons = new HBox(10, addActions);
        buttons.getChildren().addAll(ok, cancel);
        
        BorderPane content = new BorderPane(grid);
        content.setPadding(new Insets(10));
        content.setBottom(buttons);
        return content;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400)); //, 600, 300));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CommitOnFocusLost.class.getName());
}
