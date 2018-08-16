/*
 * Created on 15.08.2018
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 * Layout problem?
 * https://stackoverflow.com/q/51825695/203657
 * 
 * was overspecified .. too many sizes forced into unfullfillable overall constraint.
 * 
 * Open: why isn't the scrollPane content not visible initially?
 * It's empty but fitTo should work?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ScrollPaneFit extends Application
{

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        
        // borderPane rootPane
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: ivory;");
        
        // two scrollPanes, each should resize it's height (width should be
        // fixed) if
        // children are added beyond it's current height
        ScrollPane topSP = new ScrollPane();
        topSP.setStyle("-fx-background-color: lightgreen;");
        ScrollPane bottomSP = new ScrollPane();
        bottomSP.setStyle("-fx-background-color: lightblue;");

//        topSP.setFitToHeight(true);
//        bottomSP.setFitToHeight(true);
        
        topSP.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        topSP.setVbarPolicy(ScrollBarPolicy.NEVER);
        
        bottomSP.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        bottomSP.setVbarPolicy(ScrollBarPolicy.NEVER);

        GridPane rightPane = new GridPane();
        rightPane.setStyle("-fx-background-color: red;");
        RowConstraints rc1 = new RowConstraints();
        rc1.setPercentHeight(50);
        RowConstraints rc2 = new RowConstraints();
        rc2.setPercentHeight(50);
        rightPane.getRowConstraints().addAll(rc1, rc2);
        rightPane.add(topSP, 0, 0);
        rightPane.add(bottomSP, 0, 1);
        
        borderPane.setRight(rightPane);
        
        TilePane topContent = new TilePane();
        topContent.setStyle("-fx-background-color: lightgreen;");
        topContent.setPrefColumns(3);
        topSP.setContent(topContent);
        
        double unit = 100;
        topSP.setOnMouseClicked(new EventHandler<Event>() {
            
            @Override
            public void handle(Event event) {
                
                topContent.getChildren().add(createLabel("top", "lightpink", unit));
                topSP.setVvalue(1.0); // effective only after next pulse ...
                
            }
        });
        
        TilePane bottomContent = new TilePane();
        bottomContent.setStyle("-fx-background-color: lightyellow;");
        bottomContent.setPrefColumns(3);
        bottomSP.setContent(bottomContent);

        bottomSP.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                bottomContent.getChildren().add(createLabel("bottom", "lightblue", unit));
                bottomSP.setVvalue(1.0); // effective only after next pulse ...

            }
        });

        // size settings

        double width = 3 * unit, height = 3*unit;

        bottomSP.setMinViewportHeight(height);
        bottomSP.setMinViewportWidth(width);
        bottomSP.setPrefViewportHeight(height);
        bottomSP.setPrefViewportWidth(width);

        // just to have some center
        Label center = new Label("Something to see in here!!");
        center.setPrefSize(width, height);
        center.setStyle("-fx-background-color: moccasin;");
        borderPane.setCenter(center);
        
        // fixed size has no effect if root of scene: 
        // a borderPane as root will fill all availabe space in the scene
        // the other way round: will be effective inside another pane
//        borderPane.setMinSize(600, 600);
//        borderPane.setPrefSize(600, 600);
//        borderPane.setMaxSize(600, 600);
        
        // stage
        Scene scene = new Scene(borderPane); //, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected Label createLabel(String text, String color, double size) {
        Label l = new Label(text);
        l.setPrefSize(size, size);
        // don't over specify - instead choose an appropriate layout 
//        l.setMinSize(size, size);
//        l.setMaxSize(size, size);
        l.setStyle("-fx-background-color: " + color  + ";");
        return l;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ScrollPaneFit.class.getName());
}

