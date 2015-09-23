/*
 * Created on 02.09.2015
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import static javafx.beans.binding.StringExpression.*;
/**
 * Commit on focusLost. Not supported in core, needs manual listening
 * to focusedProperty and the custom code to transfer the text into
 * the valueFactory.
 * 
 * This is an alternative solution via a TextFormatter. A field with
 * formatter guarantees to commit on focusLost (see doc of 
 * TextFormatter.getValueConverter()). 
 * 
 * So here we install a TextConverter (configured with the properties from
 * the spinner factory) and bidi-bind the factory's value to the formatter value.
 * 
 * http://stackoverflow.com/q/32340476/203657
 */
public class SpinnerCommitOnFocusLostByTextFormatter extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage stage)
    {
        Spinner spinner = new Spinner();

        // normal setup of spinner
        SpinnerValueFactory factory = new IntegerSpinnerValueFactory(0, 10000, 0);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        // hook in a formatter with the same properties as the factory
        TextFormatter formatter = new TextFormatter(factory.getConverter(), factory.getValue());
        spinner.getEditor().setTextFormatter(formatter);
        // bidi-bind the values
        factory.valueProperty().bindBidirectional(formatter.valueProperty());
        
        Label spinnerValue = new Label(); //spinner.getEditor().getText());
        spinnerValue.textProperty().bind(stringExpression(factory.valueProperty()));
//        Bindings.bindBidirectional(spinnerValue.textProperty(), factory.valueProperty(), factory.getConverter());
//        spinner.valueProperty().addListener((source, ov, nv) -> {
//            LOG.info("old/new value" + ov + " / " + nv);
//            spinnerValue.setText(factory.getConverter().toString(nv));
//            
//        });
//        spinner.focusedProperty().addListener((s, ov, nv) -> {
//            commitEditorText(spinner);
//            // no effect
//            //spinner.getEditor().commitValue();
//            LOG.info("focused spinnerValue/modelValue/editor " 
//                    + getState(spinner));
//        });
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;

        grid.add(new Label("Spinner:"), 0, row);
        grid.add(spinner, 1, row);

        row++;
        Button dummy = new Button("Dummy - focus");
        grid.add(dummy, 0, row);
        
        row++;
        grid.add(new Label("Spinner value: "), 0, row);
        grid.add(spinnerValue, 1, row);
        Scene scene = new Scene(grid, 350, 300);

        stage.setTitle("Hello Spinner");
        stage.setScene(scene);
        stage.show();
    }
    /**
     * c&p from Spinner
     */
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }
    
    private String getState(Spinner spinner) {
        return spinner.getValue() + " / " + spinner.getValue() + " / "
                + spinner.getEditor().getText();

    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SpinnerCommitOnFocusLostByTextFormatter.class
            .getName());
}