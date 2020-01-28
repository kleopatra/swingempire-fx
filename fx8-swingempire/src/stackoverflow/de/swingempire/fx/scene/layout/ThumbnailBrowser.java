/*
 * Created on 28.01.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59883198/203657
 * 
 * performance problems with showing many thumbnails.
 * original
 */
public class ThumbnailBrowser extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  
  @Override
  public void start(Stage primaryStage) {
    // Create a Scene with a ScrollPane that contains a TilePane.
    TilePane tilePane = new TilePane();
    tilePane.getStyleClass().add("pane");
    tilePane.setCache(true);
    tilePane.setCacheHint(CacheHint.SPEED);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setContent(tilePane);

    Scene scene = new Scene(scrollPane, 1000, 600);
    primaryStage.setScene(scene);

    // Start showing the UI before taking time to load any images
    primaryStage.show();

    // Load images in the background so the UI stays responsive.
    ExecutorService executor = Executors.newFixedThreadPool(20);
    executor.submit(() -> {
      addImagesToGrid(tilePane);
    });
  }

  private void addImagesToGrid(TilePane tilePane) {
    int size = 200;
    int numCells = 2000;
    for (int i = 0; i < numCells; i++) {
      // (In the real application, get a list of image filenames, read each image's thumbnail, generating it if needed.
      // (In this minimal reproducible code, we'll just create a new dummy image for each ImageView)
      ImageView imageView = new ImageView(createFakeImage(i, size));
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(size);
      imageView.setFitWidth(size);
      imageView.setCache(true);
      imageView.setCacheHint(CacheHint.SPEED);
      Platform.runLater(() -> tilePane.getChildren().add(imageView));
    }
  }

  // Create an image with a bunch of rectangles in it just to have something to display.
  private Image createFakeImage(int imageIndex, int size) {
    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.getGraphics();
    for (int i = 1; i < size; i ++) {
      g.setColor(new Color(i * imageIndex % 256, i * 2 * (imageIndex + 40) % 256, i * 3 * (imageIndex + 60) % 256));
      g.drawRect(i, i, size - i * 2, size - i * 2);
    }
    return SwingFXUtils.toFXImage(image, null);
  }
}

