/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

    /**
     * https://javafx-jira.kenai.com/browse/RT-39042
     * Run and press button to insert item at selection
     * - select first item
     * - press button to insert item at selection
     * - expected: selection sticks to old selectedItem, that is now at
     *   second position
     * - actual: selection at third
     * 
     * Note: happens if re-setting items after instantiation.
     * @author Jeanette Winzenburg, Berlin
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public class ListViewInsertRT_39042 extends Application {
    
        int count;
        @Override
        public void start(Stage primaryStage) throws Exception {
            ObservableList items = FXCollections.observableArrayList("one", "two", "three");
            ListView listView = new ListView();
            listView.setItems(items);
            Button add = new Button("Insert at selection");
            add.setOnAction(e -> {
                if (listView.getSelectionModel().getSelectedIndex() < 0) return;
                listView.getItems().add(listView.getSelectionModel().getSelectedIndex(), "item " + count++);
            });
            // check if always on new items ... yes
            Button setItems = new Button("Set items");
            setItems.setOnAction(e -> {
                
                listView.setItems(FXCollections.observableArrayList("other", "items", "same", "problem"));
            });
            Parent pane = new VBox(listView, add, setItems);
            primaryStage.setScene(new Scene(pane));
            primaryStage.setTitle(System.getProperty("java.version"));
            primaryStage.show();
        }
    
        public static void main(String[] args) {
            launch(args);
        }
    }
