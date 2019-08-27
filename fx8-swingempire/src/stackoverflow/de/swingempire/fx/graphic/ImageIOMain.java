/*
 * Created on 27.08.2019
 *
 */
package de.swingempire.fx.graphic;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57673051/203657
 * can't write snapshot as jpeg (could in fx8, can't in fx12)
 * 
 * problem with JPEGImageWriterSpi: reports false from canEncode
 * because the colorModel of the snapshot supports alpha.
 */
public class ImageIOMain extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new Scene(new StackPane(), 800.0, 600.0);

        stage.setScene(scene);
        stage.show();

        WritableImage img = scene.snapshot(null);

        //Image file created on desktop
        BufferedImage png = SwingFXUtils.fromFXImage(img, null);
        ImageIO.write(png, "png", new File("C:\\temp\\test.png"));

        //Image file NOT created on desktop
        BufferedImage jpg = SwingFXUtils.fromFXImage(img, null);
        // image has alpha channel, can't be encoded by JPEgImageWriterSpi
        // no idea if that's expected or not
        ColorModel m = jpg.getColorModel();
        boolean alpha = m.hasAlpha();
        LOG.info("" + alpha);
        
        ImageIO.write(jpg, "jpg", new File("C:\\temp\\test.jpeg"));
        
        LOG.info("png/jpg \n" + png + " \n " + jpg);
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ImageIOMain.class.getName());
}