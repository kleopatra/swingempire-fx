/*
 * Created on 22.10.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboInTable extends Application {

    // List of items
    private static ObservableList<Item> listOfItems = FXCollections.observableArrayList();

    // List of available Colors. These will be selectable from the ComboBox
    private static ObservableList<String> availableColors = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    private static void buildSampleData() {

        availableColors.addAll("Red", "Blue", "Green", "Yellow", "Black");
    }

    @Override
    public void start(Stage primaryStage) {

        // Simple Interface
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        // Build a list of sample data. This data is loaded from my data model and passed to the constructor
        // of this editor in my real application.
        listOfItems.addAll(
                new Item("One", "Black"),
                new Item("Two", "Black"),
                new Item("Three", null),
                new Item("Four", "Green"),
                new Item("Five", "Red")
        );

        // TableView to display the list of items
        TableView<Item> tableView = new TableView<>();

        // Create the TableColumn
        TableColumn<Item, String> colName = new TableColumn<>("Name");
        TableColumn<Item, String> colColor = new TableColumn<>("Color");

        // Cell Property Factories
        colName.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getItemName()));
        colColor.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getItemColor()));

        // Add ComboBox to the Color column, populated with the list of availableColors
        colColor.setCellFactory(tc -> {
            ComboBox<String> comboBox = new ComboBox<>(availableColors);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            TableCell<Item, String> cell = new TableCell<Item, String>() {
                @Override
                protected void updateItem(String color, boolean empty) {
                    super.updateItem(color, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(comboBox);
                        comboBox.setValue(color);
                    }
                }
            };

            // Set the action of the ComboBox to set the right Value to the ValuePair
            comboBox.setOnAction(event -> {
                listOfItems.get(cell.getIndex()).setItemColor(comboBox.getValue());
            });

            return cell;
        });

        // Add the column to the TableView
        tableView.getColumns().addAll(colName, colColor);
        tableView.setItems(listOfItems);

        // Add button to load the data
        Button btnLoadData = new Button("Load Available Colors");
        btnLoadData.setOnAction(event -> {
            buildSampleData();
        });
        root.getChildren().add(btnLoadData);

        // Add the TableView to the root layout
        root.getChildren().add(tableView);

        Button btnPrintAll = new Button("Print All");
        btnPrintAll.setOnAction(event -> {
            for (Item item : listOfItems) {
                System.out.println(item.getItemName() + " : " + item.getItemColor());
            }
        });
        root.getChildren().add(btnPrintAll);

        // Show the stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Sample");
        primaryStage.show();
    }
    
    public class Item {

        private StringProperty itemName = new SimpleStringProperty();
        private StringProperty itemColor = new SimpleStringProperty();

        public Item(String name, String color) {
            this.itemName.set(name);
            this.itemColor.set(color);
        }

        public String getItemName() {
            return itemName.get();
        }

        public void setItemName(String itemName) {
            this.itemName.set(itemName);
        }

        public StringProperty itemNameProperty() {
            return itemName;
        }

        public String getItemColor() {
            return itemColor.get();
        }

        public void setItemColor(String itemColor) {
            this.itemColor.set(itemColor);
        }

        public StringProperty itemColorProperty() {
            return itemColor;
        }
    }


}
