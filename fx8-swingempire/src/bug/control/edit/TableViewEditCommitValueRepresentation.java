/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import de.swingempire.fx.scene.control.cell.DebugTextFieldTableCell;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableViewCell: representation may be incorrect with custom commitHandler 
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8187314
 * 
 * with not writable example
 * 
 * Not yet reported:
 * another: starting edit with f2 throws npe in tableViewBehaviourBase 890 - no tableColumn?
 */
public class TableViewEditCommitValueRepresentation extends Application {

    TablePosition<TableColumn, String> editPosition;
    private String editValue;
    @Override
    public void start(Stage primaryStage) {
        TableView<TableColumn> table = new TableView<>(
                FXCollections.observableArrayList(new TableColumn("first"),
                        new TableColumn("second")));
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
//        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellFactory(DebugTextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        first.setOnEditStart(t -> editPosition = t.getTablePosition());
        first.setOnEditCommit(t -> {
            editValue = t.getNewValue();
            System.out.println("doing nothing");
            table.refresh();
            
        });

        table.getColumns().addAll(first);

        Button button = new Button("Check value");
        button.setOnAction(e -> {
            if (editPosition == null) return;
            String value = editPosition.getTableColumn().getCellObservableValue(editPosition.getRow()).getValue();
            System.out.println(
                    "value in edited cell must represent backing data: " + value + " not the edited " + editValue);
        });
        BorderPane root = new BorderPane(table);
        root.setBottom(button);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
