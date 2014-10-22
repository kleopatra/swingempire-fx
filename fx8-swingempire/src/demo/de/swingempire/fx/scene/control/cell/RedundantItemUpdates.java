/*
 * Created on 21.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.ListViewSkin;


/**
 * https://javafx-jira.kenai.com/browse/RT-35395
 * redundant calls to updateItem (from listener to itemsProperty?)
 * 
 * Other Issue (seen during testing):
 * ListCells are listening to itemsProperty and register
 * ListChangeListeners if changed. Those aren't
 * updated when replacing with an equals list.
 * Trying to find whether or not it matters if the listener is updated.
 * 
 * Any macroscopic changes?
 * 
 */
public class RedundantItemUpdates extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    int idCount;
    @Override
    public void start(Stage stage) {
        ListViewSkin s;
        ObservableList<String> items = createItems(null);
        ListView<String> listView = new ListView<>(items);
       
        listView.setCellFactory(lv -> {
            System.out.println("created");
            ListCell cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    // note: this cell is misbehaing in not calling super!
                    // it's the original from bug report? Check!
                    setText(item);
                    if (!empty) {
                        System.out.println("item updated to '" + item + "'"  + "for " + getId());
                    }
                }
            };
            cell.setId("cell-id: " + idCount++);
            return cell;
         });

        Button update = new Button("Update");
        update.setOnAction(e -> {
            Object old = listView.getItems().get(0);
            listView.getItems().set(0, old + "X");
            
        });
        Button reset = new Button("Reset items");
        reset.setOnAction(e -> {
            listView.setItems(createItems(listView));
        });
        Button add = new Button("Add");
        add.setOnAction(e -> {
            listView.getItems().add(1, "other foo " + count++);
        });
        Parent content = new VBox(listView, reset, update, add);
        stage.setScene(new Scene(content, 200, 180));
        stage.setTitle(System.getProperty("java.version"));
        stage.show();

    }

    int count;
    protected ObservableList<String> createItems(ListView lv) {
        ObservableList<String> items = lv != null ? FXCollections.observableArrayList(lv.getItems())
                : FXCollections.observableArrayList("Foo");
        return items;
    }
}
