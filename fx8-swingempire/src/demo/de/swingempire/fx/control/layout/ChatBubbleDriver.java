/*
 * Created on 27.07.2015
 *
 */
package de.swingempire.fx.control.layout;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.swingempire.fx.control.layout.ChatBubble.BubbleSpec;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ChatBubbleDriver extends Application {

    public void start(Stage primaryStage) {
        try {
            Group root = new Group();
            Scene scene = new Scene(root,400,400);
//            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());         
            Pane p = new Pane();
            p.setShape(new ChatBubble(BubbleSpec.FACE_TOP));
            p.setPrefSize(400, 400);
            p.setBackground(new Background(new BackgroundFill(Color.GOLD,
                    null, null)));
            root.getChildren().add(p);
            Label label = new Label("Madam Kleopatra this is it");
            label.setShape(new ChatBubble(BubbleSpec.FACE_LEFT_CENTER));
            p.getChildren().add(label);
            label.setBackground(new Background(new BackgroundFill(Color.YELLOW,
                    null, null)));
            label.setPrefSize(200, 100);
//            label.setPadding(new Insets(20));
            label.relocate(100, 100);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch();
    }
}
