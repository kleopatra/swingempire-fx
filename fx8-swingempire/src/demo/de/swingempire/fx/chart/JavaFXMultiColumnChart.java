/*
 * Created on 17.10.2014
 *
 */
package de.swingempire.fx.chart;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

/**
 * Dynamic update of data not working correctly?
 * http://stackoverflow.com/q/26279623/203657
 * 
 * Weird smear of second bar into first.
 * 
 * Unrelated: the reason that the propertyValue isn't committed
 * automatically is that it's not a property! Culprit is
 * bad example in tutorial.
 * 
 * @web http://java-buddy.blogspot.com/
 * 
 * looks okay in 9ea-u175
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JavaFXMultiColumnChart extends Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(JavaFXMultiColumnChart.class.getName());
    public class Record {
        private SimpleStringProperty fieldMonth;

        private SimpleDoubleProperty fieldValue1;

        private SimpleDoubleProperty fieldValue2;

        Record(String fMonth, double fValue1, double fValue2) {
            this.fieldMonth = new SimpleStringProperty(fMonth);
            this.fieldValue1 = new SimpleDoubleProperty(fValue1);
            this.fieldValue2 = new SimpleDoubleProperty(fValue2);
        }

        public StringProperty fieldMonthProperty() {
            return fieldMonth;
        }
        public String getFieldMonth() {
            return fieldMonth.get();
        }
        
        public DoubleProperty fieldValue1Property() {
            return fieldValue1;
        }

        public double getFieldValue1() {
            return fieldValue1.get();
        }

        public DoubleProperty fieldValue2Property() {
            return fieldValue2;
        }
        public double getFieldValue2() {
            return fieldValue2.get();
        }

        public void setFieldMonth(String fMonth) {
            fieldMonth.set(fMonth);
        }

        public void setFieldValue1(Double fValue1) {
            fieldValue1.set(fValue1);
        }

        public void setFieldValue2(Double fValue2) {
            fieldValue2.set(fValue2);
        }
    }

    class MyList {

        ObservableList<Record> dataList;

        ObservableList<XYChart.Data<String, Double>> xyList1;

        ObservableList<XYChart.Data<String, Double>> xyList2;

        MyList() {
            dataList = FXCollections.observableArrayList(record -> {
                return new Observable[] {record.fieldValue1, record.fieldValue2};
            });
            ListChangeListener l = c -> {
                while (c.next()) {
                    if (c.wasUpdated()) {
                        for (int i = c.getFrom(); i < c.getTo(); i++) {
                            Record record = dataList.get(i);
                            // updating the data works fine
                            XYChart.Data data1 = xyList1.get(i);
                            XYChart.Data data2 = xyList2.get(i);
                            data1.setYValue(record.getFieldValue1());
                            data2.setYValue(record.getFieldValue2());
                            // replacing the data can confuse the chart
//                            xyList1.set(i, 
//                                    new XYChart.Data(record.getFieldMonth(), record.getFieldValue1()) );
//                            xyList2.set(i, 
//                                   new XYChart.Data(record.getFieldMonth(), record.getFieldValue2()) );
                        }
                    }
                }
            };
            dataList.addListener(l);
            xyList1 = FXCollections.observableArrayList();
            xyList2 = FXCollections.observableArrayList();
        }

        public void add(Record r) {
            dataList.add(r);
            xyList1.add(new XYChart.Data<>(r.getFieldMonth(), r.getFieldValue1()));
            xyList2.add(new XYChart.Data<>(r.getFieldMonth(), r.getFieldValue2()));
        }

    }

    MyList myList;

    private TableView<Record> tableView = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("java-buddy.blogspot.com");

        // prepare myList
        myList = new MyList();
        myList.add(new Record("January", 100, 120));
        myList.add(new Record("February", 200, 210));
        myList.add(new Record("March", 50, 70));
        myList.add(new Record("April", 75, 50));
        myList.add(new Record("May", 110, 120));
        myList.add(new Record("June", 300, 200));
        myList.add(new Record("July", 111, 100));
        myList.add(new Record("August", 30, 50));
        myList.add(new Record("September", 75, 70));
        myList.add(new Record("October", 55, 50));
        myList.add(new Record("November", 225, 225));
        myList.add(new Record("December", 99, 100));

        Group root = new Group();

        tableView.setEditable(true);

        TableColumn columnMonth = new TableColumn("Month");
        columnMonth
                .setCellValueFactory(new PropertyValueFactory<Record, String>(
                        "fieldMonth"));
        columnMonth.setMinWidth(60);

        TableColumn columnValue1 = new TableColumn("Value 1");
        columnValue1
                .setCellValueFactory(new PropertyValueFactory<Record, Double>(
                        "fieldValue1"));
        columnValue1.setMinWidth(60);

        TableColumn columnValue2 = new TableColumn("Value 2");
        columnValue2
                .setCellValueFactory(new PropertyValueFactory<Record, Double>(
                        "fieldValue2"));
        columnValue2.setMinWidth(60);

        // --- Add for Editable Cell of Value field, in Double
        
        columnValue1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnValue2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // --- Prepare StackedBarChart

        List<String> monthLabels = Arrays.asList("January", "February",
                "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December");

        final CategoryAxis xAxis1 = new CategoryAxis();
        final NumberAxis yAxis1 = new NumberAxis();
        xAxis1.setLabel("Month");
        xAxis1.setCategories(FXCollections
                .<String> observableArrayList(monthLabels));
        XYChart.Series XYSeries1 = new XYChart.Series(myList.xyList1);
        XYSeries1.setName("XYChart.Series 1");
        XYChart.Series XYSeries2 = new XYChart.Series(myList.xyList2);
        XYSeries2.setName("XYChart.Series 2");
        /*
         * final StackedBarChart<String,Number> stackedBarChart = new
         * StackedBarChart<>(xAxis1,yAxis1);
         * stackedBarChart.setTitle("StackedBarChart");
         * stackedBarChart.getData().addAll(XYSeries1, XYSeries2);
         */

        final BarChart<String, Number> barChart = new BarChart<>(xAxis1, yAxis1);
        barChart.setTitle("BarChart");
        barChart.getData().addAll(XYSeries1, XYSeries2);

        tableView.setItems(myList.dataList);
        tableView.getColumns().addAll(columnMonth, columnValue1, columnValue2);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.getChildren().addAll(tableView, barChart);

        root.getChildren().add(hBox);

        primaryStage.setScene(new Scene(root, 750, 400));
        primaryStage.show();
    }

}
