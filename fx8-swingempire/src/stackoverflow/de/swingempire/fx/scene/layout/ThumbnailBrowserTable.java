/*
 * Created on 28.01.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59883198/203657
 * 
 * performance problems with showing many thumbnails.
 * here tackled with a TableView (have OOM with imageSize 200 ..)
 * open: size columns, remove header
 */
public class ThumbnailBrowserTable extends Application {
    
    private ObservableList<ImageRow> imageRows = FXCollections.observableArrayList();
    private double imageSize = 100;
    private int columnCount = 4;
    
    public static class ImageRow {
        ObservableList<ObjectProperty<Image>> imageProperties = FXCollections.observableArrayList();
        public ImageRow(Image...images) {
            Arrays.stream(images).forEach(e ->imageProperties.add(new SimpleObjectProperty<>(e)));
        }
        
        public int size()  {
            return imageProperties.size();
        }
        
        public ObjectProperty<Image> imageProperty(int i) {
            return imageProperties.get(i);
        }
        
    }
    
    public static class ImageCell extends TableCell<ImageRow, Image> {
        ImageView imageView;
        double size;
        public ImageCell(double size) {
            this.size = size;
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(size);
            imageView.setFitWidth(size);
            imageView.setCache(true);
            imageView.setCacheHint(CacheHint.SPEED);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(imageView);
        }
        @Override
        protected void updateItem(Image item, boolean empty) {
            super.updateItem(item, empty);
            imageView.setImage(item);
        }
        
    }
    private void addImagesToGrid() {
        int size = (int) imageSize;
        int numRows = 10000;
        List<ImageRow> rows = new ArrayList<ImageRow>();
        for (int i = 0; i < numRows; i++) {
            Image[] cells = new Image[columnCount];
            for (int cell = 0; cell < columnCount; cell++) {
                cells[cell] = createFakeImage(i, size);
            }
            rows.add(new ImageRow(cells));
            if (i % 100 == 0) {
                System.out.println("row: " + i);
            }
        }
        Platform.runLater(() -> imageRows.addAll(rows));
    }
    
    private Parent createContent() {
        TableView<ImageRow> imageTable = new TableView<>(imageRows);
        
        for (int i = 0; i < columnCount; i++) {
            imageTable.getColumns().add(createColumn(i));
        }
        
        Label rowLabel = new Label();
        rowLabel.textProperty().bind(Bindings.size(imageRows).asString());
        BorderPane content = new BorderPane(imageTable);
        content.setBottom(new HBox(10, new Label("Size: "), rowLabel));
        return content;
    }


    /**
     * @param i
     */
    protected TableColumn<ImageRow, Image> createColumn(int i) {
        TableColumn<ImageRow, Image> first = new TableColumn<>("Column "+i);
        first.setPrefWidth(imageSize);
        first.setCellValueFactory(cc -> cc.getValue().imageProperty(i));
        first.setCellFactory(cc -> new ImageCell(200));
        return first;
    }


    // Create an image with a bunch of rectangles in it just to have something
    // to display.
    private Image createFakeImage(int imageIndex, int size) {
        BufferedImage image = new BufferedImage(size, size,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        for (int i = 1; i < size; i++) {
            g.setColor(new Color(i * imageIndex % 256,
                    i * 2 * (imageIndex + 40) % 256,
                    i * 3 * (imageIndex + 60) % 256));
            g.drawRect(i, i, size - i * 2, size - i * 2);
        }
        return SwingFXUtils.toFXImage(image, null);
    }


    @Override
    public void start(Stage primaryStage) {

        Scene scene = new Scene(createContent(), 1000, 600);
        primaryStage.setScene(scene);

        // Start showing the UI before taking time to load any images
        primaryStage.show();

        loadImages();
    }

    private void loadImages() {
        // Load images in the background so the UI stays responsive.
        ExecutorService executor = Executors.newFixedThreadPool(20);
        executor.submit(() -> {
            addImagesToGrid();
        });
        
    }


    public static void main(String[] args) {
        launch(args);
    }
}

