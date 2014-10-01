/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * ChoiceBox: incorrect selection state after setItem at selectedIndex
 * 
 * To reproduce:
 * - on startup, the index null is selected and shown
 * - click button that sets a new item at selectedIndex 
 * - expected (as per rejection of RT-19820): 
 *   all three selectedIndex/selectedItem/comboValue cleared
 * - actual:
 *   selectedIndex cleared, selectedItem/value == item before set
 *      
 * Note: the misbehaviour of selectedItem/value cannot be seen due
 * to RT-38826 (choice never shows an uncontained selectedItem/value)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxSetItemAtSelectedIndexBugRT_38864 extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");

    private Parent getContent() {
        String initialValue = items.get(0);
        ChoiceBox<String> box = new ChoiceBox<>(items);
        box.setValue(initialValue);
        Button setItem = new Button("Set item at selection");
        setItem.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            if (model == null) return;
            int oldSelected = model.getSelectedIndex();
            if (oldSelected == -1) return;
            String newItem = box.getItems().get(oldSelected) + "xx";
            box.getItems().set(oldSelected, newItem);
            LOG.info("selected/item/value " + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        
        HBox buttons = new HBox(setItem);
        BorderPane pane = new BorderPane(box);
        pane.setBottom(buttons);
        return pane;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ChoiceBoxSetItemAtSelectedIndexBugRT_38864.class
            .getName());
}
