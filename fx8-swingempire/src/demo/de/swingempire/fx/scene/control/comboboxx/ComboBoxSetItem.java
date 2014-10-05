/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

/*
 * Created on 31.08.2014
 *
 */

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import de.swingempire.fx.scene.control.comboboxx.ComboBoxX.ComboBoxSelectionModel;

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
public class ComboBoxSetItem extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");
    private String title;

    /**
     * @return
     */
    private Parent getContent() {
        String initialValue = items.get(0);
        ListView listView = new ListView(items);
        listView.getSelectionModel().select(initialValue);
        // core choiceBox
//        ComboBox<String> box = new ComboBox<>(items);
        // extended choiceBox
        ComboBoxX<String> box = new ComboBoxX<>(items);
        // can control behaviour details by custom model in extended
//        box.setSelectionModel(new MySelectionModel(box));
        // uncontained value never shown
//        box.setValue("initial uncontained");

        box.setValue(initialValue);
        Button setItem = new Button("Set item at selection");
        setItem.setOnAction(e -> {
            SingleSelectionModel model = box.getSelectionModel();
            if (model == null) return;
            int oldSelected = model.getSelectedIndex();
            if (oldSelected == -1) return;
            String newItem = box.getItems().get(oldSelected) + "xx";
            box.getItems().set(oldSelected, newItem);
            LOG.info("selected/item/value " + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
            
        });
        Button removeItem = new Button("Remove item at selection");
        removeItem.setOnAction(e -> {
            SingleSelectionModel model = box.getSelectionModel();
            if (model == null) return;
            int oldSelected = model.getSelectedIndex();
            if (oldSelected == -1) return;
            items.remove(oldSelected);
            LOG.info("selected/item/value " + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
            
        });
        Button setSelectedItemUncontained = new Button("Set selectedItem to uncontained");
        setSelectedItemUncontained.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            if (model == null) return;
            model.select("myDummySelectedItem");
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        Button setValue = new Button("Set value to uncontained");
        setValue.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            box.setValue("myDummyValue");
            if (model != null)
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        Button setNullSelectionModel = new Button("set null selectionModel and set value");
        setNullSelectionModel.setOnAction(e -> {
            box.setSelectionModel(null);
            box.setValue(items.get(2));
        });
        Pane buttons = new FlowPane(removeItem, setItem, setSelectedItemUncontained, setValue, setNullSelectionModel);
        
        
        BorderPane pane = new BorderPane(listView);
        pane.setTop(box);
        pane.setBottom(buttons);
        title =box.getClass().getName();
        return pane;
    }


    /**
     * A SelectionModel that updates the selectedItem if it is contained in
     * the data list and was replaced/updated.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class MySelectionModel<T> extends ComboBoxSelectionModel<T> {

        public MySelectionModel(ComboBoxX<T> cb) {
            super(cb);
        }

        @Override
        protected void itemsChanged(Change<? extends T> c) {
            // selection is in list
            if (getSelectedIndex() != -1) {
                while (c.next()) {
                    if (c.wasReplaced() || c.wasUpdated()) {
                        if (getSelectedIndex() >= c.getFrom()
                                && getSelectedIndex() < c.getTo()) {
                            setSelectedItem(getModelItem(getSelectedIndex()));
                            return;
                        }
                    }
                }
            }
            // super expects a clean change
            c.reset();
            super.itemsChanged(c);
        }

    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ComboBoxSetItem.class
            .getName());
}
