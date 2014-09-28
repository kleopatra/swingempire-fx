/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.choiceboxx.ChoiceBoxX;
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
 * ----- Issue: Behaviour of items.setItem(selectedIndex, newItem)
 * 
 * expected: 
 * either:  selectedIndex == oldSelectedIndex, selectedItem == newItem == value
 * or: selectedIndex == -1, selectedItem == oldItem == value, value shown 
 * 
 * first is selectedIndex-rules, second is selectedItem-rules and oldItem uncontained
 * 
 * actual:
 *  selectedIndex == -1, selectedItem == oldSelectedItem == value, nothing shown
 *  
 * ----- Issue: Behaviour of uncontained selectedItem/value
 * 
 * Explicitly doc'ed to allow values that are not in the list, that is dev can set
 * either selectedItem or value to something not in the list
 * 
 * Expected:
 * uncontained value shown in choicebox, selection marker removed in drop-down
 * actual:
 * old selectedItem/value shown in choiceBox, selection marker on old selected in drop-down  
 * 
 * ----- Issue: Initial uncontained value
 * 
 * expected: 
 * initial uncontained shown in choicebox (and no selection marker in dropdown)
 * actual
 * nothing shown in choicebox
 * 
 * In all: selectedItem/value always synched (expected), but not shown/updated in view
 * not showing might be the main problem, though behaviour on setItem still slightly
 * unintuitive: we changed the underlying data, that change should rule the new state.
 * Instead the old value is "sticky". Solvable by letting the model rule: show whatever
 * the model decides to be the new value/selectedItem.
 * 
 * ----- Issue: behaviour different  for initial selection/empty selectiono
 * initially selected: set uncontained value/selectedItem - > selection is cleared, empty
 *   choiceBox, no selection marker in popup (expected behaviour)
 * initially unselected, select by clicking into drop-down: set uncontained value/selectedItem 
 *   -> old selection shown in choicebox, old selection marker in popup (bug) 
 *  
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceSetItem extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");

    /**
     * @return
     */
    private Parent getContent() {
        ChoiceBox<String> box = new ChoiceBox<>(items);
//        ChoiceBoxX<String> box = new ChoiceBoxX<>(items);
        // uncontained value never shown
//        box.setValue("initial uncontained");
        box.setValue(items.get(0));
//        ChoiceBoxRT38724<String> box = new ChoiceBoxRT38724<>(items);
        Button setItem = new Button("Set item at selection");
        setItem.setOnAction(e -> {
            SingleSelectionModel model = box.getSelectionModel();
            int oldSelected = model.getSelectedIndex();
            if (oldSelected == -1) return;
            String newItem = box.getItems().get(oldSelected) + "xx";
            box.getItems().set(oldSelected, newItem);
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
            
        });
        
        Button setSelectedItemUncontained = new Button("Set selectedItem to uncontained");
        setSelectedItemUncontained.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            model.select("myDummySelectedItem");
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        Button setValue = new Button("Set value to uncontained");
        setValue.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            box.setValue("myDummyValue");
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        Button setNullSelectionModel = new Button("set null selectionModel and set value");
        setNullSelectionModel.setOnAction(e -> {
            box.setSelectionModel(null);
            box.setValue(items.get(2));
        });
        HBox buttons = new HBox(setItem, setSelectedItemUncontained, setValue, setNullSelectionModel);
        
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
    private static final Logger LOG = Logger.getLogger(ChoiceSetItem.class
            .getName());
}
