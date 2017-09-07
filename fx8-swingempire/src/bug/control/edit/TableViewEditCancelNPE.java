/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableView: NPE on access of event state on cancel
 * 
 * reported as
 * https://bugs.openjdk.java.net/browse/JDK-8187229
 */
public class TableViewEditCancelNPE extends Application {

    TablePosition<TableColumn, String> editPosition;
    @Override
    public void start(Stage primaryStage) {
        TableView<TableColumn> table = new TableView<>(
                FXCollections.observableArrayList(new TableColumn("first"),
                        new TableColumn("second")));
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        first.setOnEditStart(t -> editPosition = t.getTablePosition());
        first.setOnEditCancel(t -> {
            if (!editPosition.equals(t.getTablePosition())) {
                System.out.println("expected " + editPosition + " actual " + t.getTablePosition());
                System.out.println("Desaster: NPE on access of event state"); 
                t.getRowValue();
            }
            
        });
        

        table.getColumns().addAll(first);

        BorderPane root = new BorderPane(table);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
