/*
 * Created on 05.05.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ThousandSeparatorFormatter extends Application {

    TextField textfield;

    
    public void initialize() {
        textfield = new TextField();
        DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getInstance();
        LOG.info("format? " + numberFormat.getClass());
        numberFormat.setMaximumIntegerDigits(6);
        numberFormat.setParseIntegerOnly(true);
        numberFormat.setGroupingUsed(true);
        DecimalFormatSymbols symbols = numberFormat.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        numberFormat.setDecimalFormatSymbols(symbols);

        TextFormatter form = new TextFormatter(new NumberStringConverter( numberFormat));
        //why dont this work?
        textfield.setTextFormatter(form);

        //this thing "works"
//        textfield.onKeyReleasedProperty().setValue(event -> {
//            textfield.setText( String.valueOf( form.getValueConverter().fromString( textfield.getText())));
//        });
    }

    private Parent getContent() {
        initialize();
        VBox pane = new VBox(textfield);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ThousandSeparatorFormatter.class.getName());
}
