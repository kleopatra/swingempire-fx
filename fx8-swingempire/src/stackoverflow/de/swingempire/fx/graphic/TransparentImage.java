/*
 * Created on 24.08.2018
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * dragging possible only on non-transparent parts
 * https://stackoverflow.com/q/51991137/203657
 * 
 * ux?
 * but possible (from mipa): pickOnBounds picks inside the bounds
 *  of a node, false by default which picks inside the shape
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TransparentImage extends Application{

    double initMx, initMy,initX, initY;

    @Override
    public void start(Stage ps) throws Exception {
        StackPane pane = new StackPane();
        Image im = new Image("/de/swingempire/fx/graphic/transparent.png");
        ImageView view = new ImageView(im);
        double fact = im.getWidth() / im.getHeight();

        view.setFitHeight(300);
        view.setFitWidth(300 * fact);

        view.setOnMousePressed(e->{
            initX = view.getTranslateX();
            initY = view.getTranslateY();
            initMx = e.getSceneX();
            initMy = e.getSceneY();
        });

        view.setOnMouseDragged(e->{
            double dx = initMx - e.getSceneX();
            double dy = initMy - e.getSceneY();

            double nx = initX - dx;
            double ny = initY - dy;

            view.setTranslateX(nx);
            view.setTranslateY(ny);

        });
        
        // suggested by mipa
        view.setPickOnBounds(true);
        pane.getChildren().add(view);

        Scene scene = new Scene(pane, 500, 500);

        ps.setScene(scene);
        ps.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

