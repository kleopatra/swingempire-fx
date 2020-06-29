/*
 * Created on 29.06.2020
 *
 */
package de.swingempire.fx.fxml.loader;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/q/44321738/203657
 * 
 * loading data into a combo via fxml.
 */
public class ComboLoader {

    private ObservableList<Item> obsItems;

    public ComboLoader() {

        obsItems = FXCollections.observableArrayList(createItems());
    }

    private List<Item> createItems() {
            return IntStream.rangeClosed(0, 5)
                    .mapToObj(i -> "Item "+i)
                    .map(Item::new)
                    .collect(Collectors.toList());
    }
    //name of this methods corresponds to itemLoader.items in fxml.
    //if xml name was itemLoader.a this method should have been
    //getA(). A bit odd 
    public ObservableList<Item> getItems(){

        return obsItems;
    }
//
//    /**
//     * view-related config doesn't belong here, just a quick check
//     * @return
//     */
//    public Callback<ListView<Item>, ListCell<Item>> getCellFactory() {
//        return cc -> new ComboLoaderCellFactory.CListCell<Item>(Item::getName); //item -> item.getName());
//    }
//    
    
    public static class Item {

        private final StringProperty name = new SimpleStringProperty();

        public Item(String name) {
            this.name.set(name);
        }

        public final StringProperty nameProperty() {
            return name;
        }

        public String getName() {
            return nameProperty().get();
        }
//        @Override
//        public String toString() {
//            return name.getValue();
//        }
    }
}