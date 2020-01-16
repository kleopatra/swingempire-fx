/*
 * Created on 16.01.2020
 *
 */
package de.swingempire.fx.graphic;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.SubtitleTrack;
import javafx.stage.Stage;

/**
 * Playing video with subtitles
 * https://stackoverflow.com/q/59764728/203657
 * 
 * - supported if hard-coded into video file
 * - no track resolution (should there be any?)
 * - requirement is to have subtitles in external file .srt
 * - needs custom coding
 * 
 * If the requirement is to togggle subtitle showing or not: probably
 * not supported if no subtitle track is available
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class VideoExample extends Application {
    
    // plain video with music
    private static final String source = "samplevideo.mp4";
    // subtitles in external file
    private static final String jellies = "jellies.mp4";
    // hard-coded german subtitles
    private static final String german = "abersizzle_german.mp4";
    // hard-coded english subtitles
    private static final String english = "abersizzle_english.mp4";
    
    private Media media;
    
    private Parent createContent() {
        SubtitleTrack t;
        URL url = getClass().getResource(german);
        media = new Media(url.toExternalForm());
        MediaPlayer player = new MediaPlayer(media);
        System.out.println(FXUtils.invokeGetFieldValue(MediaPlayer.class, player, "jfxPlayer"));        
        player.setAutoPlay(true);
        MediaView view = new MediaView(player);
        view.setFitHeight(600);
        view.setFitWidth(1000);
        
        
        BorderPane content = new BorderPane(view);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
        System.out.println("tracks: " + media.getTracks());
        System.out.println("metadata: " + media.getMetadata());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(VideoExample.class.getName());


}
