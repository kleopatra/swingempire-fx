/*
 * Created on 01.03.2019
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * TilePane: doesn't layout as expected (3 columns, phone-like)
 * https://stackoverflow.com/q/54943082/203657
 * 
 * Problem was setting minWidth of pane and size of scene.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class LockedTilePane extends Application {

    public void start(Stage stage) throws Exception
    {
        // Create a Button or any control item
        Button Button1 = new Button("1");
        Button Button2 = new Button("ABC\n  2");
        Button Button3 = new Button("DEF\n  3");
        Button Button4 = new Button("GHI\n  4");
        Button Button5 = new Button("JKL\n  5");
        Button Button6 = new Button("MNO\n  6");
        Button Button7 = new Button("PQRS\n  7");
        Button Button8 = new Button("TUV\n  8");
        Button Button9 = new Button("WXYZ\n  9");
        Button ButtonStar = new Button("*");
        Button Button0 = new Button("0");
        Button ButtonPlus = new Button("+");

        // Create a new grid pane
        TilePane pane = new TilePane();

        pane.setPrefColumns(3);
//        pane.setPrefRows(4);

        pane.setPadding(new Insets(10, 10, 10, 10));
//        pane.setMinSize(300, 300);
        pane.setVgap(10);
        pane.setHgap(10);

        //set an action on the button using method reference
//        Button1.setOnAction(this::buttonClick);
//        Button2.setOnAction(this::buttonClick);
//        Button3.setOnAction(this::buttonClick);
//        Button4.setOnAction(this::buttonClick);
//        Button5.setOnAction(this::buttonClick);
//        Button6.setOnAction(this::buttonClick);
//        Button7.setOnAction(this::buttonClick);
//        Button8.setOnAction(this::buttonClick);
//        Button9.setOnAction(this::buttonClick);
//        ButtonStar.setOnAction(this::buttonClick);
//        Button0.setOnAction(this::buttonClick);
//        ButtonPlus.setOnAction(this::buttonClick);

        // Add the button and label into the pane

        pane.getChildren().add(Button1);
        pane.getChildren().add(Button2);
        pane.getChildren().add(Button3);
        pane.getChildren().add(Button4);
        pane.getChildren().add(Button5);
        pane.getChildren().add(Button6);
        pane.getChildren().add(Button7);
        pane.getChildren().add(Button8);
        pane.getChildren().add(Button9);
        pane.getChildren().add(ButtonStar);
        pane.getChildren().add(Button0);
        pane.getChildren().add(ButtonPlus);


        // JavaFX must have a Scene (window content) inside a Stage (window)
        // orig
//        Scene scene = new Scene(pane, 300, 100);
        HBox hbox = new HBox(pane);
        pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Scene scene = new Scene(hbox);
        stage.setTitle("Phone");
        stage.setScene(scene);

//        scene.getStylesheets().add("ButtonLook.css");

        // Show the Stage (window)
        stage.show();
        
        // fixed width .. ultimate way out for hbox, don't
        // nonono .. USE_PREF_SIZE  is the way to go
//        pane.setMinWidth(pane.getWidth());


    }

    public static void main(String[] args) {
        launch(args);
    }

}
