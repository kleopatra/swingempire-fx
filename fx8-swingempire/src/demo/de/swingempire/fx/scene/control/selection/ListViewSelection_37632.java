/*
 * Created on 19.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * selectedIndex out of sync with selected indices
 * 
 * fixed 8u60b5
 */
public class ListViewSelection_37632 extends Application {

    @Override
    public void start(Stage stage)
    {
        final ObservableList<String> listOne = FXCollections.observableArrayList("A", "B", "C");
        final ObservableList<String> listTwo = FXCollections.observableArrayList("C");
        
        final ListView<String> listView = new ListView<>();
        listView.setItems(listOne);
        listView.onMouseClickedProperty().setValue(e -> {
            System.err.println("selectedIndex = " + listView.getSelectionModel().getSelectedIndex());
            System.err.println("selectedIndices = " + listView.getSelectionModel().getSelectedIndices());
        });
        listView.getSelectionModel().selectFirst();
        
        TextField textField = new TextField();
        textField.onKeyReleasedProperty().setValue(e -> listView.setItems(listTwo));

        final Label label = new Label();
        label.textProperty().bind(listView.selectionModelProperty().getValue().selectedItemProperty());

        VBox root = new VBox(10, label, textField, listView);
        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
