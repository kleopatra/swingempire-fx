/*
 * Created on 24.09.2015
 *
 */
package de.swingempire.fx.concurrency;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * Minimal example to bind to a Task's progress.
 * http://stackoverflow.com/q/32757069/203657
 * @author Jeanette Winzenburg, Berlin
 * 
 * SO_FAQ
 */
public class ProgressTask extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    /**
     * @return
     */
    private Parent getContent() {
        ProgressBar bar = new ProgressBar(0);
        Button button = new Button("Start");
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                long max = 137;
                for (int i = 0; i <= max; i++) {
                    updateProgress(i, max);
                    Thread.sleep(100);
                }
                return null;
            }
            
        };
        button.setOnAction(e -> {
            // a task is not reusable, disable
            button.setDisable(true);
            // bind "late" to not get an initial indeterminate PB
            bar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
        });
        VBox pane = new VBox(10, bar, button);
        return pane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
