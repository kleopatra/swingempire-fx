/*
 * Created on 20.02.2018
 *
 */
package test.selection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8197991
 * selectAll cannot handle large amounts of data
 */
public class TableSelectAll extends Application {

    final ObservableList<SimpleObjectProperty<Integer>> listitems = FXCollections
            .observableArrayList();

    private static int ITEM_COUNT = 10000;

    public TableSelectAll() {
        for (int i = 0; i < ITEM_COUNT; ++i) {
            listitems.add(new SimpleObjectProperty<>(i));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        final TableView<SimpleObjectProperty<Integer>> lv = new TableView<>();
        final TableColumn<SimpleObjectProperty<Integer>, Integer> c = new TableColumn<>();
        c.setCellValueFactory(TableColumn.CellDataFeatures::getValue);
        lv.getColumns().add(c);
        lv.setItems(listitems);

        final HBox hbox = new HBox();
        hbox.getChildren().add(lv);
        primaryStage.setScene(new Scene(hbox));

        lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        primaryStage.show();
        Platform.runLater(() -> {
            long startMillis = System.currentTimeMillis();
            lv.getSelectionModel().selectAll();
            System.out.println("item count " + ITEM_COUNT + " took "
                    + (System.currentTimeMillis() - startMillis));
            System.exit(1);
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
