/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * https://javafx-jira.kenai.com/browse/RT-37395
 * 
 * Selecteditems on treeTable include null node when collapsing
 * 1. Run the below example.
 * 2. Select item "three".
 * 3. Expand item "two".
 * 4. Collapse item "two".
 */
public class SelectedTreeTableItemsRT_37385 extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // table items - 3 items, 2nd item has 2 children
        TreeItem<Item> root = new TreeItem<>();

        TreeItem<Item> two = new TreeItem<>(new Item("two"));
        two.getChildren().add(new TreeItem<>(new Item("childOne")));
        two.getChildren().add(new TreeItem<>(new Item("childTwo")));

        root.getChildren().add(new TreeItem<>(new Item("one")));
        root.getChildren().add(two);
        root.getChildren().add(new TreeItem<>(new Item("three")));

        // table columns - 1 column; name
        TreeTableColumn<Item, String> nameColumn = new TreeTableColumn<>("name");
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        // table
        TreeTableView<Item> table = new TreeTableView<>();
        table.setShowRoot(false);
        table.setRoot(root);
        table.getColumns().addAll(nameColumn);
        table.getSelectionModel().getSelectedItems().addListener(this::processListChange);

        // version / info labels
        Label osLabel = new Label(System.getProperty("os.name"));
        Label jvmLabel = new Label(
                System.getProperty("java.version") +
                        "-" + System.getProperty("java.vm.version") +
                        " (" + System.getProperty("os.arch") + ")"
        );

        // scene /stage
        primaryStage.setScene(new Scene(
                new BorderPane(
                        table,
                        null,
                        null,
                        new VBox(osLabel, jvmLabel),
                        null
                )
        ));

        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.setTitle("SelectedTreeTableItems");
        primaryStage.show();
    }

    private void processListChange(ListChangeListener.Change<? extends TreeItem<Item>> c) {
        System.out.println("Change event...");
        while (c.next()) {
            System.out.println(" Change:");

            if (c.wasRemoved()) {
                c.getRemoved().forEach(item -> {
                    if (item == null) {
                        System.out.println("  Removed: TreeItem was null.");
                    } else {
                        System.out.println("  Removed: " + item.getValue().getName());
                    }
                });
            }
            if (c.wasAdded()) {
                c.getAddedSubList().forEach(item ->
                        System.out.println("  Added: " + item.getValue().getName()));
            }
        }
    }

    public static class Item {
        private final StringProperty name = new SimpleStringProperty("name");
        public final ReadOnlyStringProperty nameProperty() {return name;}
        public final String getName() {return name.get();}

        public Item(String name) {
            this.name.set(name);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
