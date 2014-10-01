/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * ComboBox: incorrect selection state after setItem at selectedIndex
 * 
 * To reproduce:
 * - on startup, the index null is selected and shown
 * - click button that sets a new item at selectedIndex 
 * - expected: 
 *    either: selectedIndex unchanged, selectedItem/comboValue updated to new item
 *      (behaviour of ListView)
 *    or: selectedIndex/selectedItem/comboValue cleared
 *      (intended behaviour of ChoiceBox as per rejection of RT-19820)
 * - actual:
 *   selectedIndex is incremented by 1, selectedItem/value == item at
 *   incremented index 
 *         
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxSetItemAtSelectedIndexBugRT_38865 extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");

    /**
     * @return
     */
    private Parent getContent() {
        String initialValue = items.get(0);
        ComboBox<String> box = new ComboBox<>(items);
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
    private static final Logger LOG = Logger.getLogger(ComboBoxSetItemAtSelectedIndexBugRT_38865.class
            .getName());
}
