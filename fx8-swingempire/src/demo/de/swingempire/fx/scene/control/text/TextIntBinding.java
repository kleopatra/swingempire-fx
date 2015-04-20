/*
 * Created on 08.04.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import java.text.NumberFormat;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * Parse and bind int to text
 * http://stackoverflow.com/q/29515481/203657
 */
public class TextIntBinding extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        TextField price = new TextField();
        NumberFormat format = NumberFormat.getNumberInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                try {
                    // not really good: format accepts the text as long as 
                    // it can parse its beginning
                    format.parse(c.getControlNewText());
                    return c;
                } catch (Exception ex) {
                    LOG.info("rejected" + c.getControlText() + "/" + c.getControlNewText());
                    return null; 
                }
            }
            return c;
        };
        TextFormatter<Double> priceFormatter = new TextFormatter<Double>(
                new DoubleStringConverter(), 0.,
                filter);
        price.setTextFormatter(priceFormatter);
        TextField quantity = new TextField();
        TextFormatter<Integer> quantityFormatter = new TextFormatter<>(new IntegerStringConverter());
        quantity.setTextFormatter(quantityFormatter);
        
        Binding<Number> multiply = Bindings.multiply(DoubleProperty.doubleProperty(priceFormatter.valueProperty()), 
                IntegerProperty.integerProperty(quantityFormatter.valueProperty()));
        TextField result = new TextField();
        result.setEditable(false);
        TextFormatter resultFormatter = new TextFormatter(new DoubleStringConverter());
        resultFormatter.valueProperty().bind(multiply);
        result.setTextFormatter(resultFormatter);
        VBox box = new VBox(price, quantity, result);
        return box;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TextIntBinding.class
            .getName());
}
