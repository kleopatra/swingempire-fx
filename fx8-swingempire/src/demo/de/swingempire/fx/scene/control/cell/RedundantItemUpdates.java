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
        ObservableList<String> items = createItems(null);
        ListView<String> listView = new ListView<>(items);
       
        listView.setCellFactory(lv -> {
            System.out.println("created");
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    if (!empty) {
                        System.out.println("item updated to '" + item + "'"  + "for " + getId());
//                        new RuntimeException("who's calling? \n").printStackTrace();
                    }
                }
            };
            cell.setId("cell-id: " + idCount++);
            return cell;
         });

        Button update = new Button("Update Item at 0");
        update.setOnAction(e -> {
            Object old = listView.getItems().get(0);
            listView.getItems().set(0, old + "X");
            
        });
        Button reset = new Button("Reset items to equal list");
        reset.setOnAction(e -> {
            previous = listView.getItems();
            listView.setItems(createItems(listView));
        });
        
        Button updateOld = new Button("update old item at 0");
        updateOld.setOnAction(e -> {
            if (previous == null || previous == listView.getItems()) {
                System.out.println("same items");
                return;
            }
            previous.set(0, "changed old");
        });
        Button add = new Button("Add");
        add.setOnAction(e -> {
            listView.getItems().add(1, "other foo " + count++);
        });
        Parent content = new VBox(listView, reset, updateOld, update, add);
        stage.setScene(new Scene(content, 200, 180));
        stage.setTitle(System.getProperty("java.version"));
        stage.show();

    }

    int count;
    private ObservableList<String> previous;
    /**
     * Creates and returns a list the is equal to but not the same as 
     * the current items of the list.
     * 
     * @param lv
     * @return
     */
    protected ObservableList<String> createItems(ListView lv) {
        ObservableList<String> items = lv != null ? FXCollections.observableArrayList(lv.getItems())
                : FXCollections.observableArrayList("Foo");
        return items;
    }
}
