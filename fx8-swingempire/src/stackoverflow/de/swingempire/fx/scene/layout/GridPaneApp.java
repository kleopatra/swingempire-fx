/*
 * Created on 08.03.2020
 *
 */
package de.swingempire.fx.scene.layout;


import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60586303/203657
 * height increase of upper grid cell on adding children
 */
public class GridPaneApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        int columns = 1;
        int rows = 2;

        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
//        gridPane.setMaxHeight(Region.USE_PREF_SIZE);
//        gridPane.setHgap(4);
//        gridPane.setVgap(4);
//        gridPane.setPadding(new Insets(4));

        for (int i = 0; i < columns; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.CENTER, true));
        }

        for (int i = 0; i < rows; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.CENTER, true));
        }

        for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
            for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                Pane pane = new Pane() {

                    @Override
                    protected double computePrefHeight(double width) {
                        double pref = super.computePrefHeight(width);
                        System.out.println("computing pref: " + pref );
                        return pref;
                    }
                    
                };
                pane.setStyle("-fx-background-color: purple, yellow; -fx-background-insets: 0, 1;");
                gridPane.getChildren().add(pane);
                GridPane.setConstraints(pane, columnIndex, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
                if (rowIndex == 0) {
                    Circle c = new Circle(8, Color.PURPLE);
//                circle.setLayoutX(x);
//                circle.setLayoutY(y);
                    c.relocate(100,  100);
                    pane.getChildren().add(c);
                    pane.setOnMousePressed(event -> {
                        double x = event.getX();
                        double y = event.getY();
                        Circle circle = new Circle(8, Color.PURPLE);
//                        circle.setLayoutX(x);
//                        circle.setLayoutY(y);
                        circle.relocate(x,  y);
                        double oldPref = pane.prefHeight(-1);
                        Bounds old = pane.getLayoutBounds();
                        pane.getChildren().add(circle);
                        System.out.println("x=" + x + ", y=" + y + "pref: " 
                                + pane.prefHeight(-1) + " /old: " + oldPref
                                + "\n    " + old
                                + "\n    " + pane.getLayoutBounds());
//                        pane.setMaxHeight(Region.USE_PREF_SIZE);
                    });
                }
            }
        }

        Scene scene = new Scene(gridPane, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}