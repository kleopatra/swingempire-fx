/*
 * Created on 29.01.2020
 *
 */
package de.swingempire.fx.collection;

import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static de.swingempire.fx.util.FXUtils.*;
import static javafx.collections.FXCollections.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.ListChangeReport;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * Trying to dig into slight weirdness of Series/XYData
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ChartDataTest {

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    private ObservableList<XYChart.Data<String, Number>> baseData;
    private XYChart.Data<String, Number> baseFirst;
    
    private ObservableList<XYChart.Data<String, Number>> seriesData ; //xExtractorData;
    private Comparator<XYChart.Data<String, Number>> ascendingComparator;
    private Comparator<XYChart.Data<String, Number>> descendingComparator;
    
    
    
    @Test
    public void testSortedInSeriesAndChartReverse() {
        SortedList descending = new SortedList(seriesData, descendingComparator);
        Series<String, Number> series = new Series<>(descending);
        BarChart<String, Number> chart = new BarChart<>(
                new CategoryAxis(), new NumberAxis(),
                singletonObservableList(series));
        assertEquals(descending.size() - 1, descending.indexOf(baseFirst));
        List<Data> displayData = (List<Data>) invokeGetFieldValue(Series.class, series, "displayedData");
        assertEquals(descending.size(), displayData.size());
        ListChangeReport report = new ListChangeReport(descending);
        descending.setComparator(ascendingComparator);
        assertEquals(0, descending.indexOf(baseFirst));
        assertEquals(0, displayData.indexOf(baseFirst));
    }

    
    /**
     * displayData not updated on update notification
     */
    @Test
    public void testSortedDisplayData() {
        SortedList descending = new SortedList(seriesData, descendingComparator);
        Series<String, Number> series = new Series<>(descending);
        
        BarChart<String, Number> chart = new BarChart<>(
                new CategoryAxis(), new NumberAxis(),
                singletonObservableList(series));
        assertEquals(descending.size() - 1, descending.indexOf(baseFirst));
        List<Data> displayData = (List<Data>) invokeGetFieldValue(Series.class, series, "displayedData");
        assertEquals(descending.size(), displayData.size());
        assertEquals(displayData.size() - 1, displayData.indexOf(baseFirst));
        ListChangeReport report = new ListChangeReport(descending);
        baseFirst.setXValue("xlast");
        assertEquals(0, descending.indexOf(baseFirst));
        assertEquals(0, displayData.indexOf(baseFirst));
    }
    
    /**
     * Does not blow ... why not? handles the permutation (first subchange) without
     * notifying the chart?
     */
    @Test
    public void testSortedInSeriesAndChart() {
        SortedList descending = new SortedList(seriesData, descendingComparator);
        Series<String, Number> series = new Series<>(descending);
        
        BarChart<String, Number> chart = new BarChart<>(
                new CategoryAxis(), new NumberAxis(),
                singletonObservableList(series));
        assertEquals(descending.size() - 1, descending.indexOf(baseFirst));
        ListChangeReport report = new ListChangeReport(descending);
        baseFirst.setXValue("xlast");
        assertEquals(0, descending.indexOf(baseFirst));
    }
    
    /**
     * fails with duplicate added - due to XYChart.dataItemsChanged cannot 
     * cope with updates
     */
    @Test
    public void testDataInChart() {
        Series<String, Number> series = new Series<>(seriesData);
        BarChart<String, Number> chart = new BarChart<>(
                new CategoryAxis(), new NumberAxis(),
                singletonObservableList(series));
        ListChangeReport report = new ListChangeReport(seriesData);
        baseFirst.setXValue("xlast");
        assertEquals("single change with extractor", 1, report.getEventCount());
        assertTrue( wasSingleUpdated(report.getLastChange()));
    }   
    
    @Test
    public void testDataInSeries() {
        Series series = new Series(baseData);
        baseData.forEach(data -> {
            Series onData = (Series) invokeGetFieldValue(Data.class, data, "series");
            assertSame(series, onData);
        });
    }
    
    @Test
    public void testChangeXValueUpdateSortedDescending() {
        SortedList descending = new SortedList(seriesData, descendingComparator);
        assertEquals(descending.size() -1, descending.indexOf(baseFirst));
        ListChangeReport report = new ListChangeReport(descending);
        baseFirst.setXValue("last");
        report.prettyPrint();
        assertEquals(1, report.getEventCount());
        assertEquals(0, descending.indexOf(baseFirst));
        // this assumption is wrong:
        // if the update involves a permutation change, the change from the sortedList
        // has two subChanges: 1 permutation plus 1 for the original update (at its new index)
        //assertTrue(wasSinglePermutated(report.getLastChange()));
    }
    
    @Test
    public void testChangeXValueUpdateSorted() {
        SortedList ascending = new SortedList(seriesData, ascendingComparator);
        ListChangeReport report = new ListChangeReport(ascending);
        baseFirst.setXValue("last");
        assertEquals(1, report.getEventCount());
        assertEquals(ascending.size() -1, ascending.indexOf(baseFirst));
        // this assumption is wrong:
        // if the update involves a permutation change, the change from the sortedList
        // has two subChanges: 1 permutation plus 1 for the original update (at its new index)
        //assertTrue(wasSinglePermutated(report.getLastChange()));
    }
    
    @Test
    public void testDescendingXData() {
        reverse(baseData);
        SortedList descending = new SortedList(seriesData, descendingComparator);
        assertEquals(baseData, descending);
    }
    
    @Test
    public void testAscendingXData() {
        reverse(seriesData);
        SortedList ascending = new SortedList(seriesData, ascendingComparator);
        assertEquals(baseData, ascending);
    }
    
    @Test
    public void testChangeXValue() {
        ListChangeReport report = new ListChangeReport(seriesData);
        baseFirst.setXValue("xlast");
        assertEquals("single change with extractor", 1, report.getEventCount());
        assertTrue( wasSingleUpdated(report.getLastChange()));
    }
    
    /**
     * Without extractor, there's no change event
     */
    @Test
    public void testChangeXValueNoExtractor() {
        ListChangeReport report = new ListChangeReport(baseData);
        baseFirst.setXValue("xlast");
        assertEquals("no change without extract", 0, report.getEventCount());
    }
    @Before
    public void setUp() {
        baseData = observableArrayList(
                new XYChart.Data<>("alpha", 0),
                new XYChart.Data<>("bravo", 1),
                new XYChart.Data<>("charlie", 2),
                new XYChart.Data<>("delta", 3)
                );
        baseFirst = baseData.get(0);
        // need an extractor 
        seriesData = 
                observableArrayList(data -> new Observable[] {data.XValueProperty()});
        seriesData.addAll(baseData);
        
        ascendingComparator = Comparator
                .comparing(XYChart.Data::getXValue);
        descendingComparator = ascendingComparator.reversed();
    }
}
