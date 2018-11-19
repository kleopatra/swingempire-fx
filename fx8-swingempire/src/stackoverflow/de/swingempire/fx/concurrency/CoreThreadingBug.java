/*
 * Created on 13.11.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Throws IOOB
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8163078
 * related:
 * https://bugs.openjdk.java.net/browse/JDK-8198577
 * (the latter was tracked to an app bug as well, some property
 * wasn't updated on the fx thread)
 */
public class CoreThreadingBug extends Application { 
    public static void main(String args[]) { 
        launch(); 
    } 

    @Override
    public void start(Stage primaryStage){ 
        testLoop();
        Text t = new Text("x"); 
        HBox h = new HBox(t); 
        Group g = new Group(h); 

        Line l = new Line(); 
        DoubleBinding innerWidth = javafx.beans.binding.Bindings.createDoubleBinding( 
                () -> g.layoutBoundsProperty().get().getWidth(), 
                g.layoutBoundsProperty()); 
        l.endXProperty().bind(innerWidth); 

        VBox v = new VBox(l, g); 
        v.boundsInLocalProperty().addListener((a,b,c) -> {}); 

        Group g2 = new Group(v); 
        g2.layoutBoundsProperty().addListener((a,b,c) -> {}); 

        // failing computation in parent.updateCachedBounds is triggered by this remove
//        h.getChildren().remove(t); 

        Platform.exit(); 
    }

    /**
     * 
     */
    private void testLoop() {
        // this is the loop in parent.updateCachedBounds that seems
        // to be whacky - but not here
        List<Integer> list = List.of(0, 1, 2, 3, 4, 5);
        int remaining = 5;
        for (int i = list.size() - 1; remaining > 0; i--) {
            LOG.info("" + i + "/ " + list.get(i));
            --remaining;
        }
        
    } 
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CoreThreadingBug.class.getName());
}
