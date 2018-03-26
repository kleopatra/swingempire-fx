/*
 * Created on 07.03.2018
 *
 */
package de.swingempire.fx.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import static java.util.stream.Collectors.*;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Align x-axis
 * 
 * https://stackoverflow.com/q/49139565/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChartAlign extends Application {

    /**
     * Lays out XYCharts vertically such that their x-axis are aligned and
     * there's enough space to fully show the labels of all y-axis.
     *   
     * @author Jeanette Winzenburg, Berlin
     */
    public static class VChartPane extends Pane {

//        @Override
//        protected double computeMinHeight(double width) {
//            Insets insets = getInsets();
//            return snapSpaceY(insets.getTop()) +
//                   snapSpaceY(sumChartHeights(chart -> chart.minHeight(width))) +
//                   snapSpaceY(insets.getBottom());
//        }
//
//        @Override
//        protected double computePrefHeight(double width) {
//            Insets insets = getInsets();
//            return snapSpaceY(insets.getTop()) +
//                   snapSpaceY(sumChartHeights(chart -> chart.prefHeight(width))) +
//                   snapSpaceY(insets.getBottom());
//        }
//
//        @Override
//        protected double computePrefWidth(double height) {
//            Insets insets = getInsets();
//            return snapSpaceX(insets.getLeft()) +
//                   snapSpaceX(maxChartWidth(chart -> chart.prefWidth(height))) +
//                   snapSpaceX(insets.getRight());
//        }
//
//        @Override
//        protected double computeMinWidth(double height) {
//            Insets insets = getInsets();
//            return snapSpaceX(insets.getLeft()) +
//                    snapSpaceX(maxChartWidth(chart -> chart.minWidth(height))) +
//                    snapSpaceX(insets.getRight());
//        }
//
        @Override
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
            
            for (XYChart xyChart : charts) {
//                xyChart.layout();
                LOG.info("yaxis-1 /200: " + xyChart.getYAxis().prefWidth(-1) 
                        
                        + " / " + xyChart.getYAxis().prefWidth(heightPerChart) 
                        
//                        + " / "  +xyChart.prefWidth(-1)
                        );
            }
            OptionalDouble maxYAxisWidth = charts.stream()
                    .filter(chart -> chart.getYAxis() != null)
                    .mapToDouble(chart -> chart.getYAxis().prefWidth(heightPerChart))
                    .max();
            double maxYWidth = maxYAxisWidth.orElse(0);
            double remainingWidth = availableWidth - maxYWidth;
            LOG.info("remaining: " + remainingWidth);
            for (XYChart c : charts) {
                Axis axis = c.getYAxis();
                double axisWidth = axis != null ? axis.prefWidth(heightPerChart) : 0;
                double axisOffset = maxYWidth - axisWidth + left;
                double xOffset = axisOffset + left;
                double chartWidth = remainingWidth + axisWidth;
                LOG.info("axisWidth/xOffset: " + axisWidth + " / " + axisOffset + " / " 
                + " / " + xOffset + " / " + chartWidth);
//                layoutInArea(c, xOffset, top, chartWidth, 200, 0, HPos.LEFT, VPos.TOP);
                c.resizeRelocate(xOffset, top, chartWidth, heightPerChart);
                top += snapSpaceY(c.getHeight() + getSpacing());
            }
           
        }

        /**
         * @param mapper
         * @return
         */
        protected double sumChartHeights(ToDoubleFunction<XYChart> mapper) {
            List<XYChart> charts = getCharts();
            double chartHeights = charts.stream().mapToDouble(mapper).sum();
            chartHeights += (charts.size() - 1) * getSpacing();
            return chartHeights;
        }
        
        /**
         * @param mapper
         * @return
         */
        protected double maxChartWidth(ToDoubleFunction<XYChart> mapper) {
            List<XYChart> charts = getCharts();
            OptionalDouble maxWidth = charts.stream().mapToDouble(mapper).max();
            return maxWidth.orElse(0);
        }

        protected List<XYChart> getCharts() {
            return getChildren().stream()
                    .filter(child -> child instanceof XYChart)
                    .map(chart -> (XYChart) chart)
                    .collect(toList());
        }
        

        // properties
        /**
         * The amount of vertical space between each child in the vbox.
         * @return the amount of vertical space between each child in the vbox
         */
        public final DoubleProperty spacingProperty() {
            if (spacing == null) {
                spacing = new StyleableDoubleProperty() {
                    @Override
                    public void invalidated() {
                        requestLayout();
                    }

                    @Override
                    public Object getBean() {
                        return VChartPane.this;
                    }

                    @Override
                    public String getName() {
                        return "spacing";
                    }

                    @Override
                    public CssMetaData<VChartPane, Number> getCssMetaData() {
                        return StyleableProperties.SPACING;
                    }
                };
            }
            return spacing;
        }

        private DoubleProperty spacing;
        public final void setSpacing(double value) { spacingProperty().set(value); }
        public final double getSpacing() { return spacing == null ? 0 : spacing.get(); }

        /***************************************************************************
         *                                                                         *
         *                         Stylesheet Handling                             *
         *                                                                         *
         **************************************************************************/

         /*
          * Super-lazy instantiation pattern from Bill Pugh.
          */
         private static class StyleableProperties {
             private static final CssMetaData<VChartPane,Number> SPACING =
                 new CssMetaData<VChartPane,Number>("-fx-spacing",
                     SizeConverter.getInstance(), 0d) {

                @Override
                public boolean isSettable(VChartPane node) {
                    return node.spacing == null || !node.spacing.isBound();
                }

                @Override
                public StyleableProperty<Number> getStyleableProperty(VChartPane node) {
                    return (StyleableProperty<Number>)node.spacingProperty();
                }
            };

             private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
             static {
                final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<CssMetaData<? extends Styleable, ?>>(Region.getClassCssMetaData());
                styleables.add(SPACING);
                STYLEABLES = Collections.unmodifiableList(styleables);
             }
        }

        /**
         * @return The CssMetaData associated with this class, which may include the
         * CssMetaData of its superclasses.
         * @since JavaFX 8.0
         */
        public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
            return StyleableProperties.STYLEABLES;
        }

        /**
         * {@inheritDoc}
         *
         * @since JavaFX 8.0
         */


        @Override
        public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
            return getClassCssMetaData();
        }

    }
    @Override
    public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");
        final CategoryAxis xAxis1 = new CategoryAxis();
        final NumberAxis yAxis1 = new NumberAxis();
        xAxis1.setLabel("Month");
        final LineChart<String, Number> lineChart1 =
                new LineChart<>(xAxis1, yAxis1);

        final CategoryAxis xAxis2 = new CategoryAxis();
        // quick check for rotation: does it help if horizontal space gets crumped?
        // no, going to vertical at the same moment as the normal labels
        // https://stackoverflow.com/q/49130209/203657
        // xAxis2.setTickLabelRotation(45);
        xAxis2.setLabel("Month");
        final NumberAxis yAxis2 = new NumberAxis();
        final LineChart<String, Number> lineChart2 =
                new LineChart<>(xAxis2, yAxis2);

