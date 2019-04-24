/*
 * Created on 16.04.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Image params: either fully qualified url or absolute path to file
 * absolute path must be built with slashes! as always ...
 * 
 * Implementation detail: Image delegates loading to contextClassLoader of currentThread,
 * so the rules for loading resources by classLoader applies 
 * (always absolute path, leading slash or not doesn't matter!)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ImageExample extends Application {

    String filename = "kleopatra.jpg";
    
    private Parent createContent() {
        String packageName = getClass().getPackageName();
        LOG.info(packageName);
        
        Image fromName = new Image("file:" +filename);
        Image absolutPath = new Image("de/swingempire/fx/scene/control/kleopatra.jpg");
        
        Image asStream = new Image(getClass().getResourceAsStream(filename));
        Image asAbsoluteStream = new Image(getClass().getResourceAsStream("/de/swingempire/fx/scene/control/kleopatra.jpg"));
        Image asAbsoluteStreamFromLoader = new Image(getClass().getClassLoader().getResourceAsStream("de/swingempire/fx/scene/control/kleopatra.jpg"));
        
        URL resourceURL = getClass().getResource(filename);
        Image fromUrl = new Image(resourceURL.toExternalForm());
        HBox content = new HBox(10, new ImageView(absolutPath), 
                new ImageView(asStream), 
                new ImageView(asAbsoluteStream),
                new ImageView(asAbsoluteStreamFromLoader),
                new ImageView(fromUrl)
                );
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ImageExample.class.getName());

}
