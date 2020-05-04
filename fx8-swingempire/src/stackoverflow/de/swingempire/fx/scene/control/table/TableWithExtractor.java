/*
 * Created on 04.05.2020
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * From SO: https://stackoverflow.com/q/61583640/203657
 * 
 * weird behavior when not exposing the bound property - probably thinks it
 * can't change?
 */
public class TableWithExtractor extends Application {

    public void start(Stage primaryStage) {
        TableView<Bean> table = new TableView<>();
        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        TableColumn<Bean, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//        nameColumn.setOnEditCommit(e -> {
//            Bean bean = e.getRowValue();
//            bean.setName(e.getNewValue());
//        });

        TableColumn<Bean, Integer> quantityColumn = new TableColumn<>("quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                return object.toString();
            }

            @Override
            public Integer fromString(String string) {
                return Integer.parseInt(string);
            }
        }));
//        quantityColumn.setOnEditCommit(e -> {
//            Bean bean = e.getRowValue();
//            bean.setQuantity(e.getNewValue());
//        });

        TableColumn<Bean, Integer> priceColumn = new TableColumn<>("price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                return object.toString();
            }

            @Override
            public Integer fromString(String string) {
                return Integer.parseInt(string);
            }
        }));
//        quantityColumn.setOnEditCommit(e -> {
//            Bean bean = e.getRowValue();
//            bean.setQuantity(e.getNewValue());
//        });

        TableColumn<Bean, Integer> totalColumn = new TableColumn<>("total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        table.getColumns().addAll(nameColumn, priceColumn, quantityColumn, totalColumn);

        ObservableList<Bean> list = FXCollections.observableArrayList();
//                bean -> 
//            new Observable[]{bean.priceProperty(), bean.quantityProperty()});
        list.add(new Bean("Tomato", 20, 100));
        list.add(new Bean("Orange", 10, 200));

        table.setItems(list);

        BorderPane pane = new BorderPane();
        pane.setCenter(table);

        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }

    public class Bean {
        private SimpleStringProperty name;

        private SimpleIntegerProperty quantity;

        private SimpleIntegerProperty price;

        private SimpleIntegerProperty totalPrice;

        Bean(String name, Integer quantity, Integer price) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.price = new SimpleIntegerProperty(price);
            this.totalPrice = new SimpleIntegerProperty(0);
            this.totalPrice.bind(Bindings.createIntegerBinding(() -> {
                return getQuantity() * getPrice();
            }, quantityProperty(), priceProperty()));
            totalPrice.addListener(
                    (src, ov, nv) -> System.out.println(" changed: " + nv));
        }

        public String getName() {
            return this.name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public Integer getQuantity() {
            return this.quantity.get();
        }

        public void setQuantity(Integer quantity) {
            this.quantity.set(quantity);
        }

        public SimpleIntegerProperty quantityProperty() {
            return this.quantity;
        }

        public Integer getPrice() {
            return this.price.get();
        }

        public void setPrice(Integer price) {
            this.price.set(price);
        }

        public SimpleIntegerProperty priceProperty() {
            return this.price;
        }

        public Integer getTotalPrice() {
            return this.totalPrice.get();
        }

        public ReadOnlyIntegerProperty totalPriceProperty() {
            return this.totalPrice;
        }
//    public ReadOnlyProperty 
    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice.set(totalPrice);
    }
    }
}
