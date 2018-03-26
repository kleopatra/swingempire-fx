/*
 * Created on 05.03.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * ProgressbarCell not updating.
 * https://stackoverflow.com/q/49069832/203657
 * 
 * was user-error:
 * - incorrect property name
 * - incorrect looking for tasks to run (no: using state doesn't help)
 *   BUT: use executor, don't try to manage the threads manually
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class UploadSimulator extends Application {

    private TableView<UploadTesk> sampleTable = new TableView<>();
    private TableColumn<UploadTesk, String> colName = new TableColumn<>("filename");
    private TableColumn<UploadTesk, Double> colProgress = new TableColumn<>("Progress");
    private TableColumn<UploadTesk, String> colStatus = new TableColumn<>("Status");    
    final Button action = new Button("Upload");
    ExecutorService executor;
    int allowedThreads = 2;

    private final ObservableList<UploadTesk> data
            = FXCollections.observableArrayList(
                    new UploadTesk("C:\\music\\one.mp3"),
                    new UploadTesk("C:\\music\\two.mp3"),
                    new UploadTesk("C:\\music\\three.mp3"),
                    new UploadTesk("C:\\music\\for.mp3"),
                    new UploadTesk("C:\\music\\five.mp3")
            );

    @Override
    public void start(Stage stage) throws Exception {
        executor = Executors.newFixedThreadPool(2);
        colName.setCellValueFactory(new PropertyValueFactory<>("filename"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("state"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colProgress.setCellFactory(ProgressBarTableCell.<UploadTesk>forTableColumn());

        sampleTable.getColumns().addAll(colName, colProgress, colStatus);
        StackPane root = new StackPane();
        root.getChildren().add(sampleTable);
        root.getChildren().add(action);
        sampleTable.getItems().addAll(data);

        action.setOnAction((ActionEvent t) -> {
            startUpload();
        });

        Scene scene = new Scene(root, 300, 300);
        stage.setScene(scene);
//        stage.setResizable(false);
        stage.show();
    }

    public void startUpload() {
        sampleTable.getItems().stream().forEach(task -> executor.execute(task));
        executor.shutdown();
//        executor.invokeAll(sampleTable.getItems());
//        Task uploadFilesTask = new Task<Void>() {
//            int threadSleepTimeLong = 1000;
//
//            @Override
//            public Void call() throws Exception {
//                int activeThreadCount = ((ThreadPoolExecutor) executor).getActiveCount();
//                while (true) {
//                    for (int i = 0; i < allowedThreads - activeThreadCount; i++) {
//                        try {
////                            UploadTesk uploadTesk = sampleTable.getItems().stream().filter(x -> x.progressProperty().get() <= 0).findFirst().get();
//                            UploadTesk uploadTesk = sampleTable.getItems().stream().filter(x -> x.getState() == Worker.State.READY).findFirst().get();
//                            if (uploadTesk != null) {
//                                LOG.info("looping: " + i + "foundTask: " + uploadTesk);
//                                uploadTesk.setStatus("Uploading");
//                                executor.execute(uploadTesk);
//                            }
//                        } catch (Exception ex) {
//
//                        }
//                    }
//                    Thread.sleep(1000);
//
//                    activeThreadCount = ((ThreadPoolExecutor) executor).getActiveCount();
//                    LOG.info("active: " + activeThreadCount);
//                    if (activeThreadCount <= 0 && !sampleTable.getItems().stream().filter(x -> x.progressProperty().get() <= 1).findFirst().isPresent()) {
//                        break;
//                    }
//                }
//                return null;
//            }
//
//        };
//
//        new Thread(uploadFilesTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
    

    public static class UploadTesk extends Task<Void> {

        private String filename;
        private double currentProgress;

        public String getFilename() {
            return filename;
        }

        public UploadTesk(String fileName) {
            this.filename = fileName;
            this.updateProgress(0, 1);
        }

        public void setProgress(double progress) {
            this.currentProgress = progress;
                updateProgress(this.currentProgress, 1);
                Platform.runLater(() -> {
            });
        }

        public void setStatus(String status) {
                this.updateMessage(status);
                Platform.runLater(() -> {
            });
        }

        @Override
        protected Void call() throws Exception {
            while (currentProgress < 1) {
                Thread.sleep(1000);
                setProgress(currentProgress + 0.1);
            }
            setStatus("Done");
            return null;
        }

        @Override
        public String toString() {
            return filename + " " + getState();
        }
        
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(UploadSimulator.class.getName());
}

