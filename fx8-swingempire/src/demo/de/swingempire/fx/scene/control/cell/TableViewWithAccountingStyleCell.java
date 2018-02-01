/*
 * Created on 01.02.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Align currency symbol separately from the number
 * 
 * https://stackoverflow.com/q/48552499/203657
 */
public class TableViewWithAccountingStyleCell extends Application {

    public static class PriceTableCell<S> extends TableCell<S, Long> {

        private final AnchorPane pane ;
        private final Label valueLabel ;
        // locale-aware currency format to use for formatting
        private DecimalFormat format;
        
        public PriceTableCell() {
            // grab an instance
            format = (DecimalFormat) NumberFormat.getCurrencyInstance();
            //get the currency symbol
            String symbol = format.getCurrency().getSymbol();
            // replace the currency symbol with an empty string
            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            symbols.setCurrencySymbol("");
            format.setDecimalFormatSymbols(symbols);
            
            Label currencySignLabel = new Label(symbol);
            valueLabel = new Label();
            pane = new AnchorPane(currencySignLabel, valueLabel);
            AnchorPane.setLeftAnchor(currencySignLabel, 0.0);
            AnchorPane.setRightAnchor(valueLabel, 0.0);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(Long price, boolean empty) {
            super.updateItem(price, empty);
            if (empty) {
                setGraphic(null);
            } else {
                // manual formatting 
                //String text = String.format("%,d.%02d", price / 100, Math.abs(price % 100));
                valueLabel.setText(format.format(price));
                setGraphic(pane);
            }
        }
    }

    public static class Item {
        private final StringProperty name = new SimpleStringProperty();
        private final LongProperty price = new SimpleLongProperty();

        public Item(String name, long price) {
            setName(name);
            setPrice(price);
        }

        public StringProperty nameProperty() {
            return name ;
        }

        public final String getName() {
            return nameProperty().get();
        }

        public final void setName(String name) {
            nameProperty().set(name);
        }

        public LongProperty priceProperty() {
            return price ;
        }

        public final long getPrice() {
            return priceProperty().get();
        }

        public final void setPrice(long price) {
            priceProperty().set(price);
        }
    }

    @Override
    public void start(Stage primaryStage) {
//        DecimalFormat format =(DecimalFormat) NumberFormat.getCurrencyInstance();
//        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
//        symbols.setCurrencySymbol("");
//        format.setDecimalFormatSymbols(symbols);
//        String text = format.format(1000);
//        LOG.info("format: " + text);
//        Locale.setDefault(Locale.US);
        TableView<Item> table = new TableView<>();
        table.getColumns().add(column("Item", Item::nameProperty));
        TableColumn<Item, Long> priceColumn = column("Price", item -> item.priceProperty().asObject());
        priceColumn.setPrefWidth(300);

        priceColumn.setCellFactory(tc -> new PriceTableCell<>());

        table.getColumns().add(priceColumn);


        Random rng = new Random();
        for (int i = 1 ; i <= 20 ; i++) {
            table.getItems().add(new Item("Item "+i, rng.nextInt(1_000_000)));
        }

        Scene scene = new Scene(table);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private <S,T> TableColumn<S,T> column(String name, Function<S, ObservableValue<T>> property) {
        TableColumn<S,T> column = new TableColumn<>(name);
        column.setCellValueFactory(cellData -> property.apply(cellData.getValue()));
        return column ;
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewWithAccountingStyleCell.class.getName());
}

