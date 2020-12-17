/*
 * Created on 30.11.2020
 *
 */
package de.swingempire.fx.graphic;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * sync'ing multiple MediaPlayers - hanging?
 * https://stackoverflow.com/q/65064419/203657
 * 
 * can't nail ...
 */
public class EmbeddedMediaPlayer extends Application {

    public static class MediaControlMinimal extends BorderPane {
        private final List<MediaPlayer> mpList;

        private Duration duration;

        private boolean shouldPlay;

        private int setupComplete;

        private final Slider timeSlider;

        private final HBox mediaBar;

        private final Button playButton;

        private MediaPlayer controlMediaPlayer;

        public MediaControlMinimal(List<MediaPlayer> mpList, int videoRows) {

            this.mpList = mpList;
            List<MediaView> mvList = new ArrayList<>(mpList.size());

//            Pane mvPane = new Pane() {
//            };

            TilePane mvPane = new TilePane();
            for (MediaPlayer mp : mpList) {
                MediaView mediaView = new MediaView(mp);
                mvList.add(mediaView);
                mvPane.getChildren().add(mediaView);
            }

            mvPane.setStyle("-fx-background-color: black;");
            setCenter(mvPane);

            mediaBar = new HBox(); // 5 als param für spacing = 5, sieh zeile
                                   // 247
            mediaBar.setAlignment(Pos.CENTER);
            mediaBar.setPadding(new Insets(5, 10, 5, 10));
            BorderPane.setAlignment(mediaBar, Pos.CENTER);
            playButton = new Button();

            playButton.setOnAction(e -> {
                shouldPlay = !shouldPlay;
                if (shouldPlay) {
                    playAll();
                } else {
                    pauseAll();
                }
            });

            // Add time slider
            timeSlider = new Slider();
            HBox.setHgrow(timeSlider, Priority.ALWAYS);
            timeSlider.setMinWidth(50);
            timeSlider.setMaxWidth(Double.MAX_VALUE);

            timeSlider.setOnMousePressed(event -> {
                double pressedLoc = (event.getX() / timeSlider.getWidth())
                        * timeSlider.getMax();
                System.out.println("pressedLoc " + pressedLoc);
//                timeSlider.setValueChanging(true);
//                timeSlider.setValue(pressedLoc);
//                timeSlider.setValueChanging(false);

                double value = pressedLoc; //timeSlider.getValue();
                controlMediaPlayer.seek(Duration.millis(value));
                for (MediaPlayer mp : mpList) {
                    if (mp == controlMediaPlayer) continue;
                    Duration old = mp.getCurrentTime();
                    mp.seek(Duration.millis(value));
                    System.out.println("currentTime: " + old + " " + mp.getCurrentTime());
                }
            });

//            timeSlider.valueProperty().addListener((src, ov, nv) -> {
//                if (timeSlider.valueChangingProperty().get()) return;
//                for (MediaPlayer mp : mpList) {
////                  if (mp == controlMediaPlayer) continue;
//                  mp.seek(Duration.millis(timeSlider.getValue()));
//              }
//           });
            controlMediaPlayer = mpList.get(0);
//            controlMediaPlayer.currentTimeProperty().addListener((src, ov, nv) -> {
            // original: invalidationListener
            controlMediaPlayer.currentTimeProperty().addListener(obs -> {
//                System.out.println("player: " + controlMediaPlayer.getCurrentTime());
                updateSliderValue(controlMediaPlayer);
            });

            initMedia(mpList, videoRows);

            mediaBar.getChildren().add(playButton);
            mediaBar.getChildren().add(timeSlider);
            setTop(mediaBar);
            
            timeSlider.valueChangingProperty().addListener((src, ov, nv) -> {
                System.out.println("changing: " + nv);
            });
            timeSlider.valueProperty().addListener((src, ov, nv) -> {
                System.out.println("value: " + ov + " / " + nv);
            });
        }

        /**
         * @param mpList
         * @param videoRows
         */
        protected void initMedia(List<MediaPlayer> mpList, int videoRows) {
            for (MediaPlayer mp : mpList) {
                mp.statusProperty().addListener((src, ov, nv) -> {
                    System.out.println("status changed: " + ov + " / " + nv);
                });
                mp.setOnReady(() -> {
                    int videosPerRow = mpList.size() / videoRows;
                    if (setupComplete == 0) {
                        duration = mp.getMedia().getDuration();
                        timeSlider.setMax(duration.toMillis());
                        updateSliderValue(mp);
//                        final Window window = mvPane.getScene().getWindow();
//                        final double titleHeight = window.getHeight()
//                                - mvPane.getScene().getHeight();
//                        double windowHeight = videoRows
//                                * mp.getMedia().getHeight() + titleHeight;
//                            windowHeight += mediaBar.getHeight();
////                            if (!Main.isTransDesign) {
////                        }
//                        window.setHeight(windowHeight);
//                        window.setWidth(
//                                videosPerRow * mp.getMedia().getWidth());
                    }

                    if (setupComplete < mpList.size()) {
//                        final Node mpNode = mvPane.getChildren()
//                                .get(mpList.indexOf(mp));
//                        if (mpList.indexOf(mp) != 0 && mpNode.getLayoutX() == 0
//                                && mpNode.getLayoutY() == 0) {
//                            // fenster höhe
//                            double xRelocate = mp.getMedia().getWidth()
//                                    * (mpList.indexOf(mp) % videosPerRow);
//                            double yRelocate = mp.getMedia().getHeight() * Math
//                                    .floorDiv(mpList.indexOf(mp), videosPerRow);
//                            mpNode.relocate(xRelocate, yRelocate);
//                        }
                        ++setupComplete;
                    }
                });
                mp.setCycleCount(MediaPlayer.INDEFINITE);
            }
        }

        private void playAll() {
            for (MediaPlayer mp : mpList) {
                mp.play();
            }
        }

        private void pauseAll() {
            for (MediaPlayer mp : mpList) {
                mp.pause();
            }
        }

        protected void updateSliderValue(MediaPlayer mp) {
                Duration currentTime = mp.getCurrentTime();
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
//                        && !timeSlider.isValueChanging()
                        ) {
                    timeSlider.setValue(currentTime.toMillis());
                }
                Platform.runLater(() -> {
            });
            if (timeSlider != null) {
            }
        }
    }

    public static final String[] LOCAL = {
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
            "SambaReggae_Fundo1.mp4",
    };
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("players");
        Group root = new Group();
        Scene scene = new Scene(root, 500, 700);

        List<MediaPlayer> mediaPlayerList = new ArrayList<>();
        // create media player
//        for (String s : MEDIA_URL) {
        for (String s : LOCAL) {
            URL resource = getClass().getResource(s);
            Media media = new Media(resource.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(false);
            mediaPlayerList.add(mediaPlayer);
        }
        MediaControlMinimal mediaControl = new MediaControlMinimal(
                mediaPlayerList, 2);
        scene.setRoot(mediaControl);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setX(10);
    }

//    @Override
//    public void stop() {
//        System.out.println("Stage is closing");
//        System.exit(0);
//    }
//
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
//  public static final String[] MEDIA_URL = {
////"https://video.fogodosamba.de/media/SambaReggae_Sticks.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Fundo1.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Dobra.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Fundo2.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Ansage.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Timbal.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Caixa.mp4",
//"https://video.fogodosamba.de/media/SambaReggae_Repi.mp4"
//};


}