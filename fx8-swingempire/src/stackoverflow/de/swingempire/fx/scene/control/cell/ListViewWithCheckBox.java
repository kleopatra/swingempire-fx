/*
 * Created on 06.04.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * answer to https://stackoverflow.com/q/55537578/203657
 * add checkbox to listView
 * 
 * from zephyr, re-created the checkbox in every call to updateItem
 * and bidi-binding to selected
 * 
 * working as was (bidibinding is weak!), but violating the re-use
 * paradigma: if re-using the checkbox as well, the binding has be 
 * re-wired every time the index changes (otherwise will update
 * old items as well)
 * 
 */
public class ListViewWithCheckBox extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Simple interface
        VBox root = new VBox(5);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        // Our ListView
        ListView<Item> itemListView = new ListView<>();

        // Fill our list with sample Items
        for (int i = 0; i < 100; i++) {
            itemListView.getItems().addAll(new Item("Item #" + i));
        }

        // Now, we'll setup a custom CellFactory so we can display checkboxes instead of just a String
        itemListView.setCellFactory(lv -> new ListCell<Item>() {
            CheckBox checkBox = new CheckBox();
            @Override
            public void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    // Do not fill a cell if no object is listed in it
                    setGraphic(null);
                } else {
                    // Create a checkbox and use that for our graphic
//                    final CheckBox checkBox = new CheckBox(item.getName());
                    checkBox.setText(item.getName());
                    // Bind the selectedProperty of the CheckBox to our Item's activeProperty
                    checkBox.selectedProperty().bindBidirectional(item.activeProperty());
                    setGraphic(checkBox);
                }
            }
        });

        // Button to confirm values are being changed
        Button button = new Button("Print Active Statuses");
        button.setOnAction(event -> {
            for (Item item :
                    itemListView.getItems()) {
                System.out.println(item.getName() + " : " + item.isActive());
            }
        });

        // Now, add the ListView to our scene
        root.getChildren().addAll(itemListView, button);

        // Show the Stage
        primaryStage.setWidth(300);
        primaryStage.setHeight(300);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


// Sample class to hold a name and boolean value (checked/unchecked)
class Item {
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty();

    public Item(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }
}}