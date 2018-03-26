/*
 * Created on 16.02.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48793130/203657
 * disable auto-size on showing columns: make pref != 80
 * 
 * unrelated:
 * - initial sizing doesn't work (expected: auto-size)
 * - after hiding any column, header can't be resized by mouse immediately (resize
 * cursor not showing) have to left-click first anywhere on any column
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnSizing extends Application {

    private TableView table;

    @Override
    public void start(Stage primaryStage) {
        //Create table
        table = new TableView();

        //Create table items
        final ObservableList<DataContainer> data = FXCollections.observableArrayList(
            new DataContainer("Alice", "Red", "Yellow"),
            new DataContainer("Bob", "purple", "green"),
            new DataContainer("Sir Jean-Jaques-Charles III", "Deep green-cyan turqoise", "Fluorescent pink")
        );

        //Create table columns
        TableColumn nameColumn = new TableColumn("name");   
        TableColumn colorsColumn = new TableColumn("colors");
        TableColumn color1Column = new TableColumn("color1");
        TableColumn color2Column = new TableColumn("color2");

        //Make it so columns can properly display the table items
        nameColumn.setCellValueFactory( new PropertyValueFactory<>("name"));
        color1Column.setCellValueFactory( new PropertyValueFactory<>("color1"));
        color2Column.setCellValueFactory( new PropertyValueFactory<>("color2"));

        //Set the header context menus
        addHeaderContextMenus(nameColumn);
        addHeaderContextMenus(colorsColumn);
        addHeaderContextMenus(color1Column);
        addHeaderContextMenus(color2Column);

        //Add the columns to the table
        table.getColumns().addAll(nameColumn, colorsColumn);
        colorsColumn.getColumns().addAll(color1Column, color2Column);

        //Put the table inside a container
        StackPane root = new StackPane();
        AnchorPane anchor = new AnchorPane();
        root.getChildren().add(anchor);
        anchor.getChildren().add(table);

        //anchor the table to the AnchorPane
        AnchorPane.setTopAnchor(table, 1.0);
        AnchorPane.setBottomAnchor(table, 1.0);
        AnchorPane.setLeftAnchor(table, 1.0);
        AnchorPane.setRightAnchor(table, 1.0);

        //show the stage
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(new Scene(root, 300, 300));
        primaryStage.show();

        //Add the data to the table (after everything is shown)
        table.setItems(data);
        
        // quick check for shift-down (reported for ListView)
        // working - so look for what's special in ListView
//        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        table.getSelectionModel().getSelectedItems().addListener((ListChangeListener) ch -> {
//            while (ch.next()) {
//                if (ch.wasAdded()) {
//                    System.out.println("+" + ch.getAddedSubList());
//                }
//            }
//        });
        
        

    }

    //Start the application
    public static void main(String[] args) {
        launch(args);
    }

    //Add context menus to the headers.
    private void addHeaderContextMenus(TableColumn column) {

        //create the menu and attach the items
        ContextMenu menu = new ContextMenu();
        MenuItem hideItem = new MenuItem("hide");
        MenuItem unhideAllItem = new MenuItem("unhide all");
        menu.getItems().addAll(hideItem, unhideAllItem);

        //Set behavior for hide/unhide
        hideItem.setOnAction((event) -> {
            hideColumn(column);
        });      
        unhideAllItem.setOnAction((event) -> {
            unhideAllColumns();
        });

        //attach the menu to the column (header)
        column.setContextMenu(menu);
    }

    private void hideColumn(TableColumn column) {
        double width = column.getWidth();
        if (column.getPrefWidth() == 80) {
            if (width != 80) {
                column.setPrefWidth(width);
            } else {
                column.setPrefWidth(81);
            }
        }
//        column.setMaxWidth(column.getWidth());
//        column.setPrefWidth(column.getWidth());
        column.setVisible(false);
        logWidths("hiding ", column, width);
    }

    private void unhideAllColumns() {
        table.getColumns().forEach(column -> {
            unhideColumnsRecursively((TableColumn)column);
        });
    }

    private void unhideColumnsRecursively(TableColumn column) {
        column.getColumns().forEach(childColumn -> {
            unhideColumnsRecursively((TableColumn)childColumn);
        });
        if(column.visibleProperty().get() == false) {
            double width = column.getWidth();
            column.setVisible(true);
//            column.setMaxWidth(9999999); //arbitrarily large big number
//            column.setPrefWidth(width);
            logWidths("showing ", column, width);
        }
    }

    /**
     * @param column
     * @param width
     */
    protected void logWidths(String msg, TableColumn column, double width) {
        System.out.println(msg + column.getText() + " width before: " + width);
        System.out.println("current prefwidth: " + column.getPrefWidth());
        System.out.println("current actual width: " + column.getWidth());
    }
    
    public class DataContainer {

        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty color1 = new SimpleStringProperty();
        private final SimpleStringProperty color2 = new SimpleStringProperty();

        public DataContainer(String name, String color1, String color2) {
            this.name.set(name);
            this.color1.set(color1);
            this.color2.set(color2);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getColor1() {
            return color1.get();
        }

        public void setColor1(String color1) {
            this.color1.set(color1);
        }

        public String getColor2() {
            return color2.get();
        }

        public void setColor2(String color2) {
            this.color2.set(color2);
        }
    }


}

