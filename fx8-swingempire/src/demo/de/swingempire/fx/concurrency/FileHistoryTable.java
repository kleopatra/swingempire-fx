/*
 * Created on 06.04.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Simple driver for basic fileTask
 * @author Jeanette Winzenburg, Berlin
 */
public class FileHistoryTable extends Application {

    private static String DIR = "C:\\Temp";
    
    private Parent createContent() throws Exception {
        File file = new File(DIR);
        String absolutePath = file.getAbsolutePath();
        LOG.info("" + absolutePath);
        FileTask task = new FileTask(file);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        
        TableView<WatchEvent<Path>> table = new TableView<>();
        TableColumn<WatchEvent<Path>, Path> pathColumn = new TableColumn<>("Context");
        pathColumn.setCellValueFactory(v -> new SimpleObjectProperty<>(v.getValue().context()));
        
        TableColumn<WatchEvent<Path>, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(v -> new SimpleObjectProperty<>(v.getValue().count()));
        
        TableColumn<WatchEvent<Path>, WatchEvent.Kind<Path>> kindColumn = new TableColumn<>("Count");
        kindColumn.setCellValueFactory(v -> new SimpleObjectProperty<>(v.getValue().kind()));
        
        table.getColumns().addAll(pathColumn, countColumn, kindColumn);
        
        task.valueProperty().addListener((src, ov, nv) -> {
            table.getItems().addAll(nv);
        });
        
        Label label = new Label("Watching: " + absolutePath);
        LOG.info("files: " + Arrays.asList(file.listFiles()));
        HBox top = new HBox(10, label);
        
        Button start = new Button("Start");
        start.setOnAction(e -> {
            start.setDisable(true);
            thread.start();
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> task.cancel());
        cancel.disableProperty().bind(task.runningProperty().not());
        
        HBox buttons = new HBox(10, start, cancel);
        BorderPane content = new BorderPane(table);
        content.setTop(top);
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
            .getLogger(FileHistoryTable.class.getName());

}
