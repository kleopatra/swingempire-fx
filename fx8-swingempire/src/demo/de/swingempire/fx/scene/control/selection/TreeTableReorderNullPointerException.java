/*
 * Created on 08.04.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-38888
 */
public class TreeTableReorderNullPointerException extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeTableView<Person> treeTable = new TreeTableView<>();
        treeTable.setShowRoot(false);

        TreeTableColumn<Person, String> column = new TreeTableColumn<>("Name");
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>("lastName"));
        treeTable.getColumns().add(column);

        TreeItem<Person> root = new TreeItem<>(new Person(null,"root",  null));
        treeTable.setRoot(root);

        TreeItem<Person> one = new TreeItem<>(new Person(null,"one", null));
        root.getChildren().add(one);

        TreeItem<Person> two = new TreeItem<>(new Person(null,"two", null));
        root.getChildren().add(two);

        // Select two, then move two from root to one.
        treeTable.getSelectionModel().select(two);
        root.getChildren().remove(two);
        System.out.println("selected after remove: " 
                + treeTable.getSelectionModel().getSelectedItem()
                + treeTable.getSelectionModel().getSelectedIndex()
                );
        one.getChildren().add(two);
        one.setExpanded(true);

        primaryStage.setScene(new Scene(treeTable));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
