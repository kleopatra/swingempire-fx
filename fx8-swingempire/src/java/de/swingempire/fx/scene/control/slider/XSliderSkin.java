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
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import com.sun.javafx.scene.control.behavior.SliderBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * Copy of SliderSkin as starting point to experiment with using NumberAxis always.
 * 
 * Region/css based skin for Slider
*/
public class XSliderSkin extends BehaviorSkinBase<Slider, XSliderBehavior> {

    /** Track if slider is vertical/horizontal and cause re layout */
//    private boolean horizontal;
    private NumberAxis tickLine = null;
    private double trackToTickGap = 2;

    private boolean showTickMarks;
    private double thumbWidth;
    private double thumbHeight;

    private double trackStart;
    private double trackLength;
    private double thumbTop;
    private double thumbLeft;
    private double preDragThumbPos;
    private Point2D dragStart; // in skin coordinates

    private StackPane thumb;
    private StackPane track;
    private boolean trackClicked = false;
//    private double visibleAmount = 16;

    public XSliderSkin(Slider slider) {
        super(slider, new XSliderBehavior(slider));

        initialize();
        slider.requestLayout();
        registerChangeListener(slider.minProperty(), "MIN");
        registerChangeListener(slider.maxProperty(), "MAX");
        registerChangeListener(slider.valueProperty(), "VALUE");
        registerChangeListener(slider.orientationProperty(), "ORIENTATION");
        registerChangeListener(slider.showTickMarksProperty(), "SHOW_TICK_MARKS");
        registerChangeListener(slider.showTickLabelsProperty(), "SHOW_TICK_LABELS");
        registerChangeListener(slider.majorTickUnitProperty(), "MAJOR_TICK_UNIT");
        registerChangeListener(slider.minorTickCountProperty(), "MINOR_TICK_COUNT");
        registerChangeListener(slider.labelFormatterProperty(), "TICK_LABEL_FORMATTER");
    }

    private void initialize() {
        thumb = new StackPane() {
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
        track = new StackPane();
        track.getStyleClass().setAll("track");
//        horizontal = getSkinnable().isVertical();

        getChildren().clear();
        getChildren().addAll(track, thumb);
        setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        
//        MouseEvent e;
//        tickLine.setOnMousePressed(me -> {
//            trackClicked = true;
//            if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
//                double curValue = tickLine.getValueForDisplay(me.getX()).doubleValue();
//                double position = curValue / (getSkinnable().getMax() - getSkinnable().getMin());
//                getSkinnable().adjustValue(curValue);
////                getBehavior().trackPress(me, position);
//            }
//            trackClicked = false;
//        });
        track.setOnMousePressed(e -> {
            if (!thumb.isPressed()) {
                trackClicked = true;
                
                double curValue = getValueFromMouseEvent(e);
                // could do it directly (or enhance SkinBehaviour to take the value
//                getSkinnable().adjustValue(curValue);
                // delegate to XSliderBehavior (does additional stuff like guaranteing focus
                getBehavior().valueUpdateByTrack(curValue);
                // roundabout way ... give the relative position
//                double position = (curValue - getSkinnable().getMin()) / (getSkinnable().getMax() - getSkinnable().getMin());
//                getBehavior().trackPress(me, position);
                trackClicked = false;
            }
        });
        
        track.setOnMouseDragged(e -> {
            if (!thumb.isPressed()) {

                double curValue = getValueFromMouseEvent(e);
                // could do it directly (or enhance SkinBehaviour to take the value
//                getSkinnable().adjustValue(curValue);
                // delegate to XSliderBehavior (does additional stuff like guaranteing focus
                getBehavior().valueUpdateByTrack(curValue);
                // roundabout way ... give the relative position
//                double position = (curValue - getSkinnable().getMin()) / (getSkinnable().getMax() - getSkinnable().getMin());
//                getBehavior().trackPress(me, position);

                //                if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
//                    getBehavior().trackPress(me, (me.getX() / trackLength));
//                } else {
//                    getBehavior().trackPress(me, (me.getY() / trackLength));
//                }
            }
        });

        thumb.setOnMousePressed(e -> {
            double curValue = getValueFromMouseEvent(e);
            getBehavior().thumbPressed(curValue);
//            getBehavior().thumbPressed(me, 0.0f);
//            dragStart = thumb.localToParent(me.getX(), me.getY());
//            preDragThumbPos = (getSkinnable().getValue() - getSkinnable().getMin()) /
//                    (getSkinnable().getMax() - getSkinnable().getMin());
        });

        thumb.setOnMouseReleased(e -> {
            double curValue = getValueFromMouseEvent(e);
            getBehavior().thumbReleased(curValue);
//            getBehavior().thumbReleased(me);
        });

        thumb.setOnMouseDragged(e -> {
            double curValue = getValueFromMouseEvent(e);
            getBehavior().thumbDragged(curValue);

//            Point2D cur = thumb.localToParent(me.getX(), me.getY());
//            double dragPos = (getSkinnable().getOrientation() == Orientation.HORIZONTAL)?
//                cur.getX() - dragStart.getX() : -(cur.getY() - dragStart.getY());
//            getBehavior().thumbDragged(me, preDragThumbPos + dragPos / trackLength);
        });
    }

    protected double getValueFromMouseEvent(MouseEvent e) {
        MouseEvent me = e.copyFor(tickLine, tickLine);
        double mouseValue = getSkinnable().getOrientation() == Orientation.HORIZONTAL ? me.getX() : me.getY();
        double curValue = tickLine.getValueForDisplay(mouseValue).doubleValue();
        return curValue;
    }

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
        Slider slider = getSkinnable();
        if (tickLine == null) {
            // initial setup
            tickLine = createAxis(ticksVisible, labelsVisible);
            getChildren().clear();
            getChildren().addAll(tickLine, track, thumb);
        }
        tickLine.setTickLabelsVisible(labelsVisible);
        tickLine.setTickMarkVisible(ticksVisible);
        tickLine.setMinorTickVisible(ticksVisible);
        
        tickLine.setVisible(showTickMarks);
        getSkinnable().requestLayout();
    }

