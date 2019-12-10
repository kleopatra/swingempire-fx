/*
 * Created on 25.01.2019
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Drag released not fired.
 * https://stackoverflow.com/q/54357180/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DnDReleased extends Application {

    private double mouseClickPositionX, mouseClickPositionY, currentRelativePositionX, currentRelativePositionY;

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Hello World");
        Label button1 = new Label("button1");
        Button button2 = new Button("button2");

//        BorderPane mainBorderPane = new BorderPane();
////        BorderPane centerBorderPane = new BorderPane();
////        FlowPane flowPane = new FlowPane();
//        GridPane gridPane = new GridPane();
//        gridPane.setStyle("-fx-background-color: red");
//        gridPane.add(button1, 0, 0);
////        flowPane.getChildren().add(gridPane);
////        centerBorderPane.setCenter(flowPane);
////        HBox hbox = new HBox();
////        TilePane tilePane = new TilePane();
////        tilePane.getChildren().add(button2);
////        hbox.getChildren().add(tilePane);
//        mainBorderPane.setCenter(gridPane);
//        mainBorderPane.setBottom(button2);
////        centerBorderPane.setBottom(hbox);

        // original nesting
        BorderPane mainBorderPane = new BorderPane();
        BorderPane centerBorderPane = new BorderPane();
        FlowPane flowPane = new FlowPane();
        GridPane gridPane = new GridPane();
//        Button button1 = new Button("button1");
        gridPane.add(button1, 0, 0);
        flowPane.getChildren().add(gridPane);
        centerBorderPane.setCenter(flowPane);
        HBox hbox = new HBox();
        TilePane tilePane = new TilePane();
//        Button button2 = new Button("button2");
        tilePane.getChildren().add(button2);
        hbox.getChildren().add(tilePane);
        mainBorderPane.setCenter(centerBorderPane);
        centerBorderPane.setBottom(hbox);

        // button2 event handlers

        button2.setOnMousePressed(event -> {
            mouseClickPositionX = event.getSceneX();
            mouseClickPositionY = event.getSceneY();
            currentRelativePositionX = button2.getTranslateX();
            currentRelativePositionY = button2.getTranslateY();
            button2.setMouseTransparent(true);
        });

        button2.setOnMouseDragged(event -> {
            button2.setTranslateX(currentRelativePositionX + (event.getSceneX() - mouseClickPositionX));
            button2.setTranslateY(currentRelativePositionY + (event.getSceneY() - mouseClickPositionY));
        });

        button2.setOnDragDetected(event -> {
            System.out.println("started " + event);
            button2.startFullDrag();
        });

        button2.setOnDragExited(e -> {
        });
        
        button2.setOnMouseReleased((e) -> {
            Stage stage = createStage(e, button2);
            System.out.println("creating and showing");
            if (stage != null) {
                stage.show();
            }
           button2.setMouseTransparent(false);
        });

        // button1 event handlers

        gridPane.setOnDragOver(e -> {
            System.out.println("gridPane " + e.getEventType() + e);
            
        });
        
        gridPane.setOnDragEntered(e ->{
            System.out.println("gridPane " + e.getEventType() + e);
            
        });
        button1.setOnDragOver(e -> {
            System.out.println("button1 " + e.getEventType() +  e);
            
        });
        button1.setOnMouseDragReleased((event) -> {
            System.out.println("button1 " +  event.getEventType() + event);
        });

        // gridPane event handlers

        gridPane.setOnMouseDragReleased((event) -> {
            System.out.println("gridPane " + event.getEventType() +event);
        });

        primaryStage.setScene(new Scene(mainBorderPane, 300, 275));
        primaryStage.show();
    }


    /**
     * Create and show a new window if dropped outside of parent.
     */
    private Stage createStage(MouseEvent e, Button button2) {
        double xScreen = e.getScreenX();
        double yScreen = e.getScreenY();
        Window bWindow = (Stage) button2.getScene().getWindow();
        if (xScreen < bWindow.getX() || yScreen < bWindow.getY()) {
            Stage stage = new Stage();
            stage.setX(xScreen - 50);
            stage.setY(yScreen - 50);
            BorderPane content = new BorderPane(new Button(button2.getText()));
            stage.setScene(new Scene(content)); 
            return stage;
        }
        return null;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
