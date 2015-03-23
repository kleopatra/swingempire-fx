/*
 * Created on 19.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Empty is correct, but selectedItems/selectedIndices are not
 */
public class SelectionProperties_39966 extends Application {

    @Override
    public void start(Stage primaryStage) {

        try {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root, 400, 400);

            ObservableList<String> list = FXCollections
                    .observableArrayList("very first", "Hello World", "Other"); //, "sky");
            int first = 1;
            int last = 2;
            ListView<String> listView = new ListView<>(list);
//            listView.setSelectionModel(new SimpleListSelectionModel<>(listView));
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            ListChangeListener indicesListener = change -> {
                System.out.println("--- indicesListener source: " + change.getList() 
                        + "\n  selectedItems: " + listView.getSelectionModel().getSelectedItems()
                        + "\n  selectedIndex " + listView.getSelectionModel().getSelectedIndex()
                        + "\n  selectedItem " + listView.getSelectionModel().getSelectedItem()
                        );  
            };
            listView.getSelectionModel().getSelectedIndices().addListener(indicesListener);
            
            ListChangeListener itemsListener = change -> {
               System.out.println("--- itemsListener source: " + change.getList()   
                       + "\n   selectedIndices: " + listView.getSelectionModel().getSelectedIndices()
                       + "\n   selectedIndex:  " + listView.getSelectionModel().getSelectedIndex()
                       + "\n  selectedItem " + listView.getSelectionModel().getSelectedItem()
                       );  
            };
            listView.getSelectionModel().getSelectedItems().addListener(itemsListener);
            
            listView.getSelectionModel()
                .selectedItemProperty()
                .addListener((value, s1, s2) -> {
                System.out.println("--- itemListener  old/new: " + s1 + " / " +s2 
                        + "\n   selectedItems: " + listView.getSelectionModel().getSelectedItems()
                        + "\n   selectedIndices: " + listView.getSelectionModel().getSelectedIndices()
                        + "\n   selectedIndex:  "+ listView.getSelectionModel().getSelectedIndex()
                        );  
                System.out.println("  listening to selectedItem: selection empty "
                        + listView.getSelectionModel()
                        .isEmpty());
            });
            listView.getSelectionModel()
                    .selectedIndexProperty()
                    .addListener((value, s1, s2) -> {
                        System.out.println("--- indexListener old/new: " + s1 + " / " + s2
                                + "\n selectedItems: " + listView.getSelectionModel().getSelectedItems()
                                + "\n selectedIndices: " + listView.getSelectionModel().getSelectedIndices()
                                + "\n selectedItem:  "+ listView.getSelectionModel().getSelectedItem()
                                );  
                                System.out.println("   listening to selectedIndex: selection empty "
                                        + listView.getSelectionModel()
                                                .isEmpty());
                            });
            System.out
                    .println(listView.getSelectionModel().getClass().getSimpleName() + "step 1: selection {0, 2} - "
                            + "\n expecting selectionmodel.isEmpty to be false");
            // selection moved to next if first had been selected and rmoved
            // simply removed if not the very first
            listView.getSelectionModel().selectIndices(first, 2);

            System.out
                    .println("\n\nstep 2: removing item 0 - "
                            + "\n expecting selectionmodel.isEmpty to be false and last item selected ");
            list.remove(last); //list.get(listView.getSelectionModel().getSelectedIndices().get(0)));

            root.setCenter(listView);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
