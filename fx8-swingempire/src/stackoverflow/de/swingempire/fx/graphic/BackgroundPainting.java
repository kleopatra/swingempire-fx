/*
 * Created on 17.11.2019
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Image update in background thread
 * https://stackoverflow.com/q/58882822/203657
 * 
 * Problem: not updated visibly
 * Reason: the background thread is manipulating the same instance that's
 *   shown in the ImageView off the fx thread
 * Solution: return a copy of the manipulated image  
 */
public class BackgroundPainting extends Application {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 400;

    /**
     * Time in ms between repainting attempts
     **/
    private static final long REPAINTING_TIME = 100;

    private ImageView imageView;
    private RepaintingService service = new RepaintingService();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setUpStage(primaryStage);
        startService();
    }

    private void setUpStage(Stage stage) {
        Group group = new Group();
        imageView = new ImageView(new WritableImage(WINDOW_WIDTH, WINDOW_HEIGHT));
        group.getChildren().add(imageView);

        stage.setScene(new Scene(group, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    private void startService() {
        service.setOnSucceeded((eh) -> {
            imageView.setImage(service.getValue());
            int firstWhiteLine = findFirstWhiteLineInImage(service.getValue());
            System.out.println("First white line in received image: " + firstWhiteLine + service.getValue());
            try {
                Thread.sleep(REPAINTING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            service.reset();
            service.start();
        });
        service.start();
    }

    /**
     * For debug purposes: Do find the number of the first line with white pixels
     * in the given image.
     **/
    private int findFirstWhiteLineInImage(Image repaintedImage) {
        for (int line = 0; line < repaintedImage.getHeight(); line++) {
            if (Color.WHITE.equals(repaintedImage.getPixelReader().getColor(0, line))) {
                return line;
            }
        }
        return -1;
    }

    /**
     * Code from https://stackoverflow.com/a/51919485/203657 
     * copy the given image to a writeable image
     * 
     * @param image
     * @return a writeable image
     */
    public static WritableImage copyImage(Image image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }


    /**
     * A service to periodically repaint the image,
     * starting off with a white image and with each repainting adding a black line of pixels.
     **/
    private class RepaintingService extends Service<Image> {

        private volatile WritableImage image = new WritableImage(WINDOW_WIDTH, WINDOW_HEIGHT);
        private int blackLinesCount = 0;

        @Override
        protected Task<Image> createTask() {
            return new Task<Image>() {

                @Override
                protected Image call() {
                    repaintImage();
                    blackLinesCount++;
                    return copyImage(image);
                }
            };
        }

        /**
         * Repaints the image with the upper n lines being black
         * and the remaining lines being white.
         **/
        private void repaintImage() {
            for (int line = 0; line < WINDOW_HEIGHT; line++) {
                for (int column = 0; column < WINDOW_HEIGHT; column++) {
                    Color color = line <= blackLinesCount ? Color.BLACK : Color.WHITE;
                    image.getPixelWriter().setColor(column, line, color);
                }
            }
        }
    }
}

