/*
 * Created on 05.08.2019
 *
 */
package de.swingempire.fx.concurrency;

import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * https://stackoverflow.com/q/57322258/203657
 * simple concurrency: add/remove nodes
 * 
 * unclear what exactly is the context: something like a timer or 
 * a long-running background task?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ConcurrencyExample extends Application {

    static int test = 0;
    Path path;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Text text = new Text("dummy");

        text.setText("qqq");
        text.setLayoutX(450);
        text.setLayoutY(800);

//        Pane root = new Pane(text);
        Pane root = new Pane();
        BorderPane content = new BorderPane(root);
        content.setTop(text);
        Scene scene = new Scene(content, 500, 500);

        primaryStage.setScene(scene);
        primaryStage.show();


        //solution by OP: use util Timer
        // don't, fx comes with extensive concurrency support!
//        new Timer().schedule(
//             new TimerTask() {
//                 @Override
//                 public void run() {
//                     System.out.println("ping " + ++test);
//                     // this doesn't blow, but is wrong nevertheless!
//                     text.setText(" " + test);
//                     //here is the solution
//                     Platform.runLater(new Runnable() {
//                         @Override public void run() {
//                             root.getChildren().remove(path);
//                             test++;
//                             //path = makePath(coordinates.subList(test, test+5));
//                             path = makeTestPath();
//                             root.getChildren().add(path);
//                         }
//                     });
//
//                 }
//             }, 0, 1000);
//
        
        // use Task in a thread, manual coding
//        Task<Void> task = new Task<>() {
//
//            @Override
//            protected Void call() throws Exception {
//                for (int i = 0; i < 10; i++) {
//                    Thread.sleep(500);
//                    Path npath = makeTestPath();
//                    Platform.runLater(() -> {
//                        root.getChildren().remove(path);
//                        test++;
//                        path = npath;
//                        root.getChildren().add(path);
//                    });
//                }
//                return null;
//            }
//            
//        };
//        Thread thread = new Thread(task);
//        thread.start();
        
        // use task with notification
//        Task<Path> task = new Task<>() {
//
//            @Override
//            protected Path call() throws Exception {
//                int max = 50;
//                for (int i = 0; i < max; i++) {
//                    Thread.sleep(500);
//                    Path path = makeTestPath();
//                    updateProgress(i, max);
//                    updateValue(path);
//                }
//                return null;
//            }
//            
//        };
//        
//        task.valueProperty().addListener((src, ov, nv) -> {
//            root.getChildren().remove(ov);
//            root.getChildren().add(nv);
//        });
//        
//        task.progressProperty().addListener((src, ov, nv) -> {
//            text.setText("progress: " + nv);
//        });
//        
//        Thread thread = new Thread(task);
//        thread.setDaemon(true);
//        thread.start();
        
        // use timeline with onFinished handler - possible if the work
        // done is not long-running - so probably not an option
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(500),
                e -> {
                   root.getChildren().remove(path);
                   path = makeTestPath();
                   root.getChildren().add(path);
                }
                ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
    }

    public Path makeTestPath() {
        Path path = new Path();
        path.setStroke(Color.RED);
        path.getElements().add(new MoveTo(getRandomNumberInRange(1, 500), getRandomNumberInRange(1, 500)));
        for (int i = 0; i < 10; i++) {
            path.getElements().add(new LineTo(getRandomNumberInRange(1, 500), getRandomNumberInRange(1, 500)));
        }
        return path;
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}

