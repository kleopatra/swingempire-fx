/*
 * Created on 20.04.2020
 *
 */
package test.selection;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8243127
 * too many notifications sent on removing selection
 * 
 * - select all
 * - select item 8
 * - expected: a single notification fired
 * - actual: two fired
 * 
 * useage error: it's not two changes but one change with two sub-changes
 */
public class TableViewSelectedIndicesNotification extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Selection Model Bug");
        var tableView = new TableView<Integer>();
        var identityColumn = new TableColumn<Integer, String>();
        identityColumn.setCellValueFactory(integer -> new SimpleStringProperty(integer.getValue().toString()));
        tableView.getColumns().add(identityColumn);
        tableView.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        int[] count = new int[] {0};
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Integer>) c -> {
//            FXUtils.prettyPrint(c);
            while (c.next()) {
                if (c.wasRemoved()) {
                    System.out.println(count[0]++ +". subchange: " + c.getRemoved());
                }
            }
            count[0] = 0;
        });
        primaryStage.setScene(new Scene(tableView, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
