/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection.fixedissues;

import de.swingempire.fx.util.DebugUtils;
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
     * 
     * <p>
     * new coordinates:
     * https://bugs.openjdk.java.net/browse/JDK-8097447
     * fixed.
     * @author Jeanette Winzenburg, Berlin
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public class ListViewInsertRT_39042 extends Application {
    
        ObservableList items = FXCollections.observableArrayList("one", "two", "three");
        int count;
        @Override
        public void start(Stage primaryStage) throws Exception {
//            ListCell c;
            ListView listView = new ListView();
            listView.setItems(items);
            Button add = new Button("Insert at selection");
            add.setOnAction(e -> {
                int index = listView.getSelectionModel().getSelectedIndex();
                if (index < 0) return;
                listView.getItems().add(0, "item " + count++);
                DebugUtils.printSelectionState(listView);
            });
            // check if always on new items ... yes
            Button setItems = new Button("Set items");
            setItems.setOnAction(e -> {
                
                listView.setItems(FXCollections.observableArrayList("other", "items", "same", "problem"));
            });
            Button remove = new Button("Remove at selection");
            remove.setOnAction(e -> {
                // remove seems okay
                int index = listView.getSelectionModel().getSelectedIndex();
                if (index < 0) return;
                listView.getItems().remove(index);
            });
            Parent pane = new VBox(listView, add, setItems, remove);
            primaryStage.setScene(new Scene(pane, 100, 220));
            primaryStage.setTitle(System.getProperty("java.version"));
            primaryStage.show();
        }
    
        public static void main(String[] args) {
            launch(args);
        }
    }
