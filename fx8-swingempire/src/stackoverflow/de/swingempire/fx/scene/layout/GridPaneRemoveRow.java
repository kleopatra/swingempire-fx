/*
 * Created on 28.07.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/63127160/203657
 * 
 * remove row from gridPane: quick check removing all nodes in a row updates the
 * grid visually (the rows below are moved up). Note that the row constraint for the
 * remaining rows is unchanged .. 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class GridPaneRemoveRow extends Application {
    GridPane grid = new GridPane();

    private Node lastClicked;

    @Override
    public void start(Stage primaryStage) throws Exception {

        List<Node> tiles = new ArrayList<>();
        String[] imageResources = new String[] { "00.jpg", "10.jpg", "11.jpg",
                "20.jpg", "21.jpg", "22.jpg", "30.jpg", "31.jpg", "32.jpg",
                "33.jpg", "40.jpg", "41.jpg", "42.jpg", "43.jpg", "44.jpg",
                "50.jpg", "51.jpg", "52.jpg", "53.jpg", "54.jpg", "55.jpg",
                "60.jpg", "61.jpg", "62.jpg", "63.jpg", "64.jpg", "65.jpg",
                "66.jpg" };

        for (final String imageResource : imageResources) {
            Node imageView = new Button(imageResource);
            tiles.add(imageView);
        }

        // Assign mouse press handler.
        for (Node imageView : tiles) {
//            imageView.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            imageView.setOnMousePressed( e -> {
               lastClicked = imageView;
            }
                    );
//            imageView.setOnMouseClicked(e -> clickedImage = imageView);
        }

        Collections.shuffle(tiles);
        for (int col = 0; col < 7; col++) {
            for (int row = 0; row < 4; row++) {
                grid.add(tiles.remove(0), col, row);
            }
        }

        Button removeRow = new Button("remove");
        removeRow.setOnAction(e -> {
            if (lastClicked == null) return;
            int row = grid.getRowIndex(lastClicked);
            System.out.println("last row: " + row);
            List<Node> toRemove = grid.getChildren().stream()
                    .filter(child -> grid.getRowIndex(child) == row)
                    .collect(Collectors.toList());
            grid.getChildren().removeAll(toRemove);
            lastClicked = null;
        });
        
        BorderPane content = new BorderPane(grid);
        content.setBottom(new HBox(10, removeRow));
        
        primaryStage.setScene(new Scene(content)); //, 800, 350));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }


}
