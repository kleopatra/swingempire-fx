/*
 * Created on 25.07.2018
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Problem: label's size at start not correct, only after resizing
 * https://stackoverflow.com/q/51514036/203657
 * 
 * worksforme
 * @author Jeanette Winzenburg, Berlin
 */
public class NestedLayout extends Application {

    public static void main(String[] args) {
        Application.launch(NestedLayout.class);
    }

    @Override
    public void start(Stage primaryStage) {
        final ScrollPane root = new ScrollPane();
        root.setFitToWidth(true);

        VBox vbx = new VBox();
        vbx.setStyle("-fx-background-color: green");
        vbx.setSpacing(20);
        root.setContent(vbx);

        Label lbl = new Label(
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
                "sed diam nonumy eirmod tempor invidunt ut labore et dolore " + 
                "magna aliquyam erat, sed diam voluptua. At vero eos et " +
                "accusam et justo duo dolores et ea rebum.");
        lbl.setStyle("-fx-background-color: yellow");
        lbl.setWrapText(true);

        AnchorPane ap = new AnchorPane();
        ap.setStyle("-fx-background-color: blue");
        ap.getChildren().add(lbl);
        vbx.getChildren().add(ap);

        lbl.maxWidthProperty().bind(ap.widthProperty());
        lbl.setTextAlignment(TextAlignment.JUSTIFY);

        AnchorPane.setLeftAnchor(lbl, 0.0);
        AnchorPane.setTopAnchor(lbl, 0.0);
        AnchorPane.setRightAnchor(lbl, 0.0);
        AnchorPane.setBottomAnchor(lbl, 0.0);

        Scene scene = new Scene(root, 200, 250);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

