/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableView: NPE on F2 before first click
 * 
 * This might be related to the old incorrect state: 
 * https://bugs.openjdk.java.net/browse/JDK-8089652
 * 
 * it is, after first click, all is fine
 */
public class TableViewEditF2NPE extends Application {

    int counter;
    @Override
    public void start(Stage primaryStage) {
        TableView<TableColumn> table = new TableView<>(
                FXCollections.observableArrayList(new TableColumn("first"),
                        new TableColumn("second")));
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);

        Button add = new Button("addItem");
        add.setOnAction(e -> table.getItems().add(new TableColumn<>("added " + counter++)));
        BorderPane root = new BorderPane(table);
        root.setBottom(add);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
