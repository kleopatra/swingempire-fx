/*
 * Created on 18.03.2019
 *
 */
package de.swingempire.fx.application;

import java.util.concurrent.CountDownLatch;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Preloader example from download link given in
 * https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/
 * 
 * Also see https://docs.oracle.com/javafx/2/deployment/preloaders.htm
 */
public class MyApplication extends Application {

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    // Just a counter to create some delay while showing preloader.
    private static final int COUNT_LIMIT = 500000;

    private static int stepCount = 1;

    // Used to demonstrate step couns.
    public static String STEP() {
        return stepCount++ + ". ";
    }

    private Stage applicationStage;
    private boolean stopped;

    public static void main(String[] args) {
        // internal api
//        LauncherImpl.launchApplication(MyApplication.class, MyPreloader.class, args);
        // can be replaced with setting the system property
        System.setProperty("javafx.preloader", "de.swingempire.fx.application.MyPreloader");
        launch(args);
    }

    public MyApplication() {
        // Constructor is called after BEFORE_LOAD.
        System.out.println(MyApplication.STEP() + "MyApplication constructor called, thread: " + Thread.currentThread().getName());
    }

    public void requestStop() {
        stopped = true;
    }
    
    @Override
    public void init() throws Exception {
        System.out.println(MyApplication.STEP() + "MyApplication#init (doing some heavy lifting), thread: " + Thread.currentThread().getName());

        // Perform some heavy lifting (i.e. database start, check for application updates, etc. )
        for (int i = 0; i < COUNT_LIMIT; i++) {
            if (stopped) {
                break;
            }
            double progress = (100 * i) / COUNT_LIMIT;
            notifyPreloader(new Preloader.ProgressNotification(progress));
        }
        
        // does not work .. should it?
        // only interaction between launcher and fx thread directly supported?
//        CountDownLatch latch = new CountDownLatch(1);
//        Thread heavyLifting = new Thread(() -> {
//            try {
//                for (int i = 0; i < 100; i++) {
//                    notifyPreloader(new Preloader.ProgressNotification(i / 100));
//                    Thread.sleep(100);
//                }
//            } catch (Exception ex) {
//                
//            } finally {
//                latch.countDown();
//            }
//        });
//        heavyLifting.setName("lifting ...");
//        heavyLifting.start();
//        try {
//            latch.await();
//        } catch (InterruptedException ex) {
//            throw new RuntimeException("Unexpected exception: ", ex);
//        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(MyApplication.STEP() + "MyApplication#start (initialize and show primary application stage), thread: " + Thread.currentThread().getName());

        applicationStage = primaryStage;

        Duration timeToClose = Duration.seconds(2);
        String text = stopped ? "loading aborted - will close in " + timeToClose : "This is your application!";
        Label title = new Label(text);
        title.setTextAlignment(TextAlignment.CENTER);

        VBox root = new VBox(title);
        root.setAlignment(Pos.CENTER);

        // Create scene and show application stage.
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        applicationStage.setScene(scene);
        applicationStage.show();
        
        if (stopped) {
            Timeline closer = new Timeline(new KeyFrame(timeToClose, e -> applicationStage.close()));
            closer.play();
//            applicationStage.close();
        }
    }

}