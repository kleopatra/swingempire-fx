/*
 * Created on 11.02.2020
 *
 */
package de.swingempire.fx.graphic;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class RoundedCorners extends Application {

    private Parent createContent() {
        VBox root = new VBox();
        root.setPadding(new Insets(0, 0, 0, 0));// top,Right,Bottom,Left
        root.setStyle("-fx-background-color:#00BFFF");

        HBox child1 = new HBox();
//        child1.setPadding(new Insets(0, 0, 40, 0));// top,Right,Bottom,Left
        // child1.setStyle("-fx-background-color:#FFFFFF");
        child1.setStyle("-fx-background-color:red;"
                + "-fx-background-radius: 50 50 0 0;"
//                + "-fx-border-radius: 50 50 0 0;"
//                + "-fx-border-width: 5;"
//                +"-fx-border-color: black;"
                );
        BorderPane content = new BorderPane(child1);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 300, 300));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(RoundedCorners.class.getName());

}
