/*
 * Created on 27.11.2017
 *
 */
package control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage; 

/**
 * Bug: NPE when dragging slider with tooltip on autohide.
 * https://bugs.openjdk.java.net/browse/JDK-8190411
 * 
 * reason is that tooltip autohide consumes the mousePressed and thus dragStart isn't initialized
 * hack around: setConsumeAutoHidingEvents(false) - not checked for side-effects.
 * 
 * <p>
 * actually, dragStart is not important, as long as it is !null and its value is in sync
 * with preDragThumbPos: the point is the absolute px-position in parent, the preDrag is
 * its corresponding relative value on the slider. On dragging, the delta of current
 * px-position to dragStart is relativized to track length and then added to preDrag, 
 * thus giving the correct relative value again ...
 * 
 * preDrag + (curX - dragStartX)/length
 * 
 * Hmm ... setting the dragStart to 0, 0 makes the thumb jump on dragging, because
 * it's not in sync with preDrag
 */
public class FeatureAutohide extends Application { 

    public static void main(String[] args) throws Exception { 
        launch(args); 
    } 
     
    public void start(Stage primaryStage) throws Exception { 
        final BorderPane root = new BorderPane(new Label("wait for tooltip, then start draging.")); 
        final Slider slider = new Slider(1, 5, 2); 
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(0.5);
        slider.setPadding(new Insets(2, 100, 2, 100));
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        LOG.info("block: " + slider.getBlockIncrement());
        final Tooltip tt = new Tooltip("autohide destroys everything!"); 
        tt.setAutoHide(true); 
//        tt.setConsumeAutoHidingEvents(false);
        slider.setTooltip(tt); 
        root.setTop(slider); 
  
        slider.valueProperty().addListener((src, ov, nv) -> {
            LOG.info("dragStart: " + getDragStart(slider.getSkin()));
        });
        Label lbl = new Label();
        lbl.textProperty().bind(slider.valueProperty().asString());
        root.setBottom(lbl);
        
        primaryStage.setScene(new Scene(root, 800, 200)); 
        primaryStage.setTitle("Autodestroyer"); 
        primaryStage.centerOnScreen(); 
        primaryStage.show(); 
        
//        FXUtils.invokeSetFieldValue(SliderSkin.class, slider.getSkin(), "dragStart", new Point2D(0, 0));
    } 
    
    protected Point2D getDragStart(Skin<?> skin) {
//        if (skin == null) return null;
        return (Point2D) FXUtils.invokeGetFieldValue(SliderSkin.class, skin, "dragStart");
    }
    
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FeatureAutohide.class.getName());
} 