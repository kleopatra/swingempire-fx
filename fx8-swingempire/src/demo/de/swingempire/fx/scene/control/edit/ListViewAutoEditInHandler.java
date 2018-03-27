/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
;/**
 * Trying to add item/start edit on new item in commitHandler:
 * https://stackoverflow.com/q/46047134/203657
 * 
 * one last try: use timer to start editing some time later .. working but fishy
 * (basically unspecified, and as such unreliable)
 * 
 * for a working example
 * @see ListViewCommitHandler 
 */
public class ListViewAutoEditInHandler extends Application {

    private ListView<String> simpleList;
    private int expectedEditIndex = -1;
    private Timeline editTimer;
    
    /**
     * Callback for editTimer. Implemented to scroll to and force
     * edit of cell at expectedEditIndex.
     */
    private void checkEdit() {
        if (expectedEditIndex < 0) return;
        if (expectedEditIndex == simpleList.getEditingIndex()) {
            expectedEditIndex = -1;
            return;
        }
        int index = expectedEditIndex;
        expectedEditIndex = -1;
        simpleList.scrollTo(index);
        simpleList.edit(index);
    }

    @Override
    public void start(Stage primaryStage) {
        editTimer = new Timeline(new KeyFrame(Duration.millis(100), ae -> checkEdit() ));
        simpleList = new ListView<>(FXCollections.observableArrayList("Item1"));
        simpleList.setEditable(true);
        
        simpleList.setCellFactory(TextFieldListCell.forListView());

        simpleList.setOnEditStart(t -> p("edit start: " + t.getIndex()));
        simpleList.setOnEditCancel(t -> p("edit cancel: " + t.getIndex()));
        simpleList.setOnEditCommit(t -> {
            p("edit commit: " + t.getIndex());
            // any modification of the items will trigger a cancel
            simpleList.getItems().set(t.getIndex(), t.getNewValue());
            //  p("editing? " + simpleList.getEditingIndex());
            if (t.getIndex() == simpleList.getItems().size() - 1) {
                expectedEditIndex = t.getIndex() + 1;
                simpleList.getItems().add("newItem");
                simpleList.getSelectionModel().select(expectedEditIndex);
                simpleList.edit(expectedEditIndex);
                // ... so we start a timer to force
                // uncomment for a brittle solution ;)
                 editTimer.playFromStart();
            } else {
                // reset .. a bit paranoid here ;)
                expectedEditIndex = -1;
                editTimer.stop();
            }

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
