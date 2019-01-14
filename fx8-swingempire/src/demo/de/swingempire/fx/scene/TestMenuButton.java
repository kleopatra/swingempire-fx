/*
 * Created on 10.01.2019
 *
 */
package de.swingempire.fx.scene;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * MenuButton popup appears for off the window. same for combo
 * https://bugs.openjdk.java.net/browse/JDK-8088782
 * 
 * commented: user error, conflicting sizing constraints
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TestMenuButton extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        MenuButton button = new MenuButton("button");
//        MenuItem item = new MenuItem("this is a test");
//        button.getItems().add(item);
        
        ComboBox button = new ComboBox();
        button.getItems().add("mine");
        
        button.setMinSize(500, 500);
        
        BorderPane pane = new BorderPane(button);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setHeight(200);
        System.out.println("min/pref/max: " + button.minHeight(-1) + "/" + button.getPrefHeight() 
            + "/" +button.prefHeight(-1));
    }
}