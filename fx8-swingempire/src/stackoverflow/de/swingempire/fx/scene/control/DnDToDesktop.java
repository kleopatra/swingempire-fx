/*
 * Created on 09.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

/**
 * dnd to desktop not working on win10
 * https://stackoverflow.com/q/51759269/203657
 * 
 * reproducible ... but should it? reported to work in mac
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DnDToDesktop extends Application {

    @Override
    public void start(Stage primaryStage) {

        Tab tab = new Tab();
        tab.setGraphic(new Label("Drag me"));

        tab.getGraphic().setOnDragDetected(e -> {
            System.out.println("Drag detected");
            SnapshotParameters param = new SnapshotParameters();
            param.setTransform(Transform.scale(2, 2));
            WritableImage image = tab.getGraphic().snapshot(param, null);
            Dragboard dragboard = tab.getGraphic().startDragAndDrop(TransferMode.MOVE);
            dragboard.setDragView(image);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString("");
            dragboard.setContent(clipboardContent);
        });

        tab.getGraphic().setOnMouseReleased(e -> {
            System.out.println("Mouse Released");
        });

        TabPane tabpane = new TabPane();
        tabpane.getTabs().add(tab);

        StackPane stackpane = new StackPane();
        stackpane.getChildren().add(tabpane);

        Scene scene = new Scene(stackpane, 500, 250);
        primaryStage.setTitle("Drag to OS Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

