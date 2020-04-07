/*
 * Created on 02.04.2020
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60978134/203657
 * PopupLocation ...
 * @author Jeanette Winzenburg, Berlin
 */
public class PopupLocation extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        contextMenu.getItems().addAll(cut, copy, paste);
        
        // Create a Box
        Box box = new Box(100, 100, 100);
        box.setTranslateX(150);
        box.setTranslateY(0);
        box.setTranslateZ(400);

        // Create a Sphere
        Sphere sphere = new Sphere(50);
        sphere.setTranslateX(300);
        sphere.setTranslateY(-5);
        sphere.setTranslateZ(400);

        // Create a Cylinder
        Cylinder cylinder = new Cylinder(40, 120);
        cylinder.setTranslateX(500);
        cylinder.setTranslateY(-25);
        cylinder.setTranslateZ(600);

        // Create a Light
        PointLight light = new PointLight(Color.YELLOW);
        light.setTranslateX(350);
        light.setTranslateY(100);
        light.setTranslateZ(300);

        // Create a Camera to view the 3D Shapes
        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.setTranslateX(100);
        camera.setTranslateY(-50);
        camera.setTranslateZ(300);

        // Add the Shapes and the Light to the Group
        Group root = new Group(box, sphere, cylinder, light);

        // Create a Scene with depth buffer enabled
        Scene scene = new Scene(root, 400, 300, true);

        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("mouse click detected!");
                
                if (event.isPopupTrigger()) {
                    // similar results with getX() vs getSceneX() etc.
                    System.out.println("Display menu at (" + event.getSceneX() + "," + event.getSceneY() + ")");
                    
                    contextMenu.show(root, event.getSceneX(), event.getSceneY());
                }
            }
        });
        
        scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("mouse click detected!");

                if (event.isPopupTrigger()) {
                    // similar results with getX() vs getSceneX() etc.
                    System.out.println("Display menu at (" + event.getSceneX() + "," + event.getSceneY() + ")");

                    contextMenu.show(root, event.getScreenX(), event.getScreenY());
                }
            }
        });

        // Add the Camera to the Scene
        scene.setCamera(camera);

        // Add the Scene to the Stage
        stage.setScene(scene);

        // Set the Title of the Stage
        stage.setTitle("Trying to get popup menu working");

        // Display the Stage
        stage.show();
    }
    
}

