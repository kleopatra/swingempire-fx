/*
 * Created on 05.10.2019
 *
 */
package de.swingempire.fx.concurrency;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58243099/203657
 * OP argues that the not-throwing of progress binding is fine
 * 
 * actually it is not ... fact is that fx doesn't protect itself
 * against threading violations (only does when changing the scenegraph
 * structure)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FishyThreadingProgress extends Application {

    private Parent createContent() {
        HBox indicator = new HBox(10, new Label("Stars "));
        ProgressBar bar = new ProgressBar();
        // set prefWidth to fixed value to make its change visible
        bar.setPrefWidth(200);
        DoubleProperty prop = new SimpleDoubleProperty(0);
        bar.progressProperty().bind(prop);
        bar.progressProperty().addListener((scr, ov, nv) -> {
            // verify that we are not on fx app thread
            // that is are watching an illegal state change of a property
            LOG.info("on fx? " + Platform.isFxApplicationThread()
                    + bar.getPrefWidth());
            // layout update not throwing immediately
            // But: this does NOT MEAN that everything is fine!!! we just have a
            // lucky moment
            bar.setPrefWidth(bar.getPrefWidth() + 10);
            // scenegraph structure change is throwing immediately
            // protecting us against relying on pure luck 
            indicator.getChildren().add(new Label("*"));

        });
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // updating a property of a node that's attached to the
                // scenegraph
                // DONT DONT DONT ... NEVER-EVER!!!
                prop.set(prop.get() + 0.1);
            }
        });
        thread.setDaemon(true);
        thread.start();
        BorderPane content = new BorderPane(bar);
        content.setTop(indicator);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 600, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FishyThreadingProgress.class.getName());

}
