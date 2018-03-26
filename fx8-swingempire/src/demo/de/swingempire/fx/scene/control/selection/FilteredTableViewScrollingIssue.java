/*
 * Created on 30.03.2016
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.function.Predicate;
import java.util.logging.Logger;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Issue: selection not working on filtered/scrolled list
 * https://bugs.openjdk.java.net/browse/JDK-8152665
 * <p>
 * The technical (mentioned in the report) reason is that
 * the cell's index is not updated on filtering (aka: change
 * to list content). 
 * <p>
 * Weird is the different behavior when scrolling by mouse from
 * scrolling via scrollbar - couldn't find the update routine
 * when clicking the bar. The one triggered by mouse-wheel seems
 * to re-configure the cells correctly. 
 * <p>
 * 
 * Problem occurs when scrolling with mouse-wheel, and page-down (also
 * with modifiers shift and ctrl). It does not occur when scrolling
 * by clicking on vBar nor with end/down keys. On error, cell and
 * row index are out of sync: row is correctly updated to 0, while
 * cell is still on the (old before filtering) 20.
 * 
 * Happens only if last row (second last is fine)
 * 
 * Jonathan found the underlying reason: it's handling of odd/even
 * rows in virtualFlow.getAccumCell, which tries its best to
 * re-use a cell with the same eveness for the sake of css. 
 * See his comment in the report.
 * 
 * ------------ below descriptions from bug report
 * <p>
 * 1. create a table with many items, more than the number of visible rows 
 * (the table must have a filterable list of items)
 * 2. scroll with the mouse wheel until the end of the table view 
 * (not with the scroll bar!)
 * 3. select the last row
 * 4. filter the items so that only the item corresponding 
 * to the selected row remains
 * 5. try to select (if not selected), or deselect 
 * (if selected) the remaining row, by clicking table any table cell 
 * (not the empty part of the row!)
 * 
 * Note: 
 * My sample program has a flag that allows the rows to be deselected 
 * before filtering is applied. In this case, after filtering there will 
 * be no selection, and if the user tries to select the remaining row, 
 * it will be impossible. Alternatively, if the flag is set to false, 
 * the selected row is still selected after filtering, but Ctrl+Click in 
 * order to clear selection will not work.
 */
public class FilteredTableViewScrollingIssue extends Application {
    private static final boolean CLEAR_SELECTION_ENABLED = false;

    private TableView<ItemT> tableView;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final ObservableList<ItemT> items = FXCollections.observableArrayList();
        int f = -1;
        final Predicate<ItemT> acceptPredicate = item -> true;
        final Predicate<ItemT> value2Predicate = item ->
            item.integerProperty().getValue() == items.size() -1;

//        tableView = new XTableView<>();
        tableView = new TableView<>();
//        tableView.setRowFactory(cc -> {
//            return new TableRow<ItemT>() {
//                @Override
//                protected Skin<?> createDefaultSkin() {
//                    return new IndexUpdatingTableRowSkin<>(this);
//                }
//            };
//        });
        final TableColumn<ItemT, Integer> column = new TableColumn<>("Value");
        column.setCellValueFactory(param -> param.getValue().integerProperty());
        column.setCellFactory(cc -> new XTextFieldTableCell<>());
        tableView.getColumns().add(column);

        IntStream.range(0, 6)
            .forEach(value -> items.add(new ItemT(value)));
//        items.add(new ItemT(f));

        final FilteredList<ItemT> filteredItems = items.filtered(acceptPredicate);
        filteredItems.addListener((ListChangeListener) (c -> {
            System.out.println("in listener: " + c);
        })); 
//        new FXUtils.PrintingListChangeListener("filteredList ", filteredItems);
        tableView.setItems(filteredItems);
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
        borderPane.setCenter(tableView);
        borderPane.setBottom(buttonBox);

        primaryStage.setTitle(FXUtils.version() + " Table View Filtering & Scrolling Issue");
        primaryStage.setScene(new Scene(borderPane, 600, 170));
        primaryStage.setResizable(false); // resizing must be prevented, because it affects the number of allocated rows in the table view
        primaryStage.show();
    }

    private void processSelection() {
        if (CLEAR_SELECTION_ENABLED) {
            tableView.getSelectionModel().clearSelection();
        }
    }

    private static class ItemT {
        private final SimpleObjectProperty<Integer> integerProperty;

        ItemT(final int value) {
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
    @SuppressWarnings("unused")
    static final Logger LOG = Logger
            .getLogger(FilteredTableViewScrollingIssue.class.getName());
}
