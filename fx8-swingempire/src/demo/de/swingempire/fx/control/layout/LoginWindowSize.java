/*
 * Created on 28.08.2017
 *
 */
package de.swingempire.fx.control.layout;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Adjust min-size of window to its pref
 * https://stackoverflow.com/q/45905053/203657
 */
public class LoginWindowSize extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("login.css"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        System.out.println("Preferred width before showing: " + root.prefWidth(-1));
        System.out.println("Preferred height before showing: " + root.prefHeight(-1));
        stage.showingProperty().addListener((src, ov, nv) -> resetMinSize(stage, root));
        stage.show();
    }

    /**
     * Solution by James_D, doing in listener here
     * @param root
     */
    private void resetMinSize(Stage stage, Parent root) {
        Scene scene = stage.getScene();
        System.out.println("Preferred width after showing: " + root.prefWidth(-1));
        System.out.println("Preferred height after showing: " + root.prefHeight(-1));
        System.out.println("Actual width: " + scene.getWidth());
        System.out.println("Actual height: " + scene.getHeight());
//        stage.setMinWidth(root.prefWidth(-1));
//        stage.setMinHeight(root.prefHeight(-1));
        
//        Node root = scene.getRoot();
        Bounds rootBounds = root.getBoundsInLocal();
        double deltaW = stage.getWidth() - rootBounds.getWidth();
        double deltaH = stage.getHeight() - rootBounds.getHeight();
        
        Bounds prefBounds = getPrefBounds(root);
        
        stage.setMinWidth(prefBounds.getWidth() + deltaW);
        stage.setMinHeight(prefBounds.getHeight() + deltaH);
        
    }

    private Bounds getPrefBounds(Node node) {
        double prefWidth ;
        double prefHeight ;

        Orientation bias = node.getContentBias();
        if (bias == Orientation.HORIZONTAL) {
            prefWidth = node.prefWidth(-1);
            prefHeight = node.prefHeight(prefWidth);
        } else if (bias == Orientation.VERTICAL) {
            prefHeight = node.prefHeight(-1);
            prefWidth = node.prefWidth(prefHeight);
        } else {
            prefWidth = node.prefWidth(-1);
            prefHeight = node.prefHeight(-1);
        }
        return new BoundingBox(0, 0, prefWidth, prefHeight);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
