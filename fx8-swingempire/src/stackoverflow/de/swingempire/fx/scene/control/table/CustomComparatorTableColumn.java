/*
 * Created on 25.09.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static javafx.scene.control.TableColumn.SortType.*;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/50109815/203657
 * custom sort, keeping sorted by one column first, irrespective of which
 * is clicked
 * 
 * answer by sai dandem (working!)
 */
public class CustomComparatorTableColumn extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<Device> devices = FXCollections.observableArrayList();
        devices.add(new Device(1, "Apple", "Zebra", TagType.TAG));
        devices.add(new Device(2, "BlackBerry", "Parrot", TagType.TAG));
        devices.add(new Device(3, "Amazon", "Yak", TagType.NO_TAG));
        devices.add(new Device(4, "Oppo", "Penguin", TagType.NO_TAG));

        TableView<Device> tableView = new TableView<>();
        TableColumn<Device, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());

        TableColumn<Device, String> displayCol = new TableColumn<>("Display");
        displayCol.setCellValueFactory(param -> param.getValue().displayProperty());

        TableColumn<Device, TagType> tagCol = new TableColumn<>("Tag");
        tagCol.setCellValueFactory(param -> param.getValue().tagTypeProperty());

        tableView.getColumns().addAll(nameCol, displayCol, tagCol);
        tableView.setItems(devices);

        tableView.setSortPolicy(tv -> {
            final ObservableList<Device> itemsList = tableView.getItems();
            if (itemsList == null || itemsList.isEmpty()) {
                return true;
            }
            final List<TableColumn<Device, ?>> sortOrder = new ArrayList<>(tableView.getSortOrder());
            if (!sortOrder.isEmpty()) {
                // If there is no Tag column in the sort order, always adding as the first sort to consider.
                if (!sortOrder.stream().anyMatch(tc -> tc.getText().equals("Tag"))) {
                    sortOrder.add(0, tagCol);
                }
                FXCollections.sort(itemsList, new TableColumnComparator<>(sortOrder));
            }
            return true;
        });

        Scene sc = new Scene(tableView);
        primaryStage.setScene(sc);
        primaryStage.show();

    }

    class TableColumnComparator<S> implements Comparator<S> {
        private final List<TableColumn<S, ?>> allColumns;

        public TableColumnComparator(final List<TableColumn<S, ?>> allColumns) {
            this.allColumns = allColumns;
        }

        @Override
        public int compare(final S o1, final S o2) {
            for (final TableColumn<S, ?> tc : allColumns) {
                if (!isSortable(tc)) {
                    continue;
                }
                final Object value1 = tc.getCellData(o1);
                final Object value2 = tc.getCellData(o2);

                @SuppressWarnings("unchecked") final Comparator<Object> c = (Comparator<Object>) tc.getComparator();
                final int result = ASCENDING.equals(tc.getSortType()) ? c.compare(value1, value2)
                        : c.compare(value2, value1);

                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        private boolean isSortable(final TableColumn<S, ?> tc) {
            return tc.getSortType() != null && tc.isSortable();
        }
    }

    class Device {
        IntegerProperty id = new SimpleIntegerProperty();
        StringProperty name = new SimpleStringProperty();
        StringProperty display = new SimpleStringProperty();
        ObjectProperty<TagType> tagType = new SimpleObjectProperty<>();

        public Device(int id, String n, String d, TagType tag) {
            this.id.set(id);
            name.set(n);
            tagType.set(tag);
            display.set(d);
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

        public ObjectProperty<TagType> tagTypeProperty() {
            return tagType;
        }

        public StringProperty displayProperty() {
            return display;
        }

    }

    enum TagType {
        TAG, NO_TAG;
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

