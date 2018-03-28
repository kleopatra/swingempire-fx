/*
 * Created on 28.03.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.concurrent.ExecutionException;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Examples from Task/Service.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class WorkerExamples extends Application {

    private Parent createContent() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Value Change", createValueChangingPane())
               , new Tab("ObservableList", createObservableListPane())
                , new Tab("Simple", createSimpleLoopPane())
                , new Tab("Notification", createSimpleNotificationPane())
                , new Tab("Notification And Blocking", createSimpleNotificationAndBlockingPane())
                );
        return tabPane;
    }
    
    private Parent createValueChangingPane() {
        Task<Long> task = createValueChangingTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        Label messageLabel = new Label("Message: ");
        Label message = new Label();
        message.textProperty().bind(task.messageProperty());
        
        Label progressLabel = new Label("Value: ");
        Label progressAsText = new Label();
        progressAsText.textProperty().bind(task.valueProperty().asString());
        
//        ProgressBar progress = new ProgressBar();
//        progress.progressProperty().bind(task.progressProperty());
        
        Button start = new Button("Start");
        start.setOnAction(e -> {
            start.setDisable(true);
            thread.start();
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> task.cancel());
        
        int row = 0;
        GridPane grid = new GridPane();
        grid.add(messageLabel, 0, row);
        grid.add(message, 1, row++);
        grid.add(progressLabel, 0, row);
        grid.add(progressAsText, 1, row++);
//        grid.add(progress, 0, row++, 2, 1);
        VBox box = new VBox(10, grid, start, cancel);
        return box;
        
    }

    private Task<Long> createValueChangingTask() {
        Task<Long> task = new Task<Long>() {
            @Override protected Long call() throws Exception {
                long a = 0;
                long b = 1;
                for (long i = 0; i < Long.MAX_VALUE; i++) {
                    if (isCancelled()) {
                        updateValue(a);
                        updateMessage("Cancelled");
//                        return results;
                        break;
                    }
                    updateValue(a);
                    a += b;
                    b = a - b;
                }
                return a;
            }
        };
        return task;
    }
    /**
     * Example task building an observableList. 
     * 
     * Trying to get the result, endValue if succeeded or what's build up 
     * until cancelled. Can't use State listener: on cancelled, the value is
     * not yet updated. Binding the table's itemsProperty is working, but:
     * how/when to unbind?
     * 
     * @return
     */
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
        
        table.itemsProperty().bind(task.valueProperty());
        
        task.stateProperty().addListener((src, ov, nv) -> {
            // can't access get on cancelled
            if (Worker.State.SUCCEEDED == nv ) {// || Worker.State.CANCELLED == nv) {
                LOG.info("succeeded" + task.getValue());
//                try {
////                    table.setItems(task.get());
//                } catch (InterruptedException | ExecutionException e1) {
//                    e1.printStackTrace();
//                }
            } else if (Worker.State.CANCELLED == nv) {
                LOG.info("receiving cancelled " + task.getValue());
                // value still null
//                table.setItems(task.getValue());
//                table.itemsProperty().bind(task.valueProperty());
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
        
        int row = 0;
        GridPane grid = new GridPane();
        grid.add(messageLabel, 0, row);
        grid.add(message, 1, row++);
        grid.add(progressLabel, 0, row);
        grid.add(progressAsText, 1, row++);
        grid.add(progress, 0, row++, 2, 1);
        VBox box = new VBox(10, table, grid, start, cancel);
        return box;
   }
    
    /**
     * This was a bit pathological: value is only updated on cancel.
     * Now updating on all (?) terminating paths.
     * @return
     */
    private Task<ObservableList<Rectangle>> createObservableListTask() {
        Task<ObservableList<Rectangle>> task = new Task<ObservableList<Rectangle>>() {
            @Override protected ObservableList<Rectangle> call() throws Exception {
                updateMessage("Creating Rectangles ...");
                ObservableList<Rectangle> results = FXCollections.observableArrayList();
                for (int i=0; i<100; i++) {
                    if (isCancelled()) {
                        // when do we get here?
                        updateValue(results);
                        updateMessage("Cancelled");
//                        return results;
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
                            // calling task.cancel leads to here
                            LOG.info("interrupted: " + results);
                            // can do, but need to listen to valueProperty
                            // listening the cancelled of state won't help
                            // because it's not yet ready at the moment of receiving the notification
                            updateValue(results);
                            updateMessage("Cancelled");
//                            return results;
                            break;
                        }
                    }
                }
                updateValue(results);
                return results;
            }
        };
        return task;
    }
    
    private Parent createSimpleNotificationAndBlockingPane() {
        Task<Integer> task = createSimpleNotificationAndBlockingTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        Label messageLabel = new Label("Message: ");
        Label message = new Label();
        message.textProperty().bind(task.messageProperty());
        
        Label progressLabel = new Label("Progress: ");
        Label progressAsText = new Label();
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
        
        int row = 0;
        GridPane grid = new GridPane();
        grid.add(messageLabel, 0, row);
        grid.add(message, 1, row++);
        grid.add(progressLabel, 0, row);
        grid.add(progressAsText, 1, row++);
        grid.add(progress, 0, row++, 2, 1);
        VBox box = new VBox(10, grid, start, cancel);
        return box;
        
    }

    private Task<Integer> createSimpleNotificationAndBlockingTask() {
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                int iterations;
                for (iterations = 0; iterations < 1000; iterations++) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                        break;
                    }
                    updateMessage("Iteration " + iterations);
                    updateProgress(iterations, 1000);

                    // Now block the thread for a short time, but be sure
                    // to check the interrupted exception for cancellation!
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }
                    }
                }
                return iterations;
            }
        };
        return task;
    }
    
    private Parent createSimpleNotificationPane() {
        Task<Integer> task = createSimpleNotificationTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        Label messageLabel = new Label("Message: ");
        Label message = new Label();
        message.textProperty().bind(task.messageProperty());
        
        Label progressLabel = new Label("Progress: ");
        Label progressAsText = new Label();
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
        
        int row = 0;
        GridPane grid = new GridPane();
        grid.add(messageLabel, 0, row);
        grid.add(message, 1, row++);
        grid.add(progressLabel, 0, row);
        grid.add(progressAsText, 1, row++);
        grid.add(progress, 0, row++, 2, 1);
        VBox box = new VBox(10, grid, start, cancel);
        return box;
        
    }
    private Task<Integer> createSimpleNotificationTask() {
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                int iterations;
                for (iterations = 0; iterations < 10000000; iterations++) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                        break;
                    }
                    updateMessage("Iteration " + iterations);
                    updateProgress(iterations, 10000000);
                }
                return iterations;
            }
        };
        return task;
    }
    
    private Parent createSimpleLoopPane() {
        Task<Integer> task = createSimpleLoopTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        Button start = new Button("Start");
        start.setOnAction(e -> {
            start.setDisable(true);
            thread.start();
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> task.cancel());
        VBox box = new VBox(10, start, cancel);
        return box;
    }
    
    /**
     * Simple task: iterates for fixed #, queries cancel (as it must!).
     */
    private Task<Integer> createSimpleLoopTask() {
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                int iterations;
                for (iterations = 0; iterations < Integer.MAX_VALUE; iterations++) {
                    if (isCancelled()) {
                        break;
                    }
                    System.out.println("Iteration " + iterations);
                }
                return iterations;
            }
        };
        return task;
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
            .getLogger(WorkerExamples.class.getName());

}
