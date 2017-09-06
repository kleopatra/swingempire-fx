/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
;/**
 * Trying to add item/start edit on new item in commitHandler:
 * https://stackoverflow.com/q/46047134/203657
 * 
 * giving up ...
 * 
 * one last try: use timer to start editing some time later ..
 */
public class ListViewCommitHandler extends Application {

    private ListView<String> simpleList;
    private ChangeListener skinListener = (src, ov, nv) -> skinChanged();
    private VirtualFlow flow;
    private int expectedEditIndex = -1;
    protected boolean watchCancel;
    private Timeline editTimer;
    /**
     * responsible for cancelEdit after modification of items is ListViewSkin as
     * side-effect of fix to
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8094887
     * 
     * so trying to install our own listener after skin did doesn't help: still
     * not edited
     */
    private void skinChanged() {
        simpleList.skinProperty().removeListener(skinListener);
//        simpleList.getItems().addListener(
//                (Change<? extends String> c) -> FXUtils.prettyPrint(c));
        simpleList.getItems().addListener((Change<? extends String> c) -> {
            while (c.next()) {
//                if (c.wasAdded() && c.getAddedSize() == 1) {
//                    String added = c.getAddedSubList().get(0);
//                    int index = c.getList().size() - 1;
//                    String last = c.getList().get(index);
//                    if ("newItem".equals(added) && added.equals(last)) {
//                        expectedEditIndex = index;
////                        simpleList.getSelectionModel().select(index);
//                        simpleList.edit(index);
//                        // list thinks its editing, but editing component not
//                        // inserted
//                        System.out.println("editing in itemsListener: " + index
//                                + " / " + simpleList.getEditingIndex());
//                        break;
//                    }
//
//                }
            }
        });

        flow = (VirtualFlow) simpleList.getChildrenUnmodifiable().get(0);
    }

    private void checkEdit() {
        if (expectedEditIndex < 0) return;
        int index = expectedEditIndex;
        expectedEditIndex = -1;
        simpleList.edit(index);
    }

    @Override
    public void start(Stage primaryStage) {
        editTimer = new Timeline(new KeyFrame(Duration.millis(100), ae -> checkEdit() ));
        simpleList = new ListView<>(FXCollections.observableArrayList("Item1")) {
//               , "Item2", "Item3", "Item4")) {


                    @Override
                    protected void layoutChildren() {
                        super.layoutChildren();
                        
//                        int expected = expectedEditIndex; //getEditingIndex();
//                        if (expected != -1) {
//                            
//                            IndexedCell cell = flow.getVisibleCell(expected);
//                            p("cell at editingIndex: " + cell);
//                            if (cell != null) {
//                                boolean cellIsEditing = cell.isEditing();
//                                int listEditingIndex = getEditingIndex();
//                                p("in layout: editingCell ? " + expected + cellIsEditing 
//                                        + " list: " + listEditingIndex + " index of cell " + cell.getIndex());
//                                if (!cellIsEditing ) { //&& getEditingIndex() == expectedEditIndex) {
////                                    expectedEditIndex = -1;
////                                    cell.startEdit();
//                                    watchCancel=true;
//                                    edit(expectedEditIndex);
//                                    p("list after " + getEditingIndex() + cell.isEmpty());
//                                }
//                            }
//                        }
//                    
                    }
            
        };
        simpleList.setEditable(true);
        simpleList.skinProperty().addListener(skinListener);
        
        simpleList.setCellFactory(TextFieldListCell.forListView());

        simpleList.setOnEditStart(t -> {
            System.out.println(
                    "setOnEditStart " + t.getIndex() + " /" + t.getNewValue());
        });
        simpleList.setOnEditCommit(t -> {
            System.out.println(
                    "setOnEditCommit " + t.getIndex() + " /" + t.getNewValue());
            simpleList.getItems().set(t.getIndex(), t.getNewValue());
            if (t.getIndex() == simpleList.getItems().size() - 1) {
                int index = t.getIndex() + 1;
                // System.out.println("setOnEditCommit - last " + t.getIndex() +
                // " /" + t);
                simpleList.getItems().add("newItem");
                expectedEditIndex = index;
                simpleList.getSelectionModel().select(index);
//                simpleList.getFocusModel().focus(index);
                simpleList.edit(index);
                editTimer.playFromStart();
                // runlater doesn't help?
                // sometimes, but gets out of sync somehow (incorrect cell
                // starts
                // editing after a while
//                Platform.runLater(() -> {
//
//                });
            }

        });

        simpleList.setOnEditCancel(t -> {
            System.out.println(
                    "setOnEditCancel " + t.getIndex() + " /" + t.getNewValue());
            if (watchCancel) {
                
                 new RuntimeException("who-is-calling?").printStackTrace();
            }
        });

        BorderPane root = new BorderPane(simpleList);
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
