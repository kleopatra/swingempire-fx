/*
 * Created on 18.04.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


public class Tester extends Application {

    private int dx, x = 150, y = 470, projectileSpeed = 10;
    private int counter = 0, spawnTime = 180, enemySpeed = 4;
    private boolean goLeft, goRight, isShooting;

    public final static int APP_WIDTH = 300;
    public final static int APP_HEIGHT = 500;

    private Pane root;
    private Scene scene;

    private Rectangle projectile;
    private Circle player = new Circle(x, y, 10, Color.RED);
    private Rectangle enemy;
    private ArrayList<Rectangle> projectiles = new ArrayList();
    private ArrayList<Rectangle> enemies = new ArrayList();

    @Override
    public void start(Stage primaryStage) {

        root = new Pane();
        scene = new Scene(root, APP_WIDTH, APP_HEIGHT, Color.GHOSTWHITE);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        root.getChildren().addAll(player);
        controls();
        loop();
    }

    private void controls() {

        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();

            switch (key) {
                case LEFT:
                    goLeft = true;
                    break;
                case RIGHT:
                    goRight = true;
                    break;
                case SPACE:
                    projectiles.add(projectile = new Rectangle(3, 3, Color.BLUE));
                    projectile.relocate(x + player.getRadius(), y);
                    root.getChildren().add(projectile);
                    break;
            }

        });
        scene.setOnKeyReleased(event -> {
            KeyCode key = event.getCode();

            switch (key) {
                case LEFT:
                    goLeft = false;
                    break;
                case RIGHT:
                    goRight = false;
                    break;
                case SPACE:
                    isShooting = false;
                    break;
            }

        });

    }

    private void shoot() {
        for (int i = 0; i < projectiles.size(); ++i) {
            if (projectiles.get(i).getLayoutY() > (root.getBoundsInParent().getMinY() - projectile.getHeight())) {
                projectiles.get(i).relocate(projectiles.get(i).getLayoutX(), (projectiles.get(i).getLayoutY() - projectileSpeed));
            } else {
                projectiles.remove(i);
                root.getChildren().remove(i);
            }
        }

    }

    private void spawnEnemy() {

        double spawnPosition = Math.random();

        int eWidth = 20;
        int eHeight = 40;
        double ex = (APP_WIDTH - eWidth) * spawnPosition;
        int ey = (int) (root.getBoundsInParent().getMinY());

        if (counter % spawnTime == 0) {
            enemies.add(enemy = new Rectangle(ex, ey, eWidth, eHeight));
            root.getChildren().add(enemy);
            System.out.println("enemy: " + enemy + enemy.getBoundsInParent());
        }
    }

    public void moveEnemy() {
        for (int i = 0; i < enemies.size(); ++i) {
//            System.out.println(enemies.get(i).getLayoutX() + "");
            if (enemies.get(i).getLayoutY() > (root.getBoundsInParent().getMinX() + enemy.getWidth())) {
                enemies.get(i).relocate(enemies.get(i).getLayoutX(), (enemies.get(i).getLayoutY() + enemySpeed));
            } else {
//                enemies.remove(i);

            }
        }
    }

    private void loop() {

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

//                controls();
                if (goLeft) {
                    dx = -5;
                }
                if (goRight) {
                    dx = 5;
                }
                if (!goLeft && !goRight) {
                    dx = 0;
                }
                player.relocate(x += dx, y);
                shoot();

                counter++;
                spawnEnemy();
                moveEnemy();
            }
        };
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

