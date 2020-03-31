/*
 * Created on 31.03.2020
 *
 */
package de.swingempire.fx.concurrency;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Prepare answer to https://stackoverflow.com/q/60945467/203657
 * flashing table row background
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FxTransitionExample1 extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Create a green Rectangle
        Rectangle rect = new Rectangle(400, 200, Color.GREEN);
        // Create the HBox
        HBox root = new HBox(rect);
        // Set the Style-properties of the HBox
        root.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
                + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: blue;");

        // Creating the circle
//        Circle circle = new Circle(50);
//
//        // setting color and stroke of the cirlce
//        circle.setFill(Color.RED);
//        circle.setStroke(Color.BLACK);

        // Instantiating TranslateTransition class
        Duration duration = Duration.millis(5000);
//        double rate = 0.1;
//        TranslateTransition translate = new TranslateTransition(duration);
//        translate.setNode(circle);
//        translate.setToX(500);

//        Group transGroup = new Group(cir);
//        Pane transGroup = new Pane(circle);
//        circle.setTranslateX(0);

        Timeline timeline = new Timeline(new KeyFrame(duration));
//        timeline.setRate(rate);
        System.out.println(timeline.getTotalDuration());
        Label current = new Label();
//        current.textProperty().bind(timeline.currentTimeProperty().asString());

        Button start = new Button("start");
        start.setOnAction(e -> timeline.play());

//        Button connect = new Button("connect circle");
//        connect.setOnAction(e -> {
//            translate.stop();
//            translate.jumpTo(timeline.getCurrentTime());
//            translate.play();
//        });

        Slider slider = new Slider(0, 5, 0);
        timeline.currentTimeProperty().addListener((src, ov, nv) -> {
            slider.setValue(nv.toSeconds());
        });

        // Set up a fade-in and fade-out animation for the rectangle
        FadeTransition trans = new FadeTransition(duration, rect);
//        trans.setRate(rate);
        trans.setFromValue(1.0);
        trans.setToValue(0);
        // Let the animation run forever
//        trans.setCycleCount(FadeTransition.INDEFINITE);
        // Reverse direction on alternating cycles
//        trans.setAutoReverse(true);
        // Play the Animation

        Button connectFade = new Button("connect fade");
        connectFade.setOnAction(e -> {
            trans.stop();
            trans.jumpTo(timeline.getCurrentTime());
            trans.play();
        });

        BorderPane content = new BorderPane(root);
        content.setBottom(new HBox(10, current, start, connectFade));
        content.setTop(slider);
        // Create the Scene
        Scene scene = new Scene(content);
        // Add the Scene to the Stage
        stage.setScene(scene);
        // Set the Title of the Stage
        stage.setTitle("A Fade-in and Fade-out Transition Example");
        // Display the Stage
        stage.show();

    }

//    public static class MyAnimation extends Animation {
//
//        public MyAnimation() {
//            super();
//            // TODO Auto-generated constructor stub
//        }
//
//        public MyAnimation(double targetFramerate) {
//            super(targetFramerate);
//            // TODO Auto-generated constructor stub
//        }
//        
//    }
}

