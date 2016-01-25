/*
 * Created on 25.01.2016
 *
 */
package de.swingempire.fx.animation;


import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * http://stackoverflow.com/q/34985943/203657
 * Delay text output
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DelayedText extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        TextField field = new TextField();
        Button button = new Button("start");
        button.setOnAction(
//           createLowLevel(field)
             createTimeline(field)   
        );
        VBox content = new VBox(10., field, button);
        return content;
    }

    // PENDING JW: need to dig deeper - doesn't look quite right
    protected EventHandler<ActionEvent> createTimeline(TextField field) {
        return e -> {
            Timeline tl = new Timeline();
            KeyFrame kf = new KeyFrame(Duration.seconds(1.), 
                    new KeyValue(field.textProperty(), "Start")
                    );
            // duration seems to be the summed up value?
            KeyFrame kf1 = new KeyFrame(Duration.seconds(2.), 
                    new KeyValue(field.textProperty(), "End")
                    );
            tl.getKeyFrames().setAll(kf, kf1);
            tl.play();
        };
    }
    protected EventHandler<ActionEvent> createLowLevel(TextField field) {
        return e -> {
            Thread t = new Thread(() -> {
                Platform.runLater(() -> field.setText("START"));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    
                }
                Platform.runLater(() -> field.setText("END"));
            });

            t.start();
        };
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
