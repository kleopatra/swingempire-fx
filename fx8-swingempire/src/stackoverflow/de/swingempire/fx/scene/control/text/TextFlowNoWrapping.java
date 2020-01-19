/*
 * Created on 18.01.2020
 *
 */
package de.swingempire.fx.scene.control.text;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * No wrapping of textflow. Needs to be implicitly not-resizable, f.i. by setting min to pref.
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFlowNoWrapping extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TextFlow example = new TextFlow();
        example.getChildren().addAll(new Text("A very long text."), new Text("So long that will wrap, if it's parent is too small."), new Text("But instead the overrun text should be clipped."));
//        example.setPrefWidth(Double.MAX_VALUE);
        example.setMinWidth(Region.USE_PREF_SIZE);
        
//        example.setPrefWidth(Region.USE_COMPUTED_SIZE);
        example.setStyle("-fx-font-size: 14px");

        HBox wrapper = new HBox(example);
//        VBox wrapper = new VBox(example);
        wrapper.setMaxWidth(30);
        wrapper.setStyle("-fx-border-color: black");

//        HBox root = new HBox(wrapper);
        VBox root = new VBox(wrapper);
        Scene scene = new Scene(root, 500, 500);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

