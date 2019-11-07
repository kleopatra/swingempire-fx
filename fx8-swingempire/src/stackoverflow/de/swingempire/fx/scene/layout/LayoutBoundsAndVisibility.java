/*
 * Created on 07.11.2019
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58732739/203657
 * different layoutbounds on toggling content visibilty
 * 
 * oversimplified example - real question is: what does she want to
 * do with layoutBounds. On face value, replacing the group (not resizable)
 * with a region (resizable) wont change the layoutBounds with visibility.
 */
public class LayoutBoundsAndVisibility extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Circle c1 = new Circle(0.0, 0.0, 3); // radius is 3
        Circle c2 = new Circle(10.0, 10.0, 3);

        Circle c3 = new Circle(20.0, 20.0, 3); // radius is 3
        Circle c4 = new Circle(30.0, 30.0, 3);

        Group g1 = new Group();
        Group g2 = new Group();
        
        System.out.println("resizable?" + g1.isResizable());
        g1.getChildren().addAll(c1, c2);
        g2.getChildren().addAll(c3, c4);

//        Group main = new Group(g1, g2);
//        HBox main = new HBox(g1, g2);
        Region main = new Region() {
            {
                getChildren().addAll(g1, g2);
            }
        };
        System.out.println("main resizable?" + main.isResizable());
        
        main.setBackground(new Background(new BackgroundFill(Color.GOLD,
                    null, null)));
        CheckBox checkBox1 = new CheckBox("Show");
        g2.visibleProperty().bind(checkBox1.selectedProperty());

        Bounds b1 = main.getLayoutBounds();
        System.out.println(b1);

        checkBox1.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                System.out.println(main.getLayoutBounds());
            }
        });

        HBox hbox = new HBox(checkBox1, main);

        Scene scene = new Scene(hbox, 400, 300);

        primaryStage.setTitle("Hello Stackoverflow, are you happy now?");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
