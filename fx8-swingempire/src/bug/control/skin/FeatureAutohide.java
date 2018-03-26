/*
 * Created on 27.11.2017
 *
 */
package control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage; 

/**
 * Bug: NPE when dragging slider with tooltip on autohide.
 * https://bugs.openjdk.java.net/browse/JDK-8190411
 * <p>
 * reason is that tooltip autohide consumes the mousePressed and thus dragStart isn't initialized
 * hack around: setConsumeAutoHidingEvents(false). This is also suggested as bug "fix", dec 2017.
 * Side-effect: hiding trigger is passed onto the 
 * slider always
 * 
 * <ul>
 * <li> wait until tooltip visible
 * <li> click into slider
 * <li> consume == true (status now): tootip hidden, nothing else
 * <li> consume == false (suggested bug fix): tooltip hidden, slider value updated
 * </ul>
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
 * it's not in sync with preDrag.
 * 
 * Note: also slight offset due to incorrect offset calculation, 
 * https://bugs.openjdk.java.net/browse/JDK-8089730
 * 
 * @see de.swingempire.fx.control.SliderBug_8089730
 */
public class FeatureAutohide extends Application { 

    /**
     * SliderSkin that guarantees initialization of dragStart.
     */
    public static class SliderInitDragStartInFirstDrag extends SliderSkin {

        private EventHandler<MouseEvent> mousePressedFilter;
        
        public SliderInitDragStartInFirstDrag(Slider slider) {
            super(slider);
            installDragStart();
            getThumb().setStyle("-fx-background-radius: 0.1em, 5em;");
        }

        private void installDragStart() {
            // this does use the very first dragged event to init
//            getThumb().addEventHandler(MouseEvent.MOUSE_DRAGGED, me -> {
//                if (getDragStart() == null) {
//                    initDragStart(me);
//                }
//            });
//            
            // this does use a mouseFilter on the very first pressed - doesn't help
            // not reached before the drag - why not?
//            mousePressedFilter = me -> {
//                LOG.info("handler? ");
//                if (getDragStart() == null) {
//                    initDragStart(me);
//                }
//                getThumb().removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedFilter);
//            };
//            getThumb().addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedFilter);
        }

        
        @Override
        protected void layoutChildren(double x, double y, double width,
                double height) {
            super.layoutChildren(x, y, width, height);
            if (getDragStart() == null) {
                // not quite working - thumb "jumps" a bit on dragging
                double halfWidth = getThumb().getWidth() / 2;
                double layoutX = getThumb().getLayoutX() + halfWidth;
                double layoutY = getThumb().getLayoutY() + halfWidth;
                setDragStart(new Point2D(layoutX, layoutY));
                
                double relative = (getSkinnable().getValue() - getSkinnable().getMin()) /
                        (getSkinnable().getMax() - getSkinnable().getMin());
                setPreDragThumbPos(relative);
           }
        }

        /**
         * @param me
         */
        protected void initDragStart(MouseEvent me) {
            LOG.info("" + me);
            setDragStart(getThumb().localToParent(me.getX(), me.getY()));
            double relative = (getSkinnable().getValue() - getSkinnable().getMin()) /
                    (getSkinnable().getMax() - getSkinnable().getMin());
            setPreDragThumbPos(relative);
        }
        
        protected void setPreDragThumbPos(double pos) {
            FXUtils.invokeSetFieldValue(SliderSkin.class, this, "preDragThumbPos", pos);
            
        }
        protected void setDragStart(Point2D dragStart) {
           FXUtils.invokeSetFieldValue(SliderSkin.class, this, "dragStart", dragStart);
        }
        
        protected Point2D getDragStart() {
            return (Point2D) FXUtils.invokeGetFieldValue(SliderSkin.class, this, "dragStart");
        }
        
        protected StackPane getThumb() {
            return (StackPane) FXUtils.invokeGetFieldValue(SliderSkin.class, this, "thumb");

        }
        
    }
    
    /**
     * SliderSkin suggested as "fix": set consume autoHiding to false in 
     * constructor.
     */
    public static class SliderBugHackSkin extends SliderSkin {

        public SliderBugHackSkin(Slider slider) {
            super(slider);
            if (slider.getTooltip() != null) {
                slider.getTooltip().setConsumeAutoHidingEvents(false);
            }
        }
        
    }
    @Override
    public void start(Stage primaryStage) throws Exception { 
        final BorderPane root = new BorderPane(new Label("wait for tooltip, then start draging.")); 
        final Slider slider = new Slider(1, 5, 2) {

            @Override
            protected Skin<?> createDefaultSkin() {
//                return new SliderBugHackSkin(this);
                return new SliderInitDragStartInFirstDrag(this);
//                return super.createDefaultSkin();
            }
            
        }; 
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(0.5);
        slider.setPadding(new Insets(2, 100, 2, 100));
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        LOG.info("block: " + slider.getBlockIncrement());
        installTooltip(slider); 
        root.setTop(slider); 
  
        slider.valueProperty().addListener((src, ov, nv) -> {
            if (slider.getTooltip() == null) {
                installTooltip(slider);
            }
            LOG.info("dragStart/layoutX: " + getDragStart(slider.getSkin()) 
            + " / " + getThumb(slider.getSkin()).getLayoutX());
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

    /**
     * Installs an auto-hiding Tooltip on the slider.
     * @param slider
     */
    public void installTooltip(final Slider slider) {
        final Tooltip tt = new Tooltip("autohide destroys everything!"); 
        tt.setAutoHide(true); 
        // hack suggested as fix: always let autohiding events reach the slider
//        tt.setConsumeAutoHidingEvents(false);
        slider.setTooltip(tt);
    } 
    
    protected StackPane getThumb(Skin<?> skin) {
        return (StackPane) FXUtils.invokeGetFieldValue(SliderSkin.class, skin, "thumb");

    }

    protected Point2D getDragStart(Skin<?> skin) {
        return (Point2D) FXUtils.invokeGetFieldValue(SliderSkin.class, skin, "dragStart");
    }
    
    public static void main(String[] args) throws Exception { 
        launch(args); 
    } 
     
    
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FeatureAutohide.class.getName());
} 