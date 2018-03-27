/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
;/**
 * Trying to add item/start edit on new item in commitHandler:
 * https://stackoverflow.com/q/46047134/203657
 * 
 * Trying to listen to editingIndex - doesn work
 * 
 * for a working example
 * @see ListViewCommitHandler 
 */
public class ListViewAutoEditInHandler2 extends Application {

    private ListView<String> simpleList;
    private int expectedEditIndex = -1;
    

    @Override
    public void start(Stage primaryStage) {
        simpleList = new ListView<>(FXCollections.observableArrayList("Item1"));
        simpleList.setEditable(true);
        
        simpleList.setCellFactory(TextFieldListCell.forListView());

        simpleList.editingIndexProperty().addListener(c -> p("editingIndex invalidated"));
        simpleList.editingIndexProperty().addListener((src, ov, nv) -> {
            p("editing index changed: " + ov + " -> " + nv + " expected " + expectedEditIndex);
//            if (expectedEditIndex < 0) return;
//            if (
//                    ov.intValue() == expectedEditIndex - 1 && 
//                    nv.intValue() < 0) {
//                p("re-edit");
//                expectedEditIndex = -1;
//                simpleList.edit(ov.intValue());
//            }
        });
        simpleList.setOnEditStart(t -> p("edit start: " + t.getIndex()));
        simpleList.setOnEditCancel(t -> p("edit cancel: " + t.getIndex()));
        simpleList.setOnEditCommit(t -> {
            int index = t.getIndex();
            p("edit commit: " + index);
            // any modification of the items will trigger a cancel
            if (index == simpleList.getItems().size() - 1) {
                p("editing? " + simpleList.getEditingIndex());
                expectedEditIndex = index + 1;
                simpleList.getItems().add("newItem");
                simpleList.getSelectionModel().select(expectedEditIndex);
//                simpleList.edit(expectedEditIndex);
                // ... so we start a timer to force
                // uncomment for a brittle solution ;)
//                 editTimer.playFromStart();
            } else {
                // reset .. a bit paranoid here ;)
//                expectedEditIndex = -1;
//                editTimer.stop();
            }
            simpleList.getItems().set(index, t.getNewValue());

        });


        BorderPane root = new BorderPane(simpleList);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void p(String t) {
        System.out.println(t);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
