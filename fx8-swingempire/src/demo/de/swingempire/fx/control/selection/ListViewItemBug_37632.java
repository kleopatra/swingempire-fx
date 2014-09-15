/*
 * Created on 04.09.2014
 *
 */
package de.swingempire.fx.control.selection;

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
 * https://javafx-jira.kenai.com/browse/RT-37632
 * listView may not return selected item
 * 
 * Type a character in the TextField then click on "C" in the resulting list. 
 * Notice that getSelectedIndex() and getSelectedIndices() do not return the same value
 * 
 * reported as fixed in 8u40
 */
public class ListViewItemBug_37632 extends Application {

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
        launch();
    }
}
