/*
 * Created on 23.03.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.cell.TableAndComboMixedTypes.TemplateColumn;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * SO_ABANDONED
 * https://stackoverflow.com/q/49431347/203657
 * mixing types: templates in combo vs. String in column
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableAndComboMixedTypes extends Application {

    /**
     * @return
     */
    private Parent createContent() {
        initialize();
        
        Button button = new Button("log");
        button.setOnAction(e -> {
            TemplateColumn item = table.getSelectionModel().getSelectedItem();
            if (item == null) return;
            LOG.info("row: " + item + item.getDestinationColumnIndex());
        });
        HBox buttons = new HBox(10, button);
        BorderPane pane = new BorderPane(table);
        pane.setBottom(buttons);
        return pane;
    }

//    @FXML
    private TableView<TemplateColumn> table;
//    @FXML
    private TableColumn<TemplateColumn, Integer> colColumnIndex;
//    @FXML
    private TableColumn<TemplateColumn, String> colColumnName;
//    @FXML
    private TableColumn<TemplateColumn, Integer> destinationIndexColumn;
    private TableColumn<TemplateColumn, TemplateColumn> colMappedTo;

    private Template sourceTemplate;
    private Template destinationTemplate;

//    @FXML
    private void initialize() {

        createSampleData();

        colColumnIndex = new TableColumn<>("column index");
        colColumnName = new TableColumn<>("column name");
        colMappedTo = new TableColumn<>("mapped to");
        destinationIndexColumn = new TableColumn<>("destination index");
        
        // Initialize the table
        colColumnIndex.setCellValueFactory(new PropertyValueFactory<>("columnIndex"));
        colColumnName.setCellValueFactory(new PropertyValueFactory<>("columnName"));
        destinationIndexColumn.setCellValueFactory(new PropertyValueFactory<>("destinationColumnIndex"));
        
        colMappedTo.setCellValueFactory( //new PropertyValueFactory<>("destinationColumnIndex"));
                c -> new SimpleObjectProperty());
        
        table = new TableView<>();
        table.setEditable(true);
        table.getColumns().addAll(colColumnIndex, colColumnName, destinationIndexColumn, colMappedTo);
        table.setItems(sourceTemplate.getColumns());

        colColumnName.setCellFactory(TextFieldTableCell.forTableColumn());
        colMappedTo.setOnEditCommit(e -> {
            TemplateColumn target = e.getNewValue();
            e.getRowValue().setDestinationColumnIndex(target.getColumnIndex());
            LOG.info("committed: " + e.getRowValue() + e.getNewValue());
        });
        colMappedTo.setCellFactory(ComboBoxTableCell.forTableColumn(destinationTemplate.getColumns()));
        
        
//        ComboBox<TemplateColumn> cboMappedColumns = new ComboBox<>();
//        cboMappedColumns.setItems(destinationTemplate.getColumns());

    }

    private void createSampleData() {

        destinationTemplate = new Template();
        destinationTemplate.setTemplateName("Output");
        destinationTemplate.setColumns(FXCollections.observableArrayList(
                new TemplateColumn("Destination 0", 0, -1),
                new TemplateColumn("Destination 1", 1, -1),
                new TemplateColumn("Destination 2", 2, -1),
                new TemplateColumn("Destination 3", 3, -1))
        );

        sourceTemplate = new Template();
        sourceTemplate.setTemplateName("Input");
        sourceTemplate.setColumns(FXCollections.observableArrayList(
                new TemplateColumn("Source 0", 0, 3),
                new TemplateColumn("Source 1", 1, 1),
                new TemplateColumn("Source 2", 2, 0),
                new TemplateColumn("Source 3", 3, 2))
        );
        sourceTemplate.setDestinationTemplate(destinationTemplate);

    }

    public class Template {

        private final SimpleStringProperty templateName = new SimpleStringProperty("");
        private final SimpleStringProperty destinationTemplateName = new SimpleStringProperty("");
        private SimpleObjectProperty<Template> destinationTemplate = new SimpleObjectProperty<>();

        // The list of TemplateColumns
        private List<TemplateColumn> columns;

        public Template() {
            templateName.set("NEW");
        }

        public Template(ArrayList<TemplateColumn> columns) {
            this.columns = columns;
        }

        public String getTemplateName() {
            return templateName.get();
        }

        public SimpleStringProperty templateNameProperty() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName.set(templateName);
        }

        public String getDestinationTemplateName() {
            return destinationTemplateName.get();
        }

        public void setDestinationTemplateName(String destinationTemplateName) {
            this.destinationTemplateName.set(destinationTemplateName);
        }

        public ObservableList<TemplateColumn> getColumns() {
            return FXCollections.observableArrayList(columns);
        }

        public SimpleStringProperty destinationTemplateNameProperty() {
            return destinationTemplateName;
        }

        public Template getDestinationTemplate() {
            return destinationTemplate.get();
        }

        public SimpleObjectProperty<Template> destinationTemplateProperty() {
            return destinationTemplate;
        }

        public void setDestinationTemplate(Template destinationTemplate) {
            this.destinationTemplate.set(destinationTemplate);
        }

        public void setColumns(List<TemplateColumn> columns) {
            this.columns = columns;
        }

        public TemplateColumn getReportColumn(int index) {
            for (TemplateColumn col : columns) {
                if (col.getColumnIndex() == index) return col;
            }
            return null;
        }

        @Override
        public String toString() {
            return templateName.get();
        }
    }


    public class TemplateColumn {

        private final SimpleStringProperty columnName = new SimpleStringProperty("");
        private final SimpleIntegerProperty columnIndex = new SimpleIntegerProperty(0);
        private final SimpleIntegerProperty destinationColumnIndex = new SimpleIntegerProperty(-1);

        public TemplateColumn(String columnName, int columnIndex, int destinationColumnIndex) {
            this.columnName.set(columnName);
            this.columnIndex.set(columnIndex);
            this.destinationColumnIndex.set(destinationColumnIndex);
        }

        public int getDestinationColumnIndex() {
            return destinationColumnIndex.get();
        }

        public SimpleIntegerProperty destinationColumnIndexProperty() {
            return destinationColumnIndex;
        }

        public void setDestinationColumnIndex(int destinationColumnIndex) {
            this.destinationColumnIndex.set(destinationColumnIndex);
        }

        public String getColumnName() {
            return columnName.get();
        }

        public SimpleStringProperty columnNameProperty() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName.set(columnName);
        }

        public int getColumnIndex() {
            return columnIndex.get();
        }

        public SimpleIntegerProperty columnIndexProperty() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex.set(columnIndex);
        }

        @Override
        public String toString() {
            return columnName.get();
        }
    }
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableAndComboMixedTypes.class.getName());

}
