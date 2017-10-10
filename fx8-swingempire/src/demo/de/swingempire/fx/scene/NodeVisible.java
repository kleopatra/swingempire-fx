/*
 * Created on 10.10.2017
 *
 */
package de.swingempire.fx.scene;

import com.sun.javafx.scene.NodeHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Missing public api to detect whether a node is actually "visible", that
 * is detectable by users.
 * 
 * https://stackoverflow.com/q/43887427/203657
 * 
 */
public class NodeVisible extends Application {

    /*
     * Text is not guarded against fx-thread
     */
    private Text text1 = new Text();
    private Text text2 = new Text();
    /*
     * Label is guarded against fx-thread
     */
//    private Label text1 = new Label();
//    private Label text2 = new Label();

    public static void main(String[] args) {
        NodeVisible.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        TabPane root = new TabPane();

        VBox box1 = new VBox();
        text1.setText("Hello World!");
        text1.textProperty().addListener((observable, oldValue,     newValue) -> {
            System.out.println("text1 changed from " + oldValue + " to " + newValue);
        });
        box1.getChildren().addAll(text1);

        Tab tab1 = new Tab("Tab 1");
        tab1.setContent(box1);

        VBox box2 = new VBox();
        text2.setText("Another Hello World!");
        text2.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("text2 changed from " + oldValue + " to " + newValue);
        });
        box2.getChildren().add(text2);

        Tab tab2 = new Tab("Tab 2");
        tab2.setContent(box2);

        root.getTabs().addAll(tab1, tab2);

        Task<Void> task = new Task<Void>() {
            /* (non-Javadoc)
             * @see javafx.concurrent.Task#call()
             */
            @Override
            protected Void call() throws Exception {
                final String oldText = "Hello World!";
                final String newText = "New Hello World!";
                while (true) {
//                    if (text1.isVisible()) {
                    if (isTreeVisible(text1)) {
                        if (text1.getText().equals(oldText)) {
                            text1.setText(newText);
                        } else {
                            text1.setText(oldText);
                        }
                    }

                    if (isTreeVisible(text2)) {
//                    if (text2.isVisible()) {
                        if (text2.getText().equals(oldText)) {
                            text2.setText(newText);
                        } else {
                            text2.setText(oldText);
                        }
                    }
                    System.out.println("tab2 visible: " + tab2.getContent().isVisible());
                    Thread.sleep(2000);
                }
            }

        };

        stage.setScene(new Scene(root));
        stage.setWidth(200);
        stage.setHeight(200);
        stage.setTitle("JavaFX 9 Application");
        stage.show();

        Thread thread = new Thread(task, "Task");
        thread.setDaemon(true);
        thread.start();
    }

    boolean isTreeVisible(Node node) {
//        return NodeHelper.isTreeVisible(node);
        return NodeHelper.isTreeVisible(node);
    }
}

