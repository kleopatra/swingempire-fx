/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import de.swingempire.fx.scene.control.selection.ChoiceSelectionIssues.SimpleChoiceSelectionModel;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class Selectables extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");

    /**
     * @return
     */
    private Parent getContent() {
//        ChoiceBox<String> box = new ChoiceBox<>(items);
        ChoiceBoxRT38724<String> box = new ChoiceBoxRT38724<>(items);
        Button setModel = new Button("Set SelectionModel");
        setModel.setOnAction(e -> {
            SimpleChoiceSelectionModel model = new SimpleChoiceSelectionModel(box);
            int oldSelected = box.getSelectionModel().getSelectedIndex();
            if (oldSelected == -1) {
                oldSelected = items.size();
            }
            model.select(oldSelected - 1);
            box.setSelectionModel(model);
        });
        
        Button select = new Button("Select");
        select.setOnAction(e -> {
            SingleSelectionModel model = box.getSelectionModel();
            int oldSelected = model.getSelectedIndex();
            if (oldSelected == -1) {
                oldSelected = items.size();
            }
            model.select(oldSelected - 1);
            
        });
        HBox buttons = new HBox(setModel, select);
        
        
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
}
