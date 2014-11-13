/*
 * Created on 13.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-37360
 * 
 * Reselect a single item after having had multiple selections
 * fires both remove and add for the same item.
 * 
 * 1. Run the below example.
 * 2. CTRL select the second item in the table so both items are selected.
 * 3. Select _just_ the second item in the table.
 * 
 * Additional:
 * ListView: fires added if item already selected (table doesn't) 
 * 
 * <hr>
 * 
 * fixed as of: http://hg.openjdk.java.net/openjfx/8u-dev/rt/rev/a3cd2ce0ca3c
 * has changes to both MultipleSelectionModelBase and XXSelectionModels 
 * (XX = TableView, ListView, TreeTableView ...) - not ListView? but test
 * seems to pass
 * 
 * additional fire not fixed
 */
public class MultiToSingleTableItemSelection extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // table
        final TableView<Item> table = new TableView<>();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // table columns
        final TableColumn<Item, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        final TableColumn<Item, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().addAll(nameColumn, descriptionColumn);

        // selection change handling
        table.getSelectionModel().getSelectedItems().addListener(this::processListChange);

//        ListView<Item> list = new ListView<>();
        ListViewAnchored<Item> list = new ListViewAnchored<>();
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.getSelectionModel().getSelectedItems().addListener(this::processListChange);
        // table items
        final Item one = new Item();
        one.name.set("name one");
        one.description.set("description one");
        final Item two = new Item();
        two.name.set("name two");
        two.description.set("description two");
        table.getItems().addAll(one, two);
        list.getItems().addAll(table.getItems());
        
        Button selectRangeList = new Button("selectRangeList");
        selectRangeList.setOnAction(e -> {
            list.getSelectionModel().selectRange(0, list.getItems().size());
        });
        Button selectLastList = new Button("selectLastList");
        selectLastList.setOnAction(e -> {
            int last = table.getItems().size() - 1;
            list.getSelectionModel().select(last);
        });
        Button clearSelectLastList = new Button("clearSelectLastList");
        clearSelectLastList.setOnAction(e -> {
            int last = table.getItems().size() - 1;
            list.getSelectionModel().clearAndSelect(last);
        });
        Button selectRangeTable = new Button("selectRangeTable");
        selectRangeTable.setOnAction(e -> {
            table.getSelectionModel().selectRange(0, table.getItems().size());
        });
        Button selectLastTable = new Button("selectLastTable");
        selectLastTable.setOnAction(e -> {
            int last = table.getItems().size() - 1;
            table.getSelectionModel().select(last);
        });
        
        Button clearSelectLastTable = new Button("clearselectLastTable");
        clearSelectLastTable.setOnAction(e -> {
            int last = table.getItems().size() - 1;
            table.getSelectionModel().clearAndSelect(last);
        });
        
        // os / version labels
        Label osLabel = new Label(System.getProperty("os.name"));
        Label jvmLabel = new Label(
                System.getProperty("java.version") +
                        "-" + System.getProperty("java.vm.version") +
                        " (" + System.getProperty("os.arch") + ")"
        );

        // scene / stage
        primaryStage.setScene(new Scene(
                new BorderPane(
                        new HBox(table, list),
                        new HBox(selectRangeTable, selectLastTable, clearSelectLastTable, 
                                selectRangeList, selectLastList, clearSelectLastList),
                        null,
                        new VBox(osLabel, jvmLabel),
                        null
                )
        ));

//        primaryStage.setWidth(600);
        primaryStage.setHeight(200);
        primaryStage.setTitle("Table Item Reselection");
        primaryStage.show();
    }

    int eventCount;
    private void processListChange(ListChangeListener.Change<? extends Item> c) {
        System.out.println("Change event... " + eventCount++);
        int changeCount = 0;
        while (c.next()) {
            System.out.println(" Change: " + changeCount++);

            if (c.wasRemoved()) {
                c.getRemoved().forEach(item ->
                        System.out.println(" Removed: " + item.getName()));
            }
            if (c.wasAdded()) {
                c.getAddedSubList().forEach(item ->
                        System.out.println(" Added: " + item.getName()));
            }
        }
    }

    public static class Item {
        private final StringProperty name = new SimpleStringProperty("name");
        public final ReadOnlyStringProperty nameProperty() {return name;}
        public final String getName() {return name.get();}

        private final StringProperty description = new SimpleStringProperty("description");
        public final ReadOnlyStringProperty descriptionProperty() {return description;}
        public final String getDescription() {return description.get();}
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
