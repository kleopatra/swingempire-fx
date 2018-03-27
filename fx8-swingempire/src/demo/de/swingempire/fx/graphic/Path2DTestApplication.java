/*
 * Created on 10.09.2015
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

/**
 * Drawing glitch in jdk8u60
 * http://stackoverflow.com/q/32492330/203657
 * 
 * Always pixelated, probably my graphics driver.
 */
public class Path2DTestApplication extends Application {
    private static final int WIDTH = 10;

    Group content = new Group();

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX 1.8.0_60 rendering test");

        Button button = new Button("Canvas 100 x 30");
        button.setOnAction(a->doGenerateCanvas(100,30));

        Button button2 = new Button("Canvas 100 x 400");
        button2.setOnAction(a->doGenerateCanvas(100,400));

        Button button3 = new Button("Paths 100 x 30");
        button3.setOnAction(a->doGeneratePaths(100,30));
        VBox vBox = new VBox();
        vBox.getChildren().addAll(new HBox(button,button2,button3),content);

        Group root = new Group();
        root.getChildren().add(vBox);

        Scene scene = new Scene(root,80*WIDTH,60*WIDTH);//, 1500, 800);//, Color.White);
        stage.setScene(scene);
        stage.show();
    }

    private void doGeneratePaths(int maxX,int maxY) {
        Pane paths = new Pane();
        content.getChildren().clear();
        Platform.runLater(()->{
        for(int i = 0;i<maxX;i++){
            for(int j=0;j<maxY;j++){
                paths.getChildren().add(getPath(i,j));
            }
        }   

        content.getChildren().add(paths);
        });
    }

    private void doGenerateCanvas(int maxX,int maxY) {  
        content.getChildren().clear();
        Platform.runLater(()->{
        Canvas canvas = new Canvas(maxX*WIDTH, maxY*WIDTH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int counter =0;
        for(int i = 0;i<maxX;i++){
            for(int j=0;j<maxY;j++){
                gc.setFill(Color. rgb(255,(int) (Math.random()*255),191));
                double[] xCoords = new double[]{i*WIDTH, (i+1)*WIDTH, (i+1)*WIDTH, i*WIDTH};
                double[] yCoords = new double[]{j*WIDTH,(j)*WIDTH,(j+1)*WIDTH,(j+1)*WIDTH};
                gc.fillPolygon(xCoords,yCoords,xCoords.length);
                counter++;
            }
        }   
        System.out.println(counter +" polygons added");
        content.getChildren().add(canvas);
        });
    }

    protected Node getPath(int i,int j) {           
        Path path = new Path();     
        path.getElements().add(new MoveTo(i*WIDTH, j*WIDTH)); 
        path.getElements().add(new LineTo((i+1)*WIDTH, j*WIDTH)); 
        path.getElements().add(new LineTo((i+1)*WIDTH, (j+1)*WIDTH)); 
        path.getElements().add(new LineTo(i*WIDTH, (j+1)*WIDTH)); 
        path.getElements().add(new LineTo(i*WIDTH, j*WIDTH));

        Paint currentColor =Color. rgb(255,(int) (Math.random()*255),191);

        path.setFill(currentColor);
        path.setStrokeWidth(0.1);
        return path;
    }
    public static void main(String[] args) {
        Application.launch(Path2DTestApplication.class, args);
    }
}

