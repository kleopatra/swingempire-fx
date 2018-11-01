/*
 * Created on 01.11.2018
 *
 */
package de.swingempire.fx.scene.css;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/53083005/203657
 * 
 * lighter theme not applied on hover.
 * 
 * Sai: to fix, use style (vs. background directly)
 */
public class TitledPaneWithCSS extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final StackPane root = new StackPane();

        final TitledPane titledPane = new TitledPane();
        titledPane.setText("Title");
        root.getChildren().add(titledPane);

        final String titleBackgroundValue = "#00ff11";
        final ToggleButton button = new ToggleButton("Change");
        button.setOnAction(event -> {
            boolean selected = button.isSelected();

            final Node node = titledPane.lookup(".title");
//            if (selected) {
//                final Color color = Color.valueOf(titleBackgroundValue);
//                ((Region) node).setBackground(new Background(new BackgroundFill(color, null, null)));
//            } else {
//                ((Region) node).setBackground(null);
//                titledPane.applyCss();
//            }
            // sai's answer: set style, not background directly
            if (selected) {
                final Color color = Color.valueOf(titleBackgroundValue);
                node.setStyle("-fx-background-color:#00ff11;");     
            } else {
                node.setStyle(null);
//                titledPane.applyCss();
            }
            // try custom style ... not working, something wrong ..
//            if (selected) {
//                final Color color = Color.valueOf(titleBackgroundValue);
//                node.getStyleClass().add("special");     
//            } else {
//                node.getStyleClass().remove("special");
////                node.setStyle(null);
//            }
//                titledPane.applyCss();
        });

        button.setSelected(false);
        titledPane.setContent(button);

        final Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("light.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("TestApplication");
        primaryStage.show();
    }

}

