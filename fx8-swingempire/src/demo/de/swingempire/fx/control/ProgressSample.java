/*
 * Created on 06.08.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Code from tutorial.
 */
public class ProgressSample extends Application {
    
    final Float[] values = new Float[] {-1.0f, 0f, 0.6f, 1.0f};
    final Label [] labels = new Label[values.length];
    final ProgressBar[] pbs = new ProgressBar[values.length];
    final ProgressIndicator[] pins = new ProgressIndicator[values.length];
    final HBox hbs [] = new HBox [values.length];

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 300, 250);
        stage.setScene(scene);
        stage.setTitle("Progress Controls");
 
        for (int i = 0; i < values.length; i++) {
            final Label label = labels[i] = new Label();
            label.setText("progress:" + values[i]);
 
            final ProgressBar pb = pbs[i] = new ProgressBar();
            pb.setProgress(values[i]);
 
            final ProgressIndicator pin = pins[i] = new ProgressIndicator();
            pin.setProgress(values[i]);
            final HBox hb = hbs[i] = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(label, pb, pin);
        }
 
        // change text under progressIndicator
        // http://stackoverflow.com/q/31814830/203657
//        configDone(pins[pins.length - 1]);
        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.getChildren().addAll(hbs);
        scene.setRoot(vb);
        stage.show();
    }
        
    /**
     * change text under progressIndicator
     * http://stackoverflow.com/q/31814830/203657
     * @param progressIndicator
     */
    private void configDone(ProgressIndicator indicator) {
        // hack from 
        // http://stackoverflow.com/a/16038242/203657
        indicator.progressProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number newValue) {
                // If progress is 100% then show Text
                if (newValue.doubleValue() >= 1) {
                    // Apply CSS so you can lookup the text
                    indicator.applyCss();
                    Text text = (Text) indicator.lookup(".text.percentage");
                    // This text replaces "Done"
                    text.setText("Foo");
                }
            }
        });
        indicator.setProgress(0);
        indicator.setProgress(1);        
    }

    public static void main(String[] args) {
        launch(args);
    }
}