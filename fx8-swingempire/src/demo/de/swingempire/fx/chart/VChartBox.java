/*
 * Created on 10.03.2018
 *
 */
package de.swingempire.fx.chart;

import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Logger;

import static java.util.stream.Collectors.*;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;

/**
 * Lays out XYCharts vertically such that their x-axis are aligned and
 * there's enough space to fully show the labels of all y-axis.
 * 
 * https://stackoverflow.com/q/49139565/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class VChartBox extends Pane {
    
    protected void layoutChildren() {
        Insets insets = getInsets();
        double width = getWidth();
        double height = getHeight();
        double top = snapSpaceY(insets.getTop());
        double left = snapSpaceX(insets.getLeft());
        double bottom = snapSpaceY(insets.getBottom());
        double right = snapSpaceX(insets.getRight());
        double space = snapSpaceY(getSpacing());
        
        double availableWidth = snapSpaceX(width - left - right);
        List<XYChart> charts = getCharts();
        if (charts.isEmpty()) return;
        double heightPerChart = height / charts.size() - space;
        OptionalDouble maxYAxisWidth = charts.stream()
                .filter(chart -> chart.getYAxis() != null)
                .mapToDouble(chart -> chart.getYAxis().prefWidth(heightPerChart))
                .max();
        double maxYWidth = maxYAxisWidth.orElse(0);
        double remainingWidth = availableWidth - maxYWidth;
        for (XYChart c : charts) {
            Axis axis = c.getYAxis();
            double axisWidth = axis != null ? axis.prefWidth(heightPerChart) : 0;
            double axisOffset = maxYWidth - axisWidth;
            double xOffset = axisOffset + left;
            double chartWidth = remainingWidth + axisWidth;
            c.resizeRelocate(xOffset, top, chartWidth, heightPerChart);
            top += snapSpaceY(c.getHeight() + getSpacing());
        }
    }

    protected List<XYChart> getCharts() {
        return getChildren().stream().filter(child -> child instanceof XYChart)
                .map(chart -> (XYChart) chart).collect(toList());
    }
        
    // properties
    /**
     * The amount of vertical space between each child in the vbox.
     * 
     * @return the amount of vertical space between each child in the vbox
     */
    public final DoubleProperty spacingProperty() {
        if (spacing == null) {
            spacing = new SimpleDoubleProperty(this, "spacing", 20) {
                @Override
                public void invalidated() {
                    requestLayout();
                }

            };
        }
        return spacing;
    }

    private DoubleProperty spacing;

    public final void setSpacing(double value) {
        spacingProperty().set(value);
    }

    public final double getSpacing() {
        return spacing == null ? 0 : spacing.get();
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(VChartBox.class.getName());
}
