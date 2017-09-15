/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import de.swingempire.fx.scene.control.cell.DebugTextFieldListCell;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
;/**
 * Trying to add item/start edit on new item in commitHandler:
 * https://stackoverflow.com/q/46047134/203657
 * 
 * Here:
 * - use DebugTextFieldListCell
 * - add item in commitHandler
 * - start edit in itemsListener (that was installed after skin's itemsListener)
 * - force layout before
 * 
 * Working as long as no new cell was created, that is no scrolling to new value needed.
 * If so, the value in the newly editing cell is the last edited.
 */
public class ListViewCommitHandler extends Application {

    private ListView<String> list;
    private ChangeListener skinListener = (src, ov, nv) -> skinChanged();
    /**
     * responsible for cancelEdit after modification of items is ListViewSkin as
     * side-effect of fix to
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8094887
     * 
     * so trying to install our own listener after skin did doesn't help: still
     * not edited
     * 
     * Trying same as in TablePersonAddRowAndEdit: which doesn't work neither if item
     * added in commitHandler (only if added with button)
     */
    private void skinChanged() {
        list.skinProperty().removeListener(skinListener);
        installItemsListener();
    }


    /**
     * Start edit in a listener to the items.
     */
    protected void installItemsListener() {
        // start in items listener - working if cell switched out of editing 
        // before notification
        list.getItems().addListener((Change<? extends String> c) -> {
            while (c.next()) {
                if (c.wasAdded() && ! c.wasRemoved()) {
                    // force the re-layout before starting the edit
                    // still needed
                    list.layout();
                    list.scrollTo(c.getFrom());
                    p("before edit: " + c.getFrom());
                    list.edit(c.getFrom());
                    return;
                }
            }
        });
    }

    protected void commitEdit(EditEvent<String> t) {
        list.getItems().set(t.getIndex(), t.getNewValue());
        if (t.getIndex() == list.getItems().size() - 1) {
            int index = t.getIndex() + 1;
             System.out.println("editCommit: " + t.getIndex() +
             " /" + t);
            list.getItems().add("newItem");
            list.getSelectionModel().select(index);
            list.getFocusModel().focus(index);
//            list.scrollTo(index);
            //does not start edit and leads to cancel with weird index
            // start == 4, cancel == 6
//                simpleList.edit(index);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        list = new ListView<>(FXCollections.observableArrayList("Item1"// {
               , "Item2", "Item3", "Item4")) {
            
        };
        list.setEditable(true);
        // need to install custom itemsListener after skin has installed 
        list.skinProperty().addListener(skinListener);
        // with the custom cell, sequence of listener install is not important
//        installItemsListener();
//        simpleList.setCellFactory(TextFieldListCell.forListView());
        list.setCellFactory(DebugTextFieldListCell.forListView());

        list.setOnEditStart(t -> {
            System.out.println(
                    "setOnEditStart " + t.getIndex() + " /" + t.getNewValue());
        });
//        simpleList.addEventHandler(ListView.editCommitEvent(), t -> {
        list.setOnEditCommit(this::commitEdit);

        list.setOnEditCancel(t -> {
            p("setOnEditCancel " + t.getIndex() + " /" + t.getNewValue());
        });

        BorderPane root = new BorderPane(list);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    private static void p(String text) {
        System.out.println(text);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
