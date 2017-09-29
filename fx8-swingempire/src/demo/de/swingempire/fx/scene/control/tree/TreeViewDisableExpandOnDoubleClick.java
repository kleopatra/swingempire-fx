/*
 * Created on 28.09.2017
 *
 */
package de.swingempire.fx.scene.control.tree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Disable expand on double click
 * https://stackoverflow.com/q/46436974/203657
 */
public class TreeViewDisableExpandOnDoubleClick extends Application {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        TreeView tw = new TreeView();
        TreeItem rootNode = new TreeItem("Root");
        TreeItem blockOne = new TreeItem("Block1");
        TreeItem childA = new TreeItem("ChildA");
        TreeItem childB = new TreeItem("ChildB");
        blockOne.getChildren().add(childA);
        blockOne.getChildren().add(childB);
        TreeItem blockTwo = new TreeItem("Block2");
        TreeItem childC = new TreeItem("ChildC");
        TreeItem childD = new TreeItem("ChildD");
        blockTwo.getChildren().add(childC);
        blockTwo.getChildren().add(childD);
        rootNode.getChildren().add(blockOne);
        rootNode.getChildren().add(blockTwo);
        tw.setRoot(rootNode);


        tw.setCellFactory(param -> {
            TreeCell<String> treeCell = new TreeCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText("");
                        setGraphic(null);
                        return;
                    }

                    setText(item.toString());
                }
            };

            treeCell.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
                if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY))
                    e.consume();
            });
            return treeCell;
        });

        root.getChildren().add(tw);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("TreeView!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}