    protected NumberAxis createAxis(boolean ticksVisible, boolean labelsVisible) {
        Slider slider = getSkinnable();
        NumberAxis tickLine = new NumberAxis();
        tickLine.setAutoRanging(false);
        tickLine.setSide(slider.getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (slider.getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
        tickLine.setUpperBound(slider.getMax());
        tickLine.setLowerBound(slider.getMin());
        tickLine.setTickUnit(slider.getMajorTickUnit());
//        tickLine.setTickLabelsVisible(labelsVisible);
//        tickLine.setTickMarkVisible(ticksVisible);
//        tickLine.setMinorTickVisible(ticksVisible);
        // add 1 to the slider minor tick count since the axis draws one
        // less minor ticks than the number given.
        tickLine.setMinorTickCount(Math.max(slider.getMinorTickCount(),0) + 1);
        if (slider.getLabelFormatter() != null) {
            tickLine.setTickLabelFormatter(stringConverterWrapper);
        }
        return tickLine;
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
            if (/*showTickMarks && */ tickLine != null) {
                tickLine.setLowerBound(slider.getMin());
            }
            getSkinnable().requestLayout();
        } else if ("MAX".equals(p)) {
            if (/*showTickMarks && */tickLine != null) {
                tickLine.setUpperBound(slider.getMax());
            }
            getSkinnable().requestLayout();
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
            if (tickLine != null) {
                if (slider.getLabelFormatter() == null) {
                    tickLine.setTickLabelFormatter(null);
                } else {
                    tickLine.setTickLabelFormatter(stringConverterWrapper);
                    tickLine.requestAxisLayout();
                }
            }
        }
    }

    /**
     * Called when ever either min, max or value changes, so thumb's layoutX, Y is recomputed.
     */
    void positionThumb(final boolean animate) {
        Slider s = getSkinnable();
        if (s.getValue() > s.getMax()) return;// this can happen if we are bound to something 
        boolean horizontal = s.getOrientation() == Orientation.HORIZONTAL;
        final double endX = (horizontal) ? trackStart + (((trackLength * ((s.getValue() - s.getMin()) /
                (s.getMax() - s.getMin()))) - thumbWidth/2)) : thumbLeft;
        final double endY = (horizontal) ? thumbTop :
            snappedTopInset() + trackLength - (trackLength * ((s.getValue() - s.getMin()) /
                (s.getMax() - s.getMin()))); //  - thumbHeight/2
        
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

    @Override protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
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
//            trackLength = snapSize(w - 2 * trackRadius);
//            trackStart = snapPosition(x + trackRadius);
            double trackTop = (int)(startY + ((trackAreaHeight-trackHeight)/2));
            thumbTop = (int)(startY + ((trackAreaHeight-thumbHeight)/2));

            positionThumb(false);
            // layout track
            track.resizeRelocate((int)(trackStart - trackRadius),
                                 trackTop ,
                                 (int)(trackLength + trackRadius + trackRadius),
                                 trackHeight);
            // layout tick line
            if (tickLine != null) {  //showTickMarks) {
                tickLine.setLayoutX(trackStart);
                tickLine.setLayoutY(trackTop+trackHeight+trackToTickGap);
                tickLine.resize(trackLength, tickLineHeight);
                tickLine.requestAxisLayout();
            }
//            else {
//                if (tickLine != null) {
//                    tickLine.resize(0,0);
//                    tickLine.requestAxisLayout();
//                }
//                tickLine = null;
//            }
        } else {
            double tickLineWidth = (showTickMarks) ? tickLine.prefWidth(-1) : 0;
            double trackWidth = snapSize(track.prefWidth(-1));
            double trackAreaWidth = Math.max(trackWidth,thumbWidth);
            double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap+tickLineWidth : 0) ;
            double startX = x + ((w - totalWidthNeeded)/2); // center slider in available width horizontally
//            trackLength = snapSize(h - thumbHeight);
//            trackStart = snapPosition(y + (thumbHeight/2));
            trackLength = snapSize(h - 2* trackRadius);
            trackStart = snapPosition(y + trackRadius);
            double trackLeft = (int)(startX + ((trackAreaWidth-trackWidth)/2));
            thumbLeft = (int)(startX + ((trackAreaWidth-thumbWidth)/2));

            positionThumb(false);
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
                tickLine.requestAxisLayout();
            } 
//            else {
//                if (tickLine != null) {
//                    tickLine.resize(0,0);
//                    tickLine.requestAxisLayout();
//                }
//                tickLine = null;
//            }
        }
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

