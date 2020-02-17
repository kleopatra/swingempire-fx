/*
 * Created on 14.02.2020
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60227015/203657
 * add gap between text and graphic of a menuitem
 * 
 * from answer: use css
 */
public class MenuGapApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ImageView imageView = new ImageView("https://image.shutterstock.com/image-vector/home-flat-icon-you-can-260nw-451922449.jpg");
        MenuItem menuItem = new MenuItem("New filter", imageView);
        Menu menu = new Menu("Filter", null, menuItem);
        MenuBar menuBar = new MenuBar(menu);
        Scene scene = new Scene(menuBar);
//        scene.getStylesheets().add(getClass().getResource("menugap.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}