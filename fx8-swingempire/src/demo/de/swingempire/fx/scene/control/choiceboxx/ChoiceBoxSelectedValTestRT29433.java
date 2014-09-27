/*
 * Created on 24.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import com.sun.javafx.runtime.VersionInfo;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 1. Click 'Set data' button.
 * 2. Select any item.
3. Push 'Show selected item' button. It will print out selected item value.
4. Push 'Set empty list' button.
=>Push 'Show selected item' button. It will print out previous selected item value.
Expected behavior is to recieve 'null'.

 * Arguable: ChoiceBox explicitly allows selectedItem that is not in the list, not 
 * being able to be in the list trivially (because it is empty) is just a corner case
 * of uncontained.
 *

 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxSelectedValTestRT29433 extends Application {

    ObservableList<String> data
            = FXCollections.observableArrayList(
                "item1", "item2", "item3");
    
    public static void main(String[] args) {
        launch(args);
    }
    private ChoiceBox cb;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(VersionInfo.getRuntimeVersion());
        stage.setScene(createScene());
        stage.show();
    }

    private Scene createScene() {
        cb = new ChoiceBox();
     
        Button btnSetEmpty = new Button("Set empty list");
        btnSetEmpty.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                cb.setItems(FXCollections.emptyObservableList());
            }
        });
        
        Button btnSetData = new Button("Set data");
        btnSetData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) { cb.setItems(data); }
        });
        
        Button btnClearItems = new Button("Clear items");
        btnClearItems.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                cb.getItems().clear();
            }
        });
        
        Button btnShowSelected = new Button("Show selected item");
        btnShowSelected.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                System.out.println("selected item = " + cb.getSelectionModel().getSelectedItem());
            }
        });
        
        Button setItemAtSelected = new Button("setItem at selectedIndex");
        setItemAtSelected.setOnAction(e -> {
            int selected = cb.getSelectionModel().getSelectedIndex();
            if (selected < 0 ) return;
            Object item = cb.getItems().get(selected);
            cb.getItems().set(selected, item + "xx");
        });
        return new Scene(new HBox(10, cb, new VBox(5, btnSetData, btnSetEmpty, 
                btnClearItems, btnShowSelected, setItemAtSelected)));
    }
}
