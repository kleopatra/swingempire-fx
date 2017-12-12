/*
 * Created on 12.12.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Example by ItachiUchiha:
 * https://stackoverflow.com/a/30030114/203657
 * 
 * new question: 
 * https://stackoverflow.com/q/47757368/203657
 * bind radio selected to listview selected
 * 
 * works as-is in fx9, but has issues in fx8 (see answer on SO)
 */
public class RadioButtonListView extends Application {

    public static final ObservableList names =
            FXCollections.observableArrayList();
    private ToggleGroup group = new ToggleGroup();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("List View Sample");

        final ListView listView = new ListView();
        listView.setPrefSize(200, 250);
        listView.setEditable(true);

        names.addAll(
                "Adam", "Alex", "Alfred", "Albert",
                "Brenda", "Connie", "Derek", "Donny",
                "Lynne", "Myrtle", "Rose", "Rudolph",
                "Tony", "Trudy", "Williams", "Zach"
        );

        listView.setItems(names);
        listView.setCellFactory(param -> new RadioListCell());

        StackPane root = new StackPane();
        root.getChildren().add(listView);
        primaryStage.setScene(new Scene(root, 200, 250));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class RadioListCell extends ListCell<String> {
        
        RadioButton radioButton;
        ChangeListener<Boolean> radioListener = (src, ov, nv) -> radioChanged(nv);
        WeakChangeListener<Boolean> weakRadioListener = new WeakChangeListener<>(radioListener);
        
        ChangeListener<Boolean> selectedListener = (src, ov, nv) -> selectedChanged(nv);
        WeakChangeListener<Boolean> weakSelectedListener = new WeakChangeListener<>(selectedListener);
        
        public RadioListCell() {
            radioButton = new RadioButton();
            radioButton.selectedProperty().addListener(weakRadioListener);
            radioButton.setFocusTraversable(false);
            // fx8: need to force the radiobutton to the cell's full width
            //radioButton.setMaxWidth(Double.MAX_VALUE);
            // fx8: need to update radio's selection state outside of updateItem
//            selectedProperty().addListener(weakSelectedListener);
        }
        
        /**
         * Callback from listener to cell's selectedProperty.
         * @param selected the current value of the property.
         */
        protected void selectedChanged(Boolean selected) {
            if (selected) {
                radioButton.setSelected(selected);
            }
        }

        /**
         * Callback from listener to radio's selectedProperty.
         * @param selected the current value of the property. 
         */
        protected void radioChanged(boolean selected) {
            if (selected && getListView() != null && !isEmpty() && getIndex() >= 0) {
                getListView().getSelectionModel().select(getIndex());
            }
        }

        @Override
        public void updateItem(String obj, boolean empty) {
            super.updateItem(obj, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                radioButton.setToggleGroup(null);
                radioButton.setSelected(false);
            } else {
                radioButton.setText(obj);
                radioButton.setToggleGroup(group);
                // fx9: this is safe enough, radio always updated
                // fx8: not safe enough, need to listen to cell selected
                radioButton.setSelected(isSelected());
                setGraphic(radioButton);
            }
        }
    }
}

