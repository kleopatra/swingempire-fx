/*
 * Created on 22.08.2017
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ApplyCSSWithLayout extends Application {

    @Override
    public void start(Stage stage) throws Exception {

       Group root = new Group();
       Scene scene = new Scene(root);

       Button button = new Button("Hello World");
       root.getChildren().add(button);

       /*
        * need both to init pref
        */
       root.applyCss();
       root.layout();

       double width = button.getWidth();
       double height = button.getHeight();
       double pwidth = button.prefWidth(-1);
       double pheight = button.prefHeight(-1);

       System.out.println("pref: " +pwidth + ", " + pheight);
       System.out.println("actual: " + width + ", " + height);

       stage.setScene(scene);
       stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
