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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Commit on focusLost. Not supported in core, needs manual listening
 * to focusedProperty and the custom code to transfer the text into
 * the valueFactory.
 * http://stackoverflow.com/q/32340476/203657
 */
public class SpinnerCommitOnFocusLost extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage stage)
    {
        final Spinner spinner = new Spinner();

        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000));
        spinner.setEditable(true);

        spinner.valueProperty().addListener((source, ov, nv) -> 
            LOG.info("old/new value" + ov + " / " + nv));
        spinner.focusedProperty().addListener((s, ov, nv) -> {
            commitEditorText(spinner);
            // no effect
            //spinner.getEditor().commitValue();
            LOG.info("focused spinnerValue/modelValue/editor " 
                    + getState(spinner));
        });
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;

        grid.add(new Label("Spinner:"), 0, row);
        grid.add(spinner, 1, row);

        Button dummy = new Button("Dummy - focus");
        grid.add(dummy, 0, 1);
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
    private static final Logger LOG = Logger.getLogger(SpinnerCommitOnFocusLost.class
            .getName());
}