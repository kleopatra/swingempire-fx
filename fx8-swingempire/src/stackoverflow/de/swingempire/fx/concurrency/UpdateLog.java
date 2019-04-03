/*
 * Created on 31.03.2019
 *
 */
package de.swingempire.fx.concurrency;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Weird code from: https://stackoverflow.com/q/55438608/203657
 * cleaned up ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class UpdateLog extends Application {

    @Override
    public void start(Stage primaryStage) {
        Scheduler scheduler = new Scheduler();
        Scene scene = new Scene(scheduler.border, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        new Thread(scheduler).start();
    }
    public static void main(String[] args) {
        launch(args);
    }

    
    static class Scheduler implements Runnable{

        private static final int numberQueues = 3;
        static final int serviceTime = 10;
        static volatile int currentTime;
        public  BorderPane border = new BorderPane();
        public  TextArea logList = new TextArea(" Starting Logs...\n");

        public Scheduler() {
            currentTime = 0;
            border.setCenter(logList);
        }

        @Override
        public void run() {
            while (serviceTime > currentTime) {
                currentTime += 1;
                try {
                    writeToActivityLog(currentTime + " | Customer at queue" + currentTime % numberQueues);
                    System.out.println(currentTime + " | Customer at queue" + currentTime % numberQueues);
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public void writeToActivityLog(String logEntry){

            Platform.runLater(() -> {
                logList.appendText("\n" + logEntry);
            });
        }
    }


}
