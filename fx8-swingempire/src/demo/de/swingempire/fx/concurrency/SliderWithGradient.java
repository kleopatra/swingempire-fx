/*
 * Created on 13.02.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.net.URL;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48760606/203657
 * slider stuck to beginning of track, though its value being bound to
 * a random number
 * 
 * was:
 * - not on fx-thread (funnily seems to be working anyway)
 * - max was < random number
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SliderWithGradient extends Application {
    private static double START = 200;
    private static double END = 350;

    private static DoubleProperty propRandomNum = new SimpleDoubleProperty(START);
    @Override
    public void start(Stage primaryStage) {
        double startingValue = START;
        Slider slider = new Slider(START, END, startingValue);
        slider.valueProperty().bind(propRandomNum);
        
        slider.valueProperty().addListener((src, ov, nv) -> {
            System.out.println("random/nv " + propRandomNum.get() + "/" + nv + Platform.isFxApplicationThread());
        });

        slider.styleProperty().bind(Bindings.createStringBinding(() -> {
            double min = slider.getMin();
            double max = slider.getMax();
            double value = slider.getValue();

            return createSliderStyle(startingValue, min, max, value);

        }, slider.valueProperty()));

        VBox root = new VBox(5, slider);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 200);
        URL uri = getClass().getResource("sliderwithgradient.css");
//        scene.getStylesheets().add("sliderwithgradient.css");
        scene.getStylesheets().add(uri.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        ThreadRandom threads = new ThreadRandom();
        Thread t1 = new Thread(threads);
        t1.setDaemon(true);
        t1.start();

    }

    private String createSliderStyle(double startingValue, double min, double max, double value) {
        StringBuilder gradient = new StringBuilder("-slider-track-color: ");
        String defaultBG = "derive(-fx-control-inner-background, -5%) ";
        gradient.append("linear-gradient(to right, ").append(defaultBG).append("0%, ");

//        double valuePercent = 100.0 * (value - min) / (max - min);
//        double startingValuePercent = startingValue * 100.0;

        // Test
        value = propRandomNum.getValue();
        double open = 0.5;
        if(propRandomNum.getValue()>0){
            min = 200;
            max = 350;
            open = 230;
        }
        double valuePercent = 100.0 * (value - min) / (max - min);
        double startingValuePercent = 100.0 * (open - min) / (max - min);

        System.out.println("Random: " + propRandomNum.getValue());
        System.out.println("SVP:" + startingValuePercent);
        System.out.println("VP:" + valuePercent);
        System.out.println("Val:" + value);
        // End Test


        if (valuePercent > startingValuePercent) {
            gradient.append(defaultBG).append(startingValuePercent).append("%, ");
            gradient.append("green ").append(startingValuePercent).append("%, ");
            gradient.append("green ").append(valuePercent).append("%, ");
            gradient.append(defaultBG).append(valuePercent).append("%, ");
            gradient.append(defaultBG).append("100%); ");
        } else {
            gradient.append(defaultBG).append(valuePercent).append("%, ");
            gradient.append("red ").append(valuePercent).append("%, ");
            gradient.append("red ").append(startingValuePercent).append("%, ");
            gradient.append(defaultBG).append(startingValuePercent).append("%, ");
            gradient.append(defaultBG).append("100%); ");
        }
        return gradient.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void genRandomNumber() {
        Random random = new Random();

        //get the range, casting to long to avoid overflow problems
        long range = (long) END - (long) START + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long) (range * random.nextDouble());
        double randomNumber = (int) (fraction + START);

        propRandomNum.set(randomNumber);
        Platform.runLater(() -> {
        });
    }

    private static class ThreadRandom implements Runnable{

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                genRandomNumber();
//                propRandomNum.addListener((observable, oldValue, newValue) -> slider.setValue((double) newValue));
            }
        }
    }
}

