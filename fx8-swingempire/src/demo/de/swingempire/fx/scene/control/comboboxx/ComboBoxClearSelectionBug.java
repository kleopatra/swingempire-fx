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
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * ---- Issue: clear selection depends on history
 * 
 * Steps A:
 * - select an item from popup
 * - press clearSelection: selectedItem/value cleared (expected)
 * - press selectUncontainedItem: item shoen as selected item (expected) 
 * - press clearSelection: selectedItem/value not cleared (maybe expected)[*]
 * 
 * Steps B:
 * - select an item from popup
 * - press selectUncontainedItem: uncontained shown (expected)
 * - press clearSelection: selectedItem cleared - inconsistent with last step of A
 * 
 * Same inconsistency if the uncontained item is set via setValue 
 * 
 *  
 * [*] the rule for handling the selectedItem on clearSelection seems to be 
 *  - null if contained in items
 *  - keep if not contained in items
 * 
 * Not reported: behaves consistently (keep uncontained) in 8u40b7
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxClearSelectionBug extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");
    private String title;

    /**
     * @return
     */
    private Parent getContent() {
        ComboBoxX<String> box = new ComboBoxX<>(items);
        box.getSelectionModel().select(0);
        Button setSelectedItemUncontained = new Button("Set selectedItem to uncontained");
        setSelectedItemUncontained.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            if (model == null) return;
            model.select("myDummySelectedItem");
        });
        Button setValue = new Button("Set value to uncontained");
        setValue.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            if (model == null) return;
            box.setValue("myDummyValue");
        });
        Button clear = new Button("Clear Selection");
        clear.setOnAction(e -> {
            SingleSelectionModel<String> model = box.getSelectionModel();
            model.clearSelection();
        });
        Pane buttons = new FlowPane(setSelectedItemUncontained, 
                setValue, clear);
        
        
        BorderPane pane = new BorderPane(box);
        pane.setBottom(buttons);
        title = box.getClass().getName();
        return pane;
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
    private static final Logger LOG = Logger.getLogger(ComboBoxClearSelectionBug.class
            .getName());
}
