/*
 * Created 14.10.2020 
 */

package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
/**
 * Bug https://bugs.openjdk.java.net/browse/JDK-8221722
 * Uncontained value not shown:
 * 
 * Default ButtonCell (aka plain ListCell implementation in combo's skin)
 * - run
 * - (initial uncontained or initial null doesn't make a difference)
 * - press button to show uncontained: uncontained shown
 * - select contained
 * - press button again
 * - expected: uncontained shown again
 * - actual: empty button cell
 * 
 * Still virulent in fx15 (+dev to fx16)
 */
public class ComboUncontainedBug extends Application {
    /**
     * ListCell with custom text and graphic when empty (or null item)
     */
    public static class ButtonCell<T> extends ListCell<T> {
        private Node emptyGraphic = new Rectangle(5, 5);
        private Node contentGraphic = new Circle(10);
        private String emptyText;
        
        public ButtonCell(String emptyText) {
            this.emptyText = emptyText;
            textProperty().addListener((src, ov, nv) -> {
                System.out.println(emptyText + " textListener: " + getText() 
                    + " at index:" + getIndex() + " empty: " + isEmpty());
                });
        }
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            System.out.println(emptyText + " updateItem " + item + " at index: " + getIndex() + " empty: " + empty);
            if (empty || item == null) {
                setText(String.valueOf(item));
                setGraphic(emptyGraphic);
            } else {
                setText(String.valueOf(item));
                setGraphic(contentGraphic);
            }
        }
    }
    

    private Parent createContent() {
        TabPane pane = new TabPane(
                new Tab("initial set", createTabContent(true, false))
                , new Tab("initial null", createTabContent(false, false))
                , new Tab("initial set, custom cell", createTabContent(true, true))
//                , new Tab("initial null, custom cell", createTabContent(false, true))
                );
        // quick check of keyboard navigation in standalone list with duplicates
        // in combo: jumps over to first, in list: is okay
        ListView<String> combo = new ListView<>(
                FXCollections.observableArrayList(
//                        null, // JDK-8093165: null items in list (was RT-39809)
                        "Option 1", // JDK-8127705: duplicate items (was RT-19227) 
                                    // also JDK-8087523 list-selected != combo-selected with duplicates
                        "Option 1", 
                        "Option 1", 
                        "Option 1", 
                        "Option 2", "Option 3")) ;
        pane.getTabs().add(new Tab("ListView", combo));
        
        return pane;
    }

    int count;
    protected VBox createTabContent(boolean setInitial, boolean customCell) {
        ComboBox<String> combo = new ComboBox<>(
                FXCollections.observableArrayList(
//                        null, // JDK-8093165: null items in list (was RT-39809)
                        "Option 1", // JDK-8127705: duplicate items (was RT-19227) 
                                    // also JDK-8087523 list-selected != combo-selected with duplicates
                        "Option 1", // in keyboard nav up jumps to first of duplicates
                        "Option 1", 
                        "Option 1", 
                        "Option 2", "Option 3")) ;
        if (customCell) combo.setButtonCell(new ButtonCell("id: " + count++));
        if (setInitial) combo.setValue("initial");
        Button uncontained = new Button("set value to uncontained");
        uncontained.setOnAction(e -> combo.setValue("uncontained value"));
        Button clear = new Button("clear");
        clear.setOnAction(e -> {
//            combo.setValue(null);
            combo.getSelectionModel().clearSelection();
        });
        VBox content = new VBox(10, combo, uncontained, clear);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
//        stage.getScene().getStylesheets().add(getClass().getResource("comboselectedstyle.cs").toExternalForm());

        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboUncontainedBug.class.getName());

} 