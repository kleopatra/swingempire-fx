/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;


import java.util.logging.Logger;

import de.swingempire.fx.scene.control.cell.CellDecorator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jdk.internal.jline.internal.Log;

/**
 * TreeView: receives cancel event on commit
 * https://bugs.openjdk.java.net/browse/JDK-8124615
 * TreeView: F2 fires cancel
 * https://bugs.openjdk.java.net/browse/JDK-8123783
 * 
 * Bug still for ListView, but for a different reason:
 * default handler replaces value in items -> skin cancels edit  
 * 
 * Modified TVEvents to handle ListView
 */
public class ListViewCustomCellExample extends Application {
    
    public static class MyListCell<T> extends ListCell<T> implements CellDecorator<T> {
        @Override 
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            updateItemNode(item, empty);
        }
    }
        
    @Override
    public void start(Stage stage) throws Exception {
        ObservableList<String> children = FXCollections.observableArrayList("item1", "item2");
        ListView<String> listView = new ListView<>(children);
        listView.setEditable(true);
        // default factory set by skin
//        LOG.info("" + listView.getCellFactory());
        listView.setCellFactory(e -> new MyListCell<>());

        VBox vBox = new VBox(10d);
        vBox.getChildren().addAll();

        HBox hBox = new HBox(15d);
        hBox.getChildren().addAll(listView, vBox);
        Scene scene = new Scene(hBox, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListViewCustomCellExample.class.getName());
}
