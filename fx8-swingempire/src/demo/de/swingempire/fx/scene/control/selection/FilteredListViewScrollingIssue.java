/*
 * Created on 30.03.2016
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import de.swingempire.fx.scene.control.cell.XTextFieldTableCell;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Issue: selection not working on filtered/scrolled list
 * https://bugs.openjdk.java.net/browse/JDK-8152665
 * 
 * For comparison using a ListView - behaves as expected.
 * 
 * @see FilteredTableViewScrollingIssue
 */
public class FilteredListViewScrollingIssue extends Application {
    private static final boolean CLEAR_SELECTION_ENABLED = false;

    private ListView<Item> listView;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Predicate<Item> acceptPredicate = item -> true;
        final Predicate<Item> value2Predicate = item -> item.integerProperty().getValue() == 20;

//        tableView = new XTableView<>();
        listView = new ListView<>();
        
        final TableColumn<Item, Integer> column = new TableColumn<>("Value");
        column.setCellValueFactory(param -> param.getValue().integerProperty());
        column.setCellFactory(cc -> new XTextFieldTableCell<>());
//        tableView.getColumns().add(column);

        final ObservableList<Item> items = FXCollections.observableArrayList();
        IntStream.range(0, 21)
            .forEach(value -> items.add(new Item(value)));
//        items.add(new Item(2));

        final FilteredList<Item> filteredItems = items.filtered(acceptPredicate);
        filteredItems.addListener((ListChangeListener) (c -> {
            System.out.println("in listener: " + c);
        })); 
//        new FXUtils.PrintingListChangeListener("filteredList ", filteredItems);
        listView.setItems(filteredItems);
        // sorting not needed
//        final SortedList<Item> sortedFilteredItems = filteredItems.sorted();
//        sortedFilteredItems.comparatorProperty().bind(tableView.comparatorProperty());
//        tableView.setItems(sortedFilteredItems);


        final Button setFilterButton = new Button("Set filter (value = 20)");
        setFilterButton.setOnAction(e -> {
            processSelection();
            filteredItems.setPredicate(value2Predicate);
        });

        final Button clearFilterButton = new Button("Clear filter");
        clearFilterButton.setOnAction(e -> {
            processSelection();
            filteredItems.setPredicate(acceptPredicate);
        });

        final HBox buttonBox = new HBox(5, setFilterButton, clearFilterButton);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        final BorderPane borderPane = new BorderPane();
        borderPane.setCenter(listView);
        borderPane.setBottom(buttonBox);

        primaryStage.setTitle(FXUtils.version() + " Table View Filtering & Scrolling Issue");
        primaryStage.setScene(new Scene(borderPane, 600, 400));
        primaryStage.setResizable(false); // resizing must be prevented, because it affects the number of allocated rows in the table view
        primaryStage.show();
    }

    private void processSelection() {
        if (CLEAR_SELECTION_ENABLED) {
            listView.getSelectionModel().clearSelection();
        }
    }

    private static class Item {
        private final SimpleObjectProperty<Integer> integerProperty;

        Item(final int value) {
            integerProperty = new SimpleObjectProperty<>(value);
        }

        ObservableValue<Integer> integerProperty() {
            return integerProperty;
        }

        @Override
        public String toString() {
            return String.valueOf(integerProperty.get());
        }
        
        
    }

    public static void main(String[] args) {
        launch();
    }
}
