/*
 * Created on 19.04.2020
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/61249441/203657
 * show scrollbar only if needed
 */
public class SplitPaneScrollerApp extends Application {

    SplitPaneScroller controller;
    
    
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("splitpanescroller.fxml"));
        ScrollPane pane = loader.load();
        controller = loader.getController();
        primaryStage.setScene(new Scene(pane, 800, 600));
        primaryStage.show();
        TableView tableView = controller.tableView;
        SplitPane splitPane = controller.splitPane;
        int size = splitPane.getChildrenUnmodifiable().size();
        Region last = controller.last; //(Region) splitPane.getChildrenUnmodifiable().get(size -1);
        System.out.println(last);
        last.setBackground(new Background(new BackgroundFill(Color.FIREBRICK, 
                CornerRadii.EMPTY, Insets.EMPTY)));
        Pane parent = (Pane) tableView.getParent();
        System.out.println(
                "table height/pref: " + tableView.getHeight() + " / "
            + tableView.prefHeight(-1)
            + "\n"
            +  "last height/pref: " + last.getHeight() + " / "
            + parent.prefHeight(-1)
            + "\n"
          +  "splitpane height/pref: " + splitPane.getHeight() + " / "
        + splitPane.prefHeight(-1)
        + "\n"
                );
    }

    public static void main(String[] args) {
        launch(args);
    }
}