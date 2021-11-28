/*
 * Created 28.11.2021
 */
package de.swingempire.fx.webview;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;
//import javafx.scene.media.AudioClip;
import javafx.stage.Stage;


/**
 * can't use media - the module is not built.
 *
 * FIXME: add download?
 */
public class AudioClipExample extends Application {

    private Parent createContent() {
        String source = "Airplane+4.mp3";
        AudioClip plonkSound = new AudioClip(getClass().getResource(source).toExternalForm());
//        plonkSound.play();
        Button play = new Button("dummy");
        play.setOnAction(e -> plonkSound.play());
        return play;
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
    private static final Logger LOG = Logger.getLogger(AudioClipExample.class.getName());

}
