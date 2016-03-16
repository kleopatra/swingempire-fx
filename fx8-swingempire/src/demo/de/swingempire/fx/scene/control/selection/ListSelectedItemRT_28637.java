/*
 * Created on 12.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Regression guard against 28637
 * Please run the code.
 *  Select the first item.
 *  Click the Remove button, which removes the first item.
 *  Click the Show button, which says, that the selected item is "String1", 
 *  although it is no longer in the list and cannot be the selected item. (The real selected item is in fact "String2")
 *
 *  This only works, if you removed the first item in the list.
 *
 *  
 */
public class ListSelectedItemRT_28637 extends Application {
    public static void main(String[] args) {
        Application.launch();
    }

    private ObservableList<String> items = FXCollections.observableArrayList("String1", "String2", "String3", "String4");

    @Override
    public void start(Stage stage) throws Exception {

        final ListViewAnchored<String> listView = new ListViewAnchored<String>();
        listView.setItems(items);
        
        // unrelated: use multipleSelectionModel
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //------ end unrelated
        
        Button button = new Button("Remove");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                items.remove(listView.getSelectionModel().getSelectedItem());
            }
        });

        Button btnShow = new Button("Show");
        btnShow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println(listView.getSelectionModel().getSelectedItem());
            }
        });

        Scene scene = new Scene(new VBox(10, listView, button, btnShow));
        stage.setScene(scene);
        stage.show();
    }
}