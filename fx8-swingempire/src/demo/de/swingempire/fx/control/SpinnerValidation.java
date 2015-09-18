/*
 * Created on 10.04.2015
 *
 */
package de.swingempire.fx.control;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * http://stackoverflow.com/a/29557191/203657
 */
public class SpinnerValidation extends Application {

    protected static final String INITAL_VALUE = "0";

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        final Spinner<Integer> spinner = new Spinner<>();
        // get a localized format for parsing
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };
        TextFormatter<Integer> priceFormatter = new TextFormatter<Integer>(
                new IntegerStringConverter(), 0,
                filter);

        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 10000, Integer.parseInt(INITAL_VALUE)));
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(priceFormatter);
        
        Label spinnerValue = new Label(spinner.getEditor().getText());
        spinner.getValueFactory().valueProperty().addListener((s, ov, nv) -> {
            spinnerValue.setText(spinner.getValueFactory().getConverter().toString(nv));
        });
        
        Label editorValue = new Label(spinner.getEditor().getText());
        priceFormatter.valueProperty().addListener((s, ov, nv) -> {
            editorValue.setText(priceFormatter.getValueConverter().toString(nv));
        } );
        spinner.focusedProperty().addListener((s, ov, nv) -> {
//            commitEditorText(spinner);
//            spinner.getEditor().commitValue();
            LOG.info("focused spinnerValue/modelValue/editor " 
                    + getState(spinner));
        });
//        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
//                0, 10000, Integer.parseInt(INITAL_VALUE)));
        
        EventHandler<KeyEvent> enterKeyEventHandler;

        // DONT use low-level events for validation
        // note: use KeyEvent.KEY_PRESSED, because KeyEvent.KEY_TYPED is to
        // late, spinners
        // SpinnerValueFactory reached new value before key released an
        // SpinnerValueFactory will
        // throw an exception
//        spinner.getEditor().addEventHandler(KeyEvent.KEY_PRESSED,
//                enterKeyEventHandler);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;

        grid.add(new Label("Spinner:"), 0, row);
        grid.add(spinner, 1, row);
        Button dummy = new Button("Dummy - focus");
        row++;
        grid.add(dummy, 0, row);

        row++;
        grid.add(new Label("Spinner value: " ), 0, row);
        grid.add(spinnerValue, 1, row);
        
        row++;
        grid.add(new Label("editor value: " ), 0, row);
        grid.add(editorValue, 1, row);
        
        TextField field = new TextField();
        TextFormatter fieldFormatter = new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER, "initial");
        field.setTextFormatter(fieldFormatter);
        
        // plain TextField with formatter commits on focusLost
        Label fieldValue = new Label(field.getText());
        row++;
        fieldFormatter.valueProperty().addListener((s, ov, nv) -> {
            fieldValue.setText(fieldFormatter.getValueConverter().toString(nv));
        } );
        grid.add(field, 1, row);
        row++;
        grid.add(new Label("field value: "), 0, row);
        grid.add(fieldValue, 1, row);
        
        Scene scene = new Scene(grid, 350, 300);

        stage.setTitle("Hello Spinner");
        stage.setScene(scene);
        stage.show();
    }
    private String getState(Spinner spinner) {
        return spinner.getValue() + " / " + spinner.getValue() + " / "
                + spinner.getEditor().getText();

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
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SpinnerValidation.class
            .getName());
}
