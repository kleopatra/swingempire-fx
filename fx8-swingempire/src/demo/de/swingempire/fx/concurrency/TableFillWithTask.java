/*
 * Created on 28.03.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Use-case: 
 * - want to build items (for table, f.i.) in background
 * - want to get results, either complete or until cancelled
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableFillWithTask extends Application {

    private Parent createObservableListPane() {
        Task<ObservableList<Rectangle>> task = createObservableListTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        
        TableView<Rectangle> table = new TableView<>();
        TableColumn<Rectangle, Double> xCol = new TableColumn<>("X");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Rectangle, Double> yCol = new TableColumn<>("Y");
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        table.getColumns().addAll(xCol, yCol);
        
        // working ... but when to unbind?
        table.itemsProperty().bind(task.valueProperty());
        
        
        // could manually register a listener ... but then I don't need the binding 
        //        task.valueProperty().addListener((src, ov, nv) -> {
        //            if (nv != null) {
        //                table.itemsProperty().unbind();
        //            }
        //        });
        task.stateProperty().addListener((src, ov, nv) -> {
            if (Worker.State.SUCCEEDED == nv ) {
                LOG.info("succeeded" + task.getValue());
                 table.itemsProperty().unbind();
            } else if (Worker.State.CANCELLED == nv) {
                LOG.info("receiving cancelled " + task.getValue());
                // can't unbind here, value not yet updated
                // table.itemsProperty().unbind();
            } 
        });
        
        Label messageLabel = new Label("Message: ");
        Label message = new Label();
        message.textProperty().bind(task.messageProperty());
        
        Label progressAsText = new Label();
        Label progressLabel = new Label("Progress: ");
        progressAsText.textProperty().bind(task.progressProperty().asString());
        
        ProgressBar progress = new ProgressBar();
        progress.progressProperty().bind(task.progressProperty());
        
        Button start = new Button("Start");
        start.setOnAction(e -> {
            start.setDisable(true);
            thread.start();
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> task.cancel());
        cancel.disableProperty().bind(task.runningProperty().not());
        
        Button debug = new Button("items bound?");
        debug.setOnAction(e -> LOG.info("bound? " + table.itemsProperty().isBound()));
        
        int row = 0;
        GridPane grid = new GridPane();
        grid.add(table, 0, row++, 20, 1);
        grid.add(messageLabel, 0, row);
        grid.add(message, 1, row++);
        grid.add(progressLabel, 0, row);
        grid.add(progressAsText, 1, row++);
        grid.add(progress, 0, row++, 2, 1);
        grid.add(start, 0, row);
        grid.add(cancel, 1, row++);
        grid.add(debug, 0, row++);
        return grid;
   }

    private Task<ObservableList<Rectangle>> createObservableListTask() {
        Task<ObservableList<Rectangle>> task = new Task<ObservableList<Rectangle>>() {
            @Override protected ObservableList<Rectangle> call() throws Exception {
                updateMessage("Creating Rectangles ...");
                ObservableList<Rectangle> results = FXCollections.observableArrayList();
                String message = "finished";
                for (int i=0; i<100; i++) {
                    if (isCancelled()) {
                        // when do we get here?
                        message = "cancelled";
                        break;
                    }
                    Rectangle r = new Rectangle(10, 10);
                    r.setX(10 * i);
                    results.add(r);
                    updateProgress(i, 100);
                    // Now block the thread for a short time, but be sure
                    // to check the interrupted exception for cancellation!
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            message = "interrupted";
                            break;
                        }
                    }
                }
                updateValue(results);
                updateMessage(message);
                return results;
            }
           
        };
        return task;
    }

    private Parent createContent() {
        Parent pane = createObservableListPane();
        BorderPane content = new BorderPane(pane);
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
            .getLogger(TableFillWithTask.class.getName());

}