//        lineChart1.setTitle("Charts");
        lineChart1.setLegendVisible(false);
        lineChart2.setLegendVisible(false);

//        lineChart1.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null, null)));
//        lineChart2.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null, null)));
        // hack from answer: rotate ..
//        lineChart1.getYAxis().setTickLabelRotation(270);
//        lineChart2.getYAxis().setTickLabelRotation(270);
        
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Portfolio 1");

        series1.getData().add(new XYChart.Data("Jan", 23));
        series1.getData().add(new XYChart.Data("Feb", 14));
        series1.getData().add(new XYChart.Data("Mar", 15));
        series1.getData().add(new XYChart.Data("Apr", 24));
        series1.getData().add(new XYChart.Data("May", 34));
        series1.getData().add(new XYChart.Data("Jun", 36));
        series1.getData().add(new XYChart.Data("Jul", 22));
        series1.getData().add(new XYChart.Data("Aug", 45));
        series1.getData().add(new XYChart.Data("Sep", 43));
        series1.getData().add(new XYChart.Data("Oct", 17));
        series1.getData().add(new XYChart.Data("Nov", 29));
        series1.getData().add(new XYChart.Data("Dec", 25));

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Portfolio 2");
        series2.getData().add(new XYChart.Data("Jan", 330000));
        series2.getData().add(new XYChart.Data("Feb", 340000));
        series2.getData().add(new XYChart.Data("Mar", 250000));
        series2.getData().add(new XYChart.Data("Apr", 440000));
        series2.getData().add(new XYChart.Data("May", 390000));
        series2.getData().add(new XYChart.Data("Jun", 160000));
        series2.getData().add(new XYChart.Data("Jul", 550000));
        series2.getData().add(new XYChart.Data("Aug", 540000));
        series2.getData().add(new XYChart.Data("Sep", 480000));
        series2.getData().add(new XYChart.Data("Oct", 270000));
        series2.getData().add(new XYChart.Data("Nov", 370000));
        series2.getData().add(new XYChart.Data("Dec", 290000));

        lineChart1.getData().addAll(series1);
        lineChart2.getData().addAll(series2);

        VBox vBox = new VBox();
//        VChartPane vBox = new VChartPane();
//        vBox.getChildren().addAll(lineChart1, lineChart2);

//        VChartPane chartPane = new VChartPane();
//        chartPane.getChildren().addAll(lineChart1, lineChart2);

        VChartBox chartBox = new VChartBox();
        chartBox.getChildren().addAll(lineChart1, lineChart2);
        
        BorderPane content = new BorderPane(vBox);
        
//        Button s = new Button("switch to chartPane");
//        s.setOnAction(e -> {
//            s.setDisable(true);
//            vBox.getChildren().clear();
//            VChartPane chartPane = new VChartPane();
//            chartPane.getChildren().addAll(lineChart1, lineChart2);
//            content.setCenter(chartPane);
//        });
        Button measure = new Button("measure second chart");
        measure.setOnAction(e -> {
            LOG.info("" + lineChart2.getYAxis().prefWidth(-1));
        });
        HBox buttons = new HBox(10, measure);
        content.setBottom(buttons);
        
        Scene scene = new Scene(chartBox, 800, 600);

        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ChartAlign.class.getName());
}

