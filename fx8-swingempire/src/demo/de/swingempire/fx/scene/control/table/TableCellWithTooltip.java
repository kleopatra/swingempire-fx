/*
 * Created on 04.04.2016
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Per-cell Tooltip http://stackoverflow.com/a/36390173/203657
 */
public class TableCellWithTooltip extends Application {

    private BorderPane testPane;

    class TestPane extends BorderPane {

        public TestPane(List<DataModel> dataItems) {
            TableView<DataModel> tableView = new TableView<DataModel>();
            setCenter(tableView);
            TableColumn<DataModel, ClassInfo> column1 = new TableColumn<DataModel, ClassInfo>(
                    "column 1");
            TableColumn<DataModel, ClassInfo> column2 = new TableColumn<DataModel, ClassInfo>(
                    "column 2");
            column1.setCellValueFactory(
                    new PropertyValueFactory<DataModel, ClassInfo>(
                            "column1Data"));
            column2.setCellValueFactory(
                    new PropertyValueFactory<DataModel, ClassInfo>(
                            "column2Data"));
            column1.setCellFactory(column -> new CustomTableCell());
            column2.setCellFactory(column -> new CustomTableCell());
            tableView.getColumns().addAll(column1, column2);
            tableView.setItems(FXCollections.observableList(dataItems));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class CustomTableCell extends TableCell<DataModel, ClassInfo> {
        @Override
        protected void updateItem(ClassInfo item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item != null) {
                    setText(item.getName());
                    setTooltip(new Tooltip(item.getDescription()));
                }
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Task Progress Tester");
        List<DataModel> dataItems = createItems();
        testPane = new TestPane(dataItems);
        primaryStage.setScene(new Scene(testPane, 300, 250));
        primaryStage.show();
    }

    /**
     * @return
     */
    private List<DataModel> createItems() {
        List<DataModel> dataItems = 
                IntStream.range(0, 20).mapToObj(this::createData).collect(Collectors.toList());
        return dataItems;
    }

    /**
     * @param i 
     * @return
     */
    private DataModel createData(int i) {
        DataModel row1Data = new DataModel();
        row1Data.setColumn1Data(new ClassInfo("row" + i  + "Col1Name",
                "This is the description for Row" + i + ", Column 1"));
        row1Data.setColumn2Data(new ClassInfo("row" + i + "Col2Name",
                "This is the description for Row" + i + ", Column 2"));
        return row1Data;
    }

    public class DataModel {
        private ObjectProperty<ClassInfo> column1Data = new SimpleObjectProperty<ClassInfo>();

        private ObjectProperty<ClassInfo> column2Data = new SimpleObjectProperty<ClassInfo>();

        public void setColumn2Data(ClassInfo newValue) {
            column2Data.set(newValue);
        }

        public void setColumn1Data(ClassInfo newValue) {
            column1Data.set(newValue);
        }

        public ObjectProperty<ClassInfo> column1DataProperty() {
            return column1Data;
        }
        public ObjectProperty<ClassInfo> column2DataProperty() {
            return column2Data;
        }
    }

    public class ClassInfo {
        private String name;

        private String description;

        public ClassInfo(String name, String description) {
            this.setName(name);
            this.setDescription(description);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
