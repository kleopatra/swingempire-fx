/*
 * Created on 28.03.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Use-case: 
 * - want to build items (for table, f.i.) in background
 * - want to get results, either complete or until cancelled
 * 
 * Implementation details in Task:
 * - delegate to TaskCallable
 * - call implemented to get result from task 
 * - in all states (?) except cancelled: first updates value with result, then updates state
 * - if cancelled: do nothing except returning null (no update of value! nor state!) -
 *   state update handled where?
 * - no way to work around (tried custom task impl as c&p with handling different)
 * 
 * this is used in SO question
 * https://stackoverflow.com/q/49670785/203657
 * 
 * suggestions:
 * - have a look at project jdeferred: https://github.com/jdeferred/jdeferred
 * - custom task impl with additional logic (answer by fabian)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
    public class TaskValueBinding extends Application {
    
        private Parent createListPane() {
//            Task<ObservableList<Rectangle>> task = createListTask();
            PartialResultTask<ObservableList<Rectangle>> task = createPartialResultTask();
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
            
            task.setOnDone(() -> {
                table.itemsProperty().unbind();
                LOG.info("unbound!");
                
            });
            task.stateProperty().addListener((src, ov, nv) -> {
                if (Worker.State.SUCCEEDED == nv ) {
                    // this is fine because implementation in TaskCallable first 
                    // updates the value (with the result it got from T call())
                    // then updates state
                    LOG.info("succeeded" + task.getValue());
//                     table.itemsProperty().unbind();
                } else if (Worker.State.CANCELLED == nv) {
                    LOG.info("receiving cancelled " + task.getValue());
                    // can't unbind here, value not yet updated
                    //   table.itemsProperty().unbind();
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
            
            return grid;
       }
    
        private Task<ObservableList<Rectangle>> createListTask() {
            Task<ObservableList<Rectangle>> task = new Task<ObservableList<Rectangle>>() {
                @Override 
                protected ObservableList<Rectangle> call() throws Exception {
                    updateMessage("Creating Rectangles ...");
                    ObservableList<Rectangle> results = FXCollections.observableArrayList();
                    String message = "finished";
                    int count = 10;
                    for (int i=0; i<=count; i++) {
                        if (isCancelled()) {
                            updateValue(results);
                            // when do we get here?
                            message = "cancelled";
                            break;
                        }
                        Rectangle r = new Rectangle(10, 10);
                        r.setX(10 * i);
                        results.add(r);
                        updateProgress(i, count);
                        // Now block the thread for a short time, but be sure
                        // to check the interrupted exception for cancellation!
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException interrupted) {
                            if (isCancelled()) {
                                updateValue(results);
                                message = "interrupted";
                                break;
                            }
                        }
                    }
                    updateMessage(message);
                    return results;
                }
               
            };
            return task;
        }
    
        public PartialResultTask<ObservableList<Rectangle>> createPartialResultTask() {
            PartialResultTask<ObservableList<Rectangle>> task = new PartialResultTask<ObservableList<Rectangle>>() {

                @Override
                protected ObservableList<Rectangle> calculateResult() throws Exception {updateMessage("Creating Rectangles ...");
                    ObservableList<Rectangle> results = FXCollections.observableArrayList();
                    int count = 10;
                    for (int i = 0; !isCancelled() && i <= count; i++) {
                        Rectangle r = new Rectangle(10, 10);
                        r.setX(10 * i);
                        results.add(r);
                        updateProgress(i, count);
                        // Now block the thread for a short time, but be sure
                        // to check the interrupted exception for cancellation!
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException interrupted) {
                        }
                    }
                    updateMessage(isCancelled() ? "canceled" : "finished");
                    return results;
                }

            };
            return task;
        }
        
        /**
         * Task with additional logic pass partial results.
         * 
         * @author fabian https://stackoverflow.com/a/49672331/203657
         */
        public abstract class PartialResultTask<T> extends Task<T> {

            // handler triggered after last change of value
            private Runnable onDone;

            public Runnable getOnDone() {
                return onDone;
            }

            public void setOnDone(Runnable onDone) {
                this.onDone = onDone;
            }

            protected abstract T calculateResult() throws Exception;

            private void onDone() {
                if (onDone != null) {
                    Platform.runLater(onDone);
                }
            }

            @Override
            protected final T call() throws Exception {
                try {
                    T result = calculateResult();
                    updateValue(result); // update value to the final value
                    onDone();
                    return result;
                } catch (Exception ex) {
                    onDone();
                    throw ex;
                }
            }

        }
        
        @Override
        public void start(Stage stage) throws Exception {
            stage.setScene(new Scene(createListPane()));
            stage.setTitle(FXUtils.version());
            stage.show();
        }
    
        public static void main(String[] args) {
            launch(args);
        }
    
        @SuppressWarnings("unused")
        private static final Logger LOG = Logger
                .getLogger(TaskValueBinding.class.getName());
    
    }