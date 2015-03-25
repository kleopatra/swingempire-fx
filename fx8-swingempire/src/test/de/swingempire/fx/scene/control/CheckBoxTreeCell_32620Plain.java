/*
 * Created on 25.03.2015
 *
 */
package de.swingempire.fx.scene.control;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import com.sun.javafx.runtime.VersionInfo;

/**
 * https://javafx-jira.kenai.com/browse/RT-32620
 * 
 * The example in the bug report doesn't behave as described in api doc of
 * CheckBoxTreeCell, so trying to simplify. CheckBoxTreeCell needs
 * a tree with CheckBoxTreeItems, then working as expected. But:
 * not updating parent state on modifications to the children list.
 * 
 */
public class CheckBoxTreeCell_32620Plain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(VersionInfo.getRuntimeVersion());
        stage.setScene(getScene());
        stage.show();
    }

    private Scene getScene() {
        
        List<TreeItem<String>> ls = new ArrayList<TreeItem<String>>(10);
        for (int i = 0; i < 10; i++) {
            ls.add(new CheckBoxTreeItem<String>("Test " + i));
        }
        
        TreeView<String> tv = new TreeView<String>();
        tv.setRoot(new CheckBoxTreeItem<String>("Root"));
        
        tv.getRoot().getChildren().addAll(ls);
        tv.getRoot().setExpanded(true);
        tv.setEditable(true);
        
        tv.setCellFactory(CheckBoxTreeCell.forTreeView());
        BorderPane root = new BorderPane();
        root.setCenter(tv);
        
        Button removeSelected = new Button("removeSelected");
        removeSelected.setOnAction(e -> {
            TreeItem item = tv.getSelectionModel().getSelectedItem();
            if (item == null || item == tv.getRoot()) return;
            tv.getRoot().getChildren().remove(item);
        });
        HBox buttonPane = new HBox(removeSelected);
        root.setBottom(buttonPane);
        
        return new Scene(root, 600, 400);
    }
 
}