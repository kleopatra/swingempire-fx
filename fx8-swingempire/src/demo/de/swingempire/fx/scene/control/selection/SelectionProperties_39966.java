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
 */
public class SelectionProperties_39966 extends Application {

    @Override
    public void start(Stage primaryStage) {

        try {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root, 400, 400);

            ObservableList<String> list = FXCollections
                    .observableArrayList("Hello World", "Other", "sky");
            ListView<String> listView = new ListView<>(list);
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            ListChangeListener indicesListener = change -> {
                System.out.println("indicesListener" + change.getList() + "  " + 
                        listView.getSelectionModel().getSelectedIndex()
                        + " " + listView.getSelectionModel().getSelectedItems());  
            };
            
            ListChangeListener itemsListener = change -> {
               System.out.println("itemsListener" + change.getList() + "  " + 
                       listView.getSelectionModel().getSelectedIndex()
                       + " " + listView.getSelectionModel().getSelectedIndices());  
            };
            listView.getSelectionModel().getSelectedIndices().addListener(indicesListener);
            listView.getSelectionModel().getSelectedItems().addListener(itemsListener);
            
            listView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((value, s1, s2) -> {
                                System.out.println("isEmpty: "
                                        + listView.getSelectionModel()
                                                .isEmpty());
                            });
            System.out
                    .println("step 1: selection - expecting selectionmodel.isEmpty to be false");
            listView.getSelectionModel().selectIndices(0, 2);

            System.out
                    .println("step 2: removing item - expecting selectionmodel.isEmpty to be true");
            list.remove(0);

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
