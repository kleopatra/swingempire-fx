/*
 * Created on 31.08.2014
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
 * Issue: uncontained value not shown in choiceBox's label.
 * 
 * To reproduce: 
 * - run example and click any of the buttons to select/setValue to an uncontained
 *   item
 * - expected: uncontained item shown
 * - actual: uncontained item not shown
 * 
 * Additionally: behaviour different  for initial selection/empty selectiono
 * initially selected: set uncontained value/selectedItem - > selection is cleared, empty
 *   choiceBox, no selection marker in popup (expected behaviour)
 * initially unselected, select by clicking into drop-down: set uncontained value/selectedItem 
 *   -> old selection shown in choicebox, old selection marker in popup (bug) 
 *  
 * reported: https://javafx-jira.kenai.com/browse/RT-38826
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxUncontainedValue extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "5-item", "4-item", "3-item", "2-item", "1-item");
    private String title;

    /**
     * @return
     */
    private Parent getContent() {
        ChoiceBox<String> box = new ChoiceBox<>(items);
//        ChoiceBoxX<String> box = new ChoiceBoxX<>(items);
        // variant: initial uncontained value 
        box.setValue("initial uncontained");
        // variant: initial selection
//        box.setValue(items.get(0));
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
        HBox buttons = new HBox(setSelectedItemUncontained, setValue);
        
        BorderPane pane = new BorderPane(box);
        pane.setBottom(buttons);
        title = box.getClass().getSimpleName();
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(System.getProperty("java.version") + title);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ChoiceBoxUncontainedValue.class
            .getName());
}
