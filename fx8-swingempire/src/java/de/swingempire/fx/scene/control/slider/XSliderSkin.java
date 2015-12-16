/*
 * Created on 04.12.2015
 *
 */
package de.swingempire.fx.scene.control.slider;

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

import java.util.logging.Logger;

import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * Copy of SliderSkin as starting point to experiment with using NumberAxis always.
 * <p>
 * Region/css based skin for Slider
 * <p>
 * PENDING JW:
 * - when/where to call requestLayout on skinnable (or any of the children managed
 * by this)? We can bind the properties of the contained children to the slider's
 * properties. If we did, do we still need to listen to changes 
 * so that we manually request layout?
 */
public class XSliderSkin extends BehaviorSkinBase<Slider, XSliderBehavior> {

    // layout
    private double trackToTickGap = 2;

    private double thumbWidth;
    private double thumbHeight;

    private double trackStart;
    private double trackLength;
    private double thumbTop;
    private double thumbLeft;

    // managed children
    private StackPane thumb;
    private StackPane track;
    private NumberAxis tickLine;
    private boolean trackClicked = false;

    private boolean showTickMarks;

    public XSliderSkin(Slider slider) {
        super(slider, new XSliderBehavior(slider));

        initialize();
        // PENDING JW: why this? Isn't a layout pass automatically triggered
        // after instantiating the skin?
        slider.requestLayout();
        
        // handling snapToTick change (axis has nothing similar)
        registerChangeListener(slider.snapToTicksProperty(), "SNAP_TO_TICKS");
        
        // handling property changes - really needed, even if bound to axis?
        registerChangeListener(slider.minProperty(), "MIN");
        registerChangeListener(slider.maxProperty(), "MAX");
        registerChangeListener(slider.valueProperty(), "VALUE");
        registerChangeListener(slider.orientationProperty(), "ORIENTATION");
        registerChangeListener(slider.showTickMarksProperty(), "SHOW_TICK_MARKS");
        registerChangeListener(slider.showTickLabelsProperty(), "SHOW_TICK_LABELS");
        registerChangeListener(slider.majorTickUnitProperty(), "MAJOR_TICK_UNIT");
        registerChangeListener(slider.minorTickCountProperty(), "MINOR_TICK_COUNT");
//        registerChangeListener(slider.labelFormatterProperty(), "TICK_LABEL_FORMATTER");
    }

    private void initialize() {
        thumb = createThumb();
        track = createTrack();
        tickLine = createTickLine();
        
        getChildren().clear();
        getChildren().addAll(tickLine, track, thumb);
        
        setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        
        track.setOnMousePressed(e -> {
            if (!thumb.isPressed()) {
                trackClicked = true;
                getBehavior().valueUpdateByTrack(getValueFromMouseEvent(e));
                trackClicked = false;
            }
        });
        
        track.setOnMouseDragged(e -> {
            if (!thumb.isPressed()) {
                getBehavior().valueUpdateByTrack(getValueFromMouseEvent(e));
            }
        });

        thumb.setOnMousePressed(e -> {
            getBehavior().thumbPressed(getValueFromMouseEvent(e));
        });

        thumb.setOnMouseReleased(e -> {
            getBehavior().thumbReleased(getValueFromMouseEvent(e));
        });

        thumb.setOnMouseDragged(e -> {
            getBehavior().thumbDragged(getValueFromMouseEvent(e));
        });
        
    }

