/*
 * Created on 22.01.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * TreeCell showing ellipsis instead of scrollbars.
 * 
 * https://javafx-jira.kenai.com/browse/RT-19468
 * 
 * Hmm ... can't reproduce in 8u40b20
 */
public class TreeEllipsisNoScrollRT_19468 extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    private Parent getContent() {
        Pane pane = new HBox();
        TreeItem<String> root = new TreeItem<String>("ROOT", new Rectangle(20, 20, Color.CHOCOLATE));
        root.setExpanded(true);
        TreeItem<String> firstBrunch = new TreeItem<String>("brunch 1");
        firstBrunch.setExpanded(true);
        firstBrunch.getChildren().addAll(new TreeItem<String>("first item"), new TreeItem<String>("second item", new Rectangle(20, 20, Color.DARKGREY)));
        root.getChildren().addAll(firstBrunch);
        TreeItem<String> secondBrunch = new TreeItem<String>("brunch 2");
        secondBrunch.getChildren().addAll(new TreeItem<String>("first item"), new TreeItem<String>("second item", new Rectangle(20, 20, Color.DARKGREY)));
        root.getChildren().addAll(secondBrunch);
        TreeView tree = new TreeView(root);
        tree.setFocusTraversable(false);
        tree.setMaxSize(130,140);
        pane.getChildren().add(tree);
        return pane;
    }

    public void start(Stage stage) {
        stage.setX(100);
        stage.setY(100);
        stage.setWidth(700);
        stage.setHeight(700);
        Scene scene = new Scene(getContent());
        stage.setScene(scene);
        stage.show();
    }
    
}

