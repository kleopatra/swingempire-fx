/*
 * Created on 30.11.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Whacky suggestion on SO
 * https://bugs.openjdk.java.net/browse/JDK-8090829
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SickApp extends Application {

    private Parent createContent() {
        
        return new Button("nix");
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) throws Exception {
        new SickApp().start(null);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SickApp.class.getName());

}
