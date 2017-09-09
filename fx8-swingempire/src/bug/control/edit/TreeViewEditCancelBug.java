/*
 * Created on 05.09.2017
 *
 */
package control.edit;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TreeViewCell: check state of cancel event
 * 
 * reported:
 */
public class TreeViewEditCancelBug extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        TreeItem<String> rootItem = new TreeItem<>("root");
        TreeItem<String> child = new TreeItem<>("child", new Label("X"));
        rootItem.getChildren().add(child);
        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(TextFieldTreeCell.forTreeView());

//        treeView.setOnEditCancel(e -> );
        Button check = new Button("Print child value");
        check.setOnAction(e -> System.out.println(child.getValue()));
        BorderPane pane = new BorderPane(treeView);
        pane.setBottom(check);
        Scene scene = new Scene(pane, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}