    protected StackPane createThumb() {
        StackPane thumb = new StackPane() {
            @Override
            public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
                switch (attribute) {
                    case VALUE: return getSkinnable().getValue();
                    default: return super.queryAccessibleAttribute(attribute, parameters);
                }
            }
        };
        thumb.getStyleClass().setAll("thumb");
        thumb.setAccessibleRole(AccessibleRole.THUMB);
        return thumb;
    }

    protected StackPane createTrack() {
        StackPane track = new StackPane();
        track.getStyleClass().setAll("track");
        return track;
    }

    protected NumberAxis createTickLine() {
        Slider slider = getSkinnable();
        NumberAxis tickLine = new NumberAxis();
        tickLine.setAutoRanging(false);
        tickLine.setSide(slider.getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (slider.getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
        tickLine.upperBoundProperty().bind(slider.maxProperty());
//        tickLine.setUpperBound(slider.getMax());
        tickLine.lowerBoundProperty().bind(slider.minProperty());
//        tickLine.setLowerBound(slider.getMin());
        
        tickLine.setTickUnit(slider.getMajorTickUnit());
        // add 1 to the slider minor tick count since the axis draws one
        // less minor ticks than the number given.
        tickLine.setMinorTickCount(Math.max(slider.getMinorTickCount(),0) + 1);
        // JW: cant bind directly, type mismatch
        // tickLine.tickLabelFormatterProperty().bind(slider.labelFormatterProperty());
//        if (slider.getLabelFormatter() != null) {
//            tickLine.setTickLabelFormatter(stringConverterWrapper);
//        }
        /*
         * Conditional binding: use sliders if not null, otherwise
         * let axis' default converter do the job
         */
        ObjectBinding<StringConverter<Number>> b = Bindings
                .when(slider.labelFormatterProperty().isNotNull())
                .then(stringConverterWrapper)
                .otherwise((StringConverter<Number>) null);
        tickLine.tickLabelFormatterProperty().bind(b);
        return tickLine;
    }

    /**
     * Returns the slider value that corresponds to the mouseEvent.
     * The given event is converted to cooredinates of the axis, such that it
     * doesn't matter to which child component it had been delivered originally.
     *  
     * @param e a mouseEvent delivered to any child of the slider (or the slider itself)
     * @return the value that corresponds to the mouse location.
     */
    protected double getValueFromMouseEvent(MouseEvent e) {
        MouseEvent me = e.copyFor(tickLine, tickLine);
        double mouseValue = getSkinnable().getOrientation() == Orientation.HORIZONTAL ? me.getX() : me.getY();
        double curValue = tickLine.getValueForDisplay(mouseValue).doubleValue();
        return curValue;
    }

    /**
     * Called when ever either min, max or value changes, so thumb's layoutX, Y is recomputed.
     * 
     * PENDING JW: would love to use the axis coodinate transformation to position the thumb.
     * works fine except when max (or back to intermediate size) the window:
     * the axis seems to not be relayouted thus showing the thumb at the wrong position.
     * 
     * Needs https://bugs.openjdk.java.net/browse/JDK-8144920 fixed, until then
     * we don't use the axis conversion but keep the manual calc.
     * Added the workaround from the bug-report (calling tickLine.layout() in layoutChildren())
     * seems to do the trick.
     */
    void positionThumb(final boolean animate) {
        
        Slider s = getSkinnable();
        if (s.getValue() > s.getMax()) return;// this can happen if we are bound to something 
        boolean horizontal = s.getOrientation() == Orientation.HORIZONTAL;
        // PENDING JW: use commented lines once the bug on axis is fixed.
        double pixelOnAxis = tickLine.getDisplayPosition(s.getValue());
        double endX = horizontal ? trackStart + pixelOnAxis - thumbWidth/2 : thumbLeft;
        double endY = horizontal ? thumbTop :
            trackStart + pixelOnAxis - thumbWidth/2;

        // commented lines is option 3. as described in layoutChildren
//        final double endX = (horizontal) ? trackStart + (((trackLength * ((s.getValue() - s.getMin()) /
//                (s.getMax() - s.getMin()))) - thumbWidth/2)) : thumbLeft;
//        final double endY = (horizontal) ? thumbTop :
//            snappedTopInset() + trackLength - (trackLength * ((s.getValue() - s.getMin()) /
//                (s.getMax() - s.getMin()))); //  - thumbHeight/2
        
        if (animate) {
            // lets animate the thumb transition
            final double startX = thumb.getLayoutX();
            final double startY = thumb.getLayoutY();
            Transition transition = new Transition() {
                {
                    setCycleDuration(Duration.millis(200));
                }
    
                @Override protected void interpolate(double frac) {
                    if (!Double.isNaN(startX)) {
                        thumb.setLayoutX(startX + frac * (endX - startX));
                    }
                    if (!Double.isNaN(startY)) {
                        thumb.setLayoutY(startY + frac * (endY - startY));
                    }
                }
            };
            transition.play();
        } else {
            thumb.setLayoutX(endX);
            thumb.setLayoutY(endY);
        }
    }

    /**
     * Need the wrapper due to type mismatch: NumberAxis has a converter<Number>
     * slider has converter<Double>
     */
    StringConverter<Number> stringConverterWrapper = new StringConverter<Number>() {
        Slider slider = getSkinnable();
        @Override public String toString(Number object) {
            return(object != null) ? slider.getLabelFormatter().toString(object.doubleValue()) : "";
        }
        @Override public Number fromString(String string) {
            return slider.getLabelFormatter().fromString(string);
        }
    };
    
     private void setShowTickMarks(boolean ticksVisible, boolean labelsVisible) {
        showTickMarks = (ticksVisible || labelsVisible);
        tickLine.setTickLabelsVisible(labelsVisible);
        tickLine.setTickMarkVisible(ticksVisible);
        tickLine.setMinorTickVisible(ticksVisible);
        
        tickLine.setVisible(showTickMarks);
        getSkinnable().requestLayout();
    }


     @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        Slider slider = getSkinnable();
        if ("ORIENTATION".equals(p)) {
            if (/*showTickMarks && */tickLine != null) {
                tickLine.setSide(slider.getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (slider.getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
            }
            getSkinnable().requestLayout();
        } else if ("VALUE".equals(p)) {
            // only animate thumb if the track was clicked - not if the thumb is dragged
            positionThumb(trackClicked);
        } else if ("MIN".equals(p) ) {
//            if (/*showTickMarks && */ tickLine != null) {
//                tickLine.setLowerBound(slider.getMin());
//            }
//            getSkinnable().requestLayout();
        } else if ("MAX".equals(p)) {
//            if (/*showTickMarks && */tickLine != null) {
//                tickLine.setUpperBound(slider.getMax());
//            }
//            getSkinnable().requestLayout();
        } else if ("SHOW_TICK_MARKS".equals(p) || "SHOW_TICK_LABELS".equals(p)) {
            setShowTickMarks(slider.isShowTickMarks(), slider.isShowTickLabels());
        }  else if ("MAJOR_TICK_UNIT".equals(p)) {
            if (tickLine != null) {
                tickLine.setTickUnit(slider.getMajorTickUnit());
                getSkinnable().requestLayout();
            }
        } else if ("MINOR_TICK_COUNT".equals(p)) {
            if (tickLine != null) {
                tickLine.setMinorTickCount(Math.max(slider.getMinorTickCount(),0) + 1);
                getSkinnable().requestLayout();
            }
        } else if ("TICK_LABEL_FORMATTER".equals(p)) {
            // no need to do anything, we bind
//            if (tickLine != null) {
//                if (slider.getLabelFormatter() == null) {
//                    tickLine.setTickLabelFormatter(null);
//                } else {
//                    tickLine.setTickLabelFormatter(stringConverterWrapper);
//                    tickLine.requestAxisLayout();
//                }
//            }
            } else if ("SNAP_TO_TICKS".equals(p)) {
                if (slider.isSnapToTicks()) {
                    slider.adjustValue(slider.getValue());
                }
            }
    }

    /**
     * Layout code unchanged except for 
     * <li> laying out the axis always
     * <li> positioning the thumb at the end of method
     */
    @Override 
    protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
//        LOG.info("layout ...");
         // calculate the available space
        // resize thumb to preferred size
        thumbWidth = snapSize(thumb.prefWidth(-1));
        thumbHeight = snapSize(thumb.prefHeight(-1));
        thumb.resize(thumbWidth, thumbHeight);
        // we are assuming the is common radius's for all corners on the track
        double trackRadius = track.getBackground() == null ? 0 : track.getBackground().getFills().size() > 0 ?
                track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius() : 0;

        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            double tickLineHeight =  (showTickMarks) ? tickLine.prefHeight(-1) : 0;
            double trackHeight = snapSize(track.prefHeight(-1));
            double trackAreaHeight = Math.max(trackHeight,thumbHeight);
            double totalHeightNeeded = trackAreaHeight  + ((showTickMarks) ? trackToTickGap+tickLineHeight : 0);
            double startY = y + ((h - totalHeightNeeded)/2); // center slider in available height vertically
            // calculate offset with thumb guarantees that the thumb fits at min/max
            trackLength = snapSize(w - thumbWidth);
            trackStart = snapPosition(x + (thumbWidth/2));
            double trackTop = (int)(startY + ((trackAreaHeight-trackHeight)/2));
            thumbTop = (int)(startY + ((trackAreaHeight-thumbHeight)/2));

            // layout tick line
            if (tickLine != null) {  //showTickMarks) {
                tickLine.setLayoutX(trackStart);
                tickLine.setLayoutY(trackTop+trackHeight+trackToTickGap);
                tickLine.resize(trackLength, tickLineHeight);
//                tickLine.requestAxisLayout();
            }
//            positionThumb(false);
            // layout track
            track.resizeRelocate((int)(trackStart - trackRadius),
                                 trackTop ,
                                 (int)(trackLength + trackRadius + trackRadius),
                                 trackHeight);
        } else {
            double tickLineWidth = (showTickMarks) ? tickLine.prefWidth(-1) : 0;
            double trackWidth = snapSize(track.prefWidth(-1));
            double trackAreaWidth = Math.max(trackWidth,thumbWidth);
            double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap+tickLineWidth : 0) ;
            double startX = x + ((w - totalWidthNeeded)/2); // center slider in available width horizontally
            trackLength = snapSize(h - thumbHeight);
            trackStart = snapPosition(y + (thumbHeight/2));
            double trackLeft = (int)(startX + ((trackAreaWidth-trackWidth)/2));
            thumbLeft = (int)(startX + ((trackAreaWidth-thumbWidth)/2));

//            positionThumb(false);
            // layout track
            track.resizeRelocate(trackLeft,
                                 (int)(trackStart - trackRadius),
                                 trackWidth,
                                 (int)(trackLength + trackRadius + trackRadius));
            // layout tick line
            if (tickLine != null) { //showTickMarks) {
                tickLine.setLayoutX(trackLeft+trackWidth+trackToTickGap);
                tickLine.setLayoutY(trackStart);
                tickLine.resize(tickLineWidth, trackLength);
//                tickLine.requestAxisLayout();
            } 
        }
        // debugging - keep a little while
        //double pixelOnAxis = tickLine.getDisplayPosition(getSkinnable().getValue());
        
        // ideally we want to use axis api in positioning the thumb
        // works - kindof - the not-working isn't so obvious - for resizing, 
        // doesn't work at all when max/min window (it's blatantly obvious)
        // requires https://bugs.openjdk.java.net/browse/JDK-8144920 to be fixed
        // then the axis' internal state is not yet updated
        // it's only marked invalid, the actual update happens in the next
        // layout pass. 
        // options:
        // 1. force layout by invoking protected method (NumberAxis is final
        // , can't extend
        // 2. delay the thumb locating until next layout (runLater)
        // 3. not use axis in thumb locating
        // this is option 1.
//        if (tickLine != null) {
//            // force internal update ... 
//            forceAxisLayout();
//        }
//        // this is option 2. (move positioning up into
//        Platform.runLater(() -> {
//            // wait with thumb positioning until axis has updated itself
//        });
        // workaround from Vadim (bug report)
        tickLine.layout();
        positionThumb(false);
    }

    double minTrackLength() {
        return 2*thumb.prefWidth(-1);
    }

    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Slider s = getSkinnable();
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            return (leftInset + minTrackLength() + thumb.minWidth(-1) + rightInset);
        } else {
            return(leftInset + thumb.prefWidth(-1) + rightInset);
        }
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Slider s = getSkinnable();
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            double axisHeight = showTickMarks ? (tickLine.prefHeight(-1) + trackToTickGap) : 0;
            return topInset + thumb.prefHeight(-1) + axisHeight + bottomInset;
        } else {
            return topInset + minTrackLength() + thumb.prefHeight(-1) + bottomInset;
        }
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Slider s = getSkinnable();
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            if(showTickMarks) {
                return Math.max(140, tickLine.prefWidth(-1));
            } else {
                return 140;
            }
        } else {
            double axisWidth = showTickMarks ? (tickLine.prefWidth(-1) + trackToTickGap) : 0;
            return leftInset + Math.max(thumb.prefWidth(-1), track.prefWidth(-1)) + axisWidth + rightInset;
        }
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Slider s = getSkinnable();
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            return topInset + Math.max(thumb.prefHeight(-1), track.prefHeight(-1)) +
             ((showTickMarks) ? (trackToTickGap+tickLine.prefHeight(-1)) : 0)  + bottomInset;
        } else {
            if(showTickMarks) {
                return Math.max(140, tickLine.prefHeight(-1));
            } else {
                return 140;
            }
        }
    }

    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return Double.MAX_VALUE;
        } else {
            return getSkinnable().prefWidth(-1);
        }
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return getSkinnable().prefHeight(width);
        } else {
            return Double.MAX_VALUE;
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(XSliderSkin.class
            .getName());
}

