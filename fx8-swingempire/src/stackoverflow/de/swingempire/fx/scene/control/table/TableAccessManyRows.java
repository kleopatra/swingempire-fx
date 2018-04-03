/*
 * Created on 02.04.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/49608261/203657
 * assumed performance issue on add
 * 
 * can't reproduce
 * 
 * ----
 * 
 * check styling (answer by fabian) - working 
 * requirement: no odd background in free space of row
 * https://stackoverflow.com/a/49628699/203657
 */
public class TableAccessManyRows extends Application {

    private TableView<NodeInfo> table = new TableView<NodeInfo>();
    private final ObservableList<NodeInfo> data = FXCollections.observableArrayList();
    private static int index = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        scene.getStylesheets().add(getClass().getResource("customtablerow.css").toExternalForm());
        stage.setWidth(450);
        stage.setHeight(500);

        TableColumn<NodeInfo, String> firstNameCol = new TableColumn("First Name");
//        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<NodeInfo, String>("firstName"));

        TableColumn lastNameCol = new TableColumn("Last Name");
//        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<NodeInfo, String>("lastName"));

        for (int i = 0; i < 5; i++) {
            data.add(new NodeInfo("first Name" + index, "last Name" + index++));
        }

        table.setItems(data);
        table.getColumns().addAll(firstNameCol, lastNameCol);

        Button btn = new Button("add new item");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("-------------add a new row: " + data.size());
                data.add(new NodeInfo("first Name" + index, "last Name" + index++));
            }
        });

        Button scroll = new Button("scroll to middle");
        scroll.setOnAction(e -> {
            
            System.out.println("-------------scrolled to a new row: " + data.size() / 2);
            table.scrollTo(data.size() / 2);
        });
        final VBox vbox = new VBox(20);
        vbox.getChildren().addAll(table, btn, scroll);
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        stage.setScene(scene);
        stage.show();
    }

    public static class NodeInfo {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;

        private NodeInfo(String fName, String lastName) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lastName);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String fName) {
            firstName.set(fName);
        }

        public String getLastName() {
            System.out.println(lastName.get());
            return lastName.get();
        }

        public void setLastName(String lName) {
            lastName.set(lName);
        }
    }

}

