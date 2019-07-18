/*
 * Created on 17.07.2019
 *
 */
package de.swingempire.fx.concurrency;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * https://stackoverflow.com/q/57066959/203657
 * on keeping a key pressed, move rect (player) only every xx ms
 * 
 * This is a very low-level solution by slaw. Original question was using a 
 * player with methods to move: decision should be there.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class KeyPressedWithTimer extends Application {

    private static final long THRESHOLD = 500_000_000L; // 500 ms

    public static class Player {
        private Rectangle rect;
        private Timeline left;
        private TranslateTransition leftTrans;
        
        public Player() {
            rect = new Rectangle(25, 25, Color.DODGERBLUE);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1);

            left = new Timeline();
//            left.setRate(5);
//            left.setAutoReverse(true);
//            left.setCycleCount(2);
//            left.setCycleCount(10000);
            left.getKeyFrames().add(new KeyFrame(Duration.millis(5000),
                    new KeyValue(rect.translateXProperty(), -100)
//                    ,new KeyValue(rect.translateXProperty(), -100, Interpolator.DISCRETE)
                    ));
            
            leftTrans = new TranslateTransition(Duration.millis(5000), rect);
            leftTrans.setByX(-50);
        }
        
        public void moveLeft() {
//            left.play();
            leftTrans.play();
        }
        
        public void stopLeft() {
//            left.pause();
            leftTrans.pause();
        }
    }
    
    private Player player;
    private long lastMoveNanos;
    private Rectangle rect;

    @Override
    public void start(Stage primaryStage) {
        player = new Player();
        rect = player.rect;
                
        var root = new Group(rect);
        root.setOnKeyPressed(this::handleKeyPressed);
        root.setOnKeyReleased(this::keyReleased);
        var scene = new Scene(root, 600, 400);
        rect.setX(scene.getWidth() / 2 - rect.getWidth() / 2);
        rect.setY(scene.getHeight() / 2 - rect.getHeight() / 2);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        root.requestFocus();
    }

    private void keyReleased(KeyEvent event) {
        if (event.getCode().isArrowKey()) {
            event.consume();

            if (event.getCode() == KeyCode.LEFT) {
                player.stopLeft();
                return;
            }
        }
    }
    
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode().isArrowKey()) {
            event.consume();

            if (event.getCode() == KeyCode.LEFT) {
                player.moveLeft();
                return;
            }
            
            long now = System.nanoTime();
            if (lastMoveNanos <= 0L || now - lastMoveNanos >= THRESHOLD) {
                switch (event.getCode()) {
                    case UP:
                        rect.setY(rect.getY() - rect.getHeight());
                        break;
                    case DOWN:
                        rect.setY(rect.getY() + rect.getHeight());
                        break;
                    case LEFT:
                        rect.setX(rect.getX() - rect.getWidth());
                        break;
                    case RIGHT:
                        rect.setX(rect.getX() + rect.getWidth());
                        break;
                    default:
                        throw new AssertionError();
                }
                lastMoveNanos = now;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

