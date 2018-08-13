/*
 * Created on 11.08.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/51787549/203657
 * 
 * error was: incorrect impl of listChangeListener (missing c.next())
 * not sure why she couldn't implement the remove ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeToList extends Application {

    private ObservableList<Download> downloads = FXCollections.observableArrayList();
    @FXML private TreeView<Download> treeDownloads;

    private int count;
    
    private Parent createContent() {
        treeDownloads = new TreeView<>();
        treeDownloads.setRoot(new TreeItem<>(new Download("root")));
        initialize();
        
        Button add = new Button("add");
        add.setOnAction(e ->  {
            downloads.addAll(new Download("added" + count++));
        });
        
        Button remove = new Button("remove");
        remove.setOnAction(e -> {
            List<TreeItem<Download>> selected = treeDownloads.getSelectionModel().getSelectedItems();
            if (selected.isEmpty()) return;
            List<Download> downloadsToRemove = selected.stream()
                    .map(treeItem -> treeItem.getValue())
                    .collect(Collectors.toList());
            downloads.removeAll(downloadsToRemove);
        });
        HBox buttons = new HBox(10, add, remove);
        BorderPane content = new BorderPane(treeDownloads);
        content.setBottom(buttons);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeToList.class.getName());

    @FXML
    public void initialize() {
        treeDownloads.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeDownloads.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        treeDownloads.setShowRoot(false);

        downloads.addListener(new ListChangeListener<Download>() {
            @Override
            public void onChanged(Change<? extends Download> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        addDownloads(c.getAddedSubList());
                    }
                    if (c.wasRemoved()) {
                        removeDownloads(c.getRemoved());
                    }
                    
                }
            }
        });
        downloads.add(new Download("3847"));
        downloads.add(new Download("3567"));
        downloads.add(new Download("2357"));
    }

    private void addDownloads(List<? extends Download> downloads) {
        downloads.forEach(download -> {
            TreeItem<Download> treeItem = new TreeItem<>(download);
            treeDownloads.getRoot().getChildren().add(treeItem);
            new Thread(download::start).start();
        });
    }

    private void removeDownloads(List<? extends Download> downloads) {
        // remove treeitems from the treeview that hold these downloads
        List<TreeItem<Download>> treeItemsToRemove = treeDownloads.getRoot().getChildren().stream()
                .filter(treeItem -> downloads.contains(treeItem.getValue()))
                .collect(Collectors.toList());
        treeDownloads.getRoot().getChildren().removeAll(treeItemsToRemove);
    }

    
    public static class Download {

        private DoubleProperty progress = new SimpleDoubleProperty(0D);
        private StringProperty id = new SimpleStringProperty("");

        public Download(String id) {
            this.id.set(id);
        }

        public void start() {
            while (progress.getValue() < 1) {
                try {
                    Thread.sleep(1000);
                    progress.add(0.1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }      

        @Override
        public String toString() {
            return id.getValue();
        }
    }


}



