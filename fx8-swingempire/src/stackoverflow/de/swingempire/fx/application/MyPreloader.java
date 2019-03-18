/*
 * Created on 18.03.2019
 *
 */
package de.swingempire.fx.application;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.ProgressNotification;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * How to report back to the application? F.i. if the preloader stage is closed
 * before the application is ready with the heavy lifting?
 * 
 *  
 * 
 */
public class MyPreloader extends Preloader {

    private static final double WIDTH = 400;
    private static final double HEIGHT = 400;

    MyApplication app;
    
    private Stage preloaderStage;
    private Scene scene;

    private Label progress;

    public MyPreloader() {
        // Constructor is called before everything.
        System.out.println(MyApplication.STEP() + "MyPreloader constructor called, thread: " + Thread.currentThread().getName());
    }

    @Override
    public void init() throws Exception {
        System.out.println(MyApplication.STEP() + "MyPreloader#init (could be used to initialize preloader view), thread: " + Thread.currentThread().getName());

        // If preloader has complex UI it's initialization can be done in MyPreloader#init
        Label title = new Label("Showing preloader stage!\nLoading, please wait...");
        title.setTextAlignment(TextAlignment.CENTER);
        progress = new Label("0%");
        
        VBox root = new VBox(title, progress);
        root.setAlignment(Pos.CENTER);
        
        scene = new Scene(root, WIDTH, HEIGHT);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(MyApplication.STEP() + "MyPreloader#start (showing preloader stage), thread: " + Thread.currentThread().getName());

        this.preloaderStage = primaryStage;

        preloaderStage.setOnCloseRequest(e -> {
            if (app != null) {
                try {
                    System.out.println("stopping app ...");
                    app.requestStop();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        // Set preloader scene and show stage.
        preloaderStage.setScene(scene);
        preloaderStage.show();
        
       
    }

    /**
     * Progress not delivered here ... only in appNotification
     */
    @Override
    public void handleProgressNotification(ProgressNotification info) {
//        progress.setText(info.getProgress() + "%");
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        // Handle application notification in this point (see MyApplication#init).
        if (info instanceof ProgressNotification) {
            progress.setText(((ProgressNotification) info).getProgress() + "%");
        } else {
            System.out.println("other: " + info);
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        // Handle state change notifications.
        StateChangeNotification.Type type = info.getType();
        System.out.println(info.getType());
        switch (type) {
            case BEFORE_LOAD:
                // Called after MyPreloader#start is called.
                System.out.println(MyApplication.STEP() + "BEFORE_LOAD" + info.getApplication());
                break;
            case BEFORE_INIT:
                // Called before MyApplication#init is called.
                System.out.println(MyApplication.STEP() + "BEFORE_INIT" + info.getApplication());
                this.app = (MyApplication) info.getApplication();
                break;
            case BEFORE_START:
                // Called after MyApplication#init and before MyApplication#start is called.
                System.out.println(MyApplication.STEP() + "BEFORE_START");

                preloaderStage.hide();
                break;
        }
    }


    
    
}