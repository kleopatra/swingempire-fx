/*
 * Created on 21.07.2019
 *
 */
package de.swingempire.fx.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DragAndDropExample extends Application {
    
    
    ImageView imageView;
    StackPane contentPane;
    BorderPane layout;
 
    public static void main(String[] args) {
        launch(args);
    }
 
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drag and Drop");
        layout = new BorderPane();
        contentPane = new StackPane();
        Scene scene = new Scene(layout, 800, 800, Color.WHITE);
 
 
 
 
 
        contentPane.setOnDragOver(new EventHandler<>() {
            @Override
            public void handle(final DragEvent event) {
                mouseDragOver(event);
            }
        });
 
        contentPane.setOnDragDropped(new EventHandler<>() {
            @Override
            public void handle(final DragEvent event) {
                mouseDragDropped(event);
            }
        });
 
         contentPane.setOnDragExited(new EventHandler<>() {
            @Override
            public void handle(final DragEvent event) {
                contentPane.setStyle("-fx-border-color: #C6C6C6;");
            }
        });
 
       layout.setCenter(contentPane);
 
        primaryStage.setScene(scene);
        primaryStage.show();
 
    }
 
    void addImage(Image i, StackPane pane){
 
        imageView = new ImageView();
        imageView.setImage(i);
        pane.getChildren().add(imageView);
    }
 
  private void mouseDragDropped(final DragEvent e) {
        final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            // Only get the first file from the list
            final File file = db.getFiles().get(0);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println(file.getAbsolutePath());
                    try {
                        if(!contentPane.getChildren().isEmpty()){
                            contentPane.getChildren().remove(0);
                        }
                        Image img = new Image(new FileInputStream(file.getAbsolutePath()));  
 
                        addImage(img, contentPane);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(DragAndDropExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
        e.setDropCompleted(success);
        e.consume();
    }
 
    private  void mouseDragOver(final DragEvent e) {
        final Dragboard db = e.getDragboard();
 
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");
 
        if (db.hasFiles()) {
            if (isAccepted) {
                contentPane.setStyle("-fx-border-color: red;"
              + "-fx-border-width: 5;"
              + "-fx-background-color: #C6C6C6;"
              + "-fx-border-style: solid;");
                e.acceptTransferModes(TransferMode.COPY);
            }
        } else {
            e.consume();
        }
    }
 
}

