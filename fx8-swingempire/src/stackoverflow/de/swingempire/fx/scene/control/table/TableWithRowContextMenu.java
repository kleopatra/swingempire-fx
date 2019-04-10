/*
 * Created on 10.04.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55610269/203657
 * Problem with row-contextMenu: does not show up on very first click
 * 
 * was user error: new contextMenu created on each request
 */
public class TableWithRowContextMenu extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane myBorderPane = new BorderPane();
        TableView<People> myTable = new TableView<>();

        TableColumn<People, String> nameColumn = new TableColumn<>();
        TableColumn<People, Integer> ageColumn = new TableColumn<>();

        ObservableList<People> peopleList = FXCollections.observableArrayList();
        peopleList.addAll(new People("John Doe", 23), new People("Dummy", 110));

        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));

        ageColumn.setMinWidth(100);
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("Age"));

        myTable.setItems(peopleList);
        myTable.getColumns().addAll(nameColumn, ageColumn);

        myTable.setRowFactory(tv -> {
            TableRow<People> row = new TableRow<>() {
              ContextMenu rowMenu = new ContextMenu();
              MenuItem sampleMenuItem = new MenuItem("Sample Button");
              {
                  rowMenu.getItems().addAll(sampleMenuItem);
                  contextMenuProperty()
                      .bind(Bindings
                          .when(Bindings.isNotNull(itemProperty()))
                          .then(rowMenu).otherwise((ContextMenu) null));
                  rowMenu.setOnShowing(e -> {
                      People selectedRow = getItem();
                      sampleMenuItem.setDisable(selectedRow.getAge() > 100);
                  });
              }
               
            };
            return row;
        }); 
//        myTable.setRowFactory(tv -> {
//            TableRow<People> row = new TableRow<>() {
//                
//            };
//
//            row.setOnContextMenuRequested((event) -> {
//                People selectedRow = row.getItem();
//
//                ContextMenu rowMenu = new ContextMenu();
//
//                MenuItem sampleMenuItem = new MenuItem("Sample Button");
//                if (selectedRow.getAge() > 100) {
//                    sampleMenuItem.setDisable(true);
//                }
//
//                rowMenu.getItems().add(sampleMenuItem);
//                /*
//                 * if (row.getItem() != null) { // this block comment displays
//                 * the context menu instantly rowMenu.show(row,
//                 * event.getScreenX(), event.getScreenY()); } else { // do
//                 * nothing }
//                 */
//
//                // this requires the row to be right clicked 2 times before
//                // displaying the context menu
//                row.contextMenuProperty()
//                        .bind(Bindings
//                                .when(Bindings.isNotNull(row.itemProperty()))
//                                .then(rowMenu).otherwise((ContextMenu) null));
//            });
//
//            return row;
//        });

        myBorderPane.setCenter(myTable);

        Scene scene = new Scene(myBorderPane, 500, 500);
        primaryStage.setTitle("MCVE");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main (String[] args) {
        launch(args);
    }

    public class People {
        SimpleStringProperty name;
        SimpleIntegerProperty age;
        public People(String name, int age) {
            this.name = new SimpleStringProperty(name);
            this.age = new SimpleIntegerProperty(age);
        }

        public SimpleStringProperty NameProperty() {
            return this.name;
        }
        public SimpleIntegerProperty AgeProperty() {
            return this.age;
        }
        public String getName() {
            return this.name.get();
        }
        public int getAge() {
            return this.age.get();
        }

        }
}
