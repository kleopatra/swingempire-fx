/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;


import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * TreeView: receives cancel event on commit
 * https://bugs.openjdk.java.net/browse/JDK-8124615
 * TreeView: F2 fires cancel
 * https://bugs.openjdk.java.net/browse/JDK-8123783
 * 
 * example from bug report (was TVEvents), fixed
 * 
 * PENDING: TreeView has no commitHandler?
 */
public class TreeViewEditEvents extends Application {

    private int onEditStartCounter = 0;
    private int onEditCancelCounter = 0;
    private int onEditCommitCounter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        TreeView<String> treeView = new TreeView<>();
        treeView.setEditable(true);
        treeView.setCellFactory(getCellFactory());

        TreeItem<String> rootItem = new TreeItem<>("root");
        rootItem.setExpanded(true);

        treeView.setRoot(rootItem);
        ObservableList<TreeItem<String>> children = treeView.getRoot().getChildren();
        for (int i = 0; i < 10; i++) {
            children.add(new TreeItem("item-" + (i + 1)));
        }

        final Label labelOnEditStartCounter = new Label("On edit start: ");
        final Label labelOnEditCancelCounter = new Label("On edit cancel: ");
        final Label labelOnEditCommitCounter = new Label("On edit commit: ");

        treeView.setOnEditStart(new EventHandler() {
            @Override
            public void handle(Event t) {
                onEditStartCounter++;
                labelOnEditStartCounter.setText("On edit start: " + String.valueOf(onEditStartCounter));
            }
        });

        treeView.addEventHandler(treeView.editCommitEvent(), new EventHandler() {
//        treeView.setOnEditCommit(new EventHandler() {
            @Override
            public void handle(Event t) {
                System.out.println(treeView.getOnEditCommit());
                onEditCommitCounter++;
                labelOnEditCommitCounter.setText("On edit commit: " + String.valueOf(onEditCommitCounter));
            }
        });

        treeView.setOnEditCancel(new EventHandler() {
            @Override
            public void handle(Event t) {
                onEditCancelCounter++;
                labelOnEditCancelCounter.setText("On edit cancel: " + String.valueOf(onEditCancelCounter));
            }
        });

        VBox vBox = new VBox(10d);
        vBox.getChildren().addAll(labelOnEditStartCounter, labelOnEditCancelCounter, labelOnEditCommitCounter);

        HBox hBox = new HBox(15d);
        hBox.getChildren().addAll(treeView, vBox);
        Scene scene = new Scene(hBox, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    private Callback<TreeView<String>, TreeCell<String>> getCellFactory() {
        return javafx.scene.control.cell.TextFieldTreeCell.forTreeView();
    }
}