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
 * 
 * Left-over from Issue-??: 
 * Must use InvalidationListener on list-valued properties.
 * 
 * ListCells are listening to itemsProperty and register
 * ListChangeListeners if changed. Those aren't
 * updated when replacing with an equals list. Here we
 * see that the cell is still notified when the old list
 * changes.
 * 
 * - click replace-items to replace the items by a list that's equal but not the same
 * - click update-old to change the previous list
 * - expected: updateItem not called
 * - actual: updateItem called
 * 
 * no harm done except listening to stale list
 * - click update-current
 * - expected and actual: updateItem called and updated
 * 
 * which might imply that the listener is not needed at all: now skin
 * updates its listener correctly (in an InvalidationListener) and reliably triggers
 * cell updates on changes to the itemsProperty. 
 * 
 */
public class ListCellListeningToOldItems extends Application {

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

        Button reset = new Button("Replace items to equal list");
        reset.setOnAction(e -> {
            previous = listView.getItems();
            listView.setItems(createItems(listView));
        });
        
        Button updateOld = new Button("update previous list at 0");
        updateOld.setOnAction(e -> {
            if (previous == null || previous == listView.getItems()) {
                System.out.println("same items");
                return;
            }
            previous.set(0, "changed old");
        });
        Button updateCurrent = new Button("Update current list at 0");
        updateCurrent.setOnAction(e -> {
            Object old = listView.getItems().get(0);
            listView.getItems().set(0, old + "X");
            
        });
        Parent content = new VBox(listView, reset, updateOld, updateCurrent);
        stage.setScene(new Scene(content, 200, 180));
        stage.setTitle(System.getProperty("java.version"));
        stage.show();

    }

    int count;
    private ObservableList<String> previous;
    /**
     * Creates and returns a list the is equal to but not the same as 
     * the current items of the list.
     */
    protected ObservableList<String> createItems(ListView lv) {
        ObservableList<String> items = lv != null ? FXCollections.observableArrayList(lv.getItems())
                : FXCollections.observableArrayList("Foo");
        return items;
    }
}
