/*
 * Created on 03.09.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52139475/203657
 * select a single treeItem only 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeSingleSelectedCheckbox extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    
    public static class Dependant {

        String one;
        String two;
        public Dependant(String one, String two) {
            this.one = one;
            this.two = two;
        }
        
        
    }
    
    
    private List<Dependant> myList = new ArrayList();

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {

        CheckBoxTreeItem<String> rootItem = new CheckBoxTreeItem("Root");
        final List<CheckBoxTreeItem<String>> treeItems = new ArrayList(6);
        for (int i = 0; i < 6; i++) {
            CheckBoxTreeItem<String> item = new CheckBoxTreeItem("L0" + i + "");
            item.setIndependent(true);
            treeItems.add(item);
            myList.add(new Dependant("0" + i + "", "type1"));
        }
        rootItem.getChildren().addAll(treeItems);

        rootItem.setExpanded(true);
        rootItem.setIndependent(true);
        CheckBoxTreeItem<String> rootItem2 = new CheckBoxTreeItem("folder");
        final List<CheckBoxTreeItem<String>> treeItems2 = new ArrayList(6);
        for (int i = 0; i < 6; i++) {
            CheckBoxTreeItem<String> item = new CheckBoxTreeItem("L1" + i + "");
            item.setIndependent(true);
            treeItems2.add(item);
            myList.add(new Dependant("0" + i + "", "type2"));
        }
        rootItem2.getChildren().addAll(treeItems2);
        rootItem2.setIndependent(true);
        rootItem.getChildren().set(2, rootItem2);

        TreeView tree = new TreeView(rootItem);

        tree.setCellFactory((Object item) -> {

            final CheckBoxTreeCell<String> cell = new CheckBoxTreeCell();

//            cell.itemProperty().addListener((obs, s, s1) -> {
//
//                cell.disableProperty().unbind();
//                if (s1 != null && !s1.isEmpty()) {
//                    BooleanProperty prop = new SimpleBooleanProperty();
//                    prop.set((s1.equals("folder")));
//                    cell.disableProperty().bind(prop);
//                }
//            });
            return cell;
        });

        tree.setRoot(rootItem);

        StackPane root = new StackPane();
        root.getChildren().add(tree);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

}

