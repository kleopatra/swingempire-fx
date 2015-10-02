/*
 * Created on 01.10.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Combo fires twice when modifying its items in an Edit commit
 * http://stackoverflow.com/q/32864097/203657
 * 
 * we can wrap the modification into a runlater, but shouldn't the edit be terminated 
 * before firing the event?
 */
public class ComboBoxTableCellEdit2 extends Application {

    public static class MyComboTableCell<S, T> extends ComboBoxTableCell<S, T> {
        
        public MyComboTableCell(StringConverter<T> converter, ObservableList<T> items) {
            super(converter, items);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEditing()) {
                getGraphic().requestFocus();
            }
        }
        
        
        
    }
    
    @Override
    public void start(Stage primaryStage) {
        TableView<MappingItem> table = new TableView<>();

        // FIRST COLUMN
        TableColumn<MappingItem, String> colA = new TableColumn<>("Excel Column");        
        colA.setCellValueFactory(new PropertyValueFactory<>("excelColumnName"));

        //SECOND COLUMN
        TableColumn<MappingItem, GoldplusField> colB = new TableColumn<>("Database Field Column");
        colB.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingItem, GoldplusField>, ObservableValue<GoldplusField>> () {
            @Override
            public ObservableValue<GoldplusField> call(TableColumn.CellDataFeatures<MappingItem, GoldplusField> param) {
                return new ReadOnlyObjectWrapper(param.getValue().getGpField());
            }            
        });

        GoldplusField gp1 = new GoldplusField("T1", "fName", "First Name");
        GoldplusField gp2 = new GoldplusField("T1", "phn", "Phone");
        GoldplusField gp3 = new GoldplusField("T2", "lName", "Last Name");
        GoldplusField gp4 = new GoldplusField("T2", "adrs", "Address");        

        ObservableList<GoldplusField> deactiveFieldsList = FXCollections.observableArrayList();
        ObservableList<GoldplusField> activeFieldsList = FXCollections.observableArrayList(gp1, gp2, gp3, gp4);
        colB.setCellFactory(ComboBoxTableCell.forTableColumn(new FieldToStringConvertor(), activeFieldsList));  
        colB.setCellFactory(t -> new MyComboTableCell(new FieldToStringConvertor(), activeFieldsList));
        colB.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<MappingItem, GoldplusField>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<MappingItem, GoldplusField> t) {
                    LOG.info("got editCommit: " + t);
                    if (t.getNewValue() != null) { 
                        deactiveFieldsList.add(t.getNewValue());

                        MappingItem rowItem = t.getRowValue();
                        rowItem.setGpField(t.getNewValue());
//                        ((MappingItem) t.getTableView().getItems().get(
//                        t.getTablePosition().getRow())
//                        ).setGpField(t.getNewValue());
                        Platform.runLater(() -> {
                            
                        });
                        // ******************************************************************************************** //
                        // This creates a new instance of the EventHandler in which I get the "next" item on the List.
                        // ******************************************************************************************** //
                        activeFieldsList.remove(t.getNewValue());   
                    }
                }
            }
        );


        //THIRD COLUMN
        TableColumn<MappingItem, String> colC = new TableColumn<>("Test Column");
        PropertyValueFactory<MappingItem, String> nameFac = new PropertyValueFactory<>("name");
        colC.setCellValueFactory(nameFac);
        colC.setCellFactory(TextFieldTableCell.forTableColumn());        

        table.setEditable(true);
        table.getColumns().addAll(colA, colB, colC);

        GoldplusField gp5 = new GoldplusField("T1", "other", "Other");
        MappingItem mi1 = new MappingItem("name", gp5);
        mi1.excelColumnName.set("name1");
        MappingItem mi2 = new MappingItem("phone", gp5);
        mi2.excelColumnName.set("nam2");
        ObservableList<MappingItem> miList = FXCollections.observableArrayList(mi1, mi2);

        table.setItems(miList);

        StackPane root = new StackPane();
        root.getChildren().add(table);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    class FieldToStringConvertor extends StringConverter<GoldplusField> {

        @Override
        public String toString(GoldplusField object) {

            if (object != null)
                return object.getGpName();
            else
                return "";
        }

        @Override
        public GoldplusField fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }        
    }

    public class MappingItem {
        private StringProperty excelColumnName = new SimpleStringProperty(this, "excelColumnName");
        private ObjectProperty<GoldplusField> gpField = new SimpleObjectProperty<GoldplusField>(this, "gpField");

        public String getExcelColumnName() { return excelColumnName.get(); }

        public void setExcelColumnName(String excelColumnName) { this.excelColumnName.set(excelColumnName); }

        public StringProperty excelColumnNameProperty() { return excelColumnName;        }

        public GoldplusField getGpField() { return gpField.get(); }

        public void setGpField(GoldplusField gpField) { this.gpField.set(gpField); }

        public ObjectProperty gpFieldProperty() { return this.gpField;        }

        public MappingItem(String columnName) { this.excelColumnName.set(columnName); }    

        public MappingItem(GoldplusField gpField) { this.gpField.set(gpField); }    

        public MappingItem(String columnName, GoldplusField gpField) {

            this.excelColumnName.set(columnName);
            this.gpField.set(gpField);
        }         
    }    

    public class GoldplusField {
        private StringProperty table = new SimpleStringProperty(this, "table");
        private StringProperty dbName = new SimpleStringProperty(this, "dbName");
        private StringProperty gpName = new SimpleStringProperty(this, "gpName");

        public String getDbName() { return dbName.get(); }

        public String getGpName() { return gpName.get(); }

        public String getTable() { return table.get(); }

        public void setDbName(String dbName) { this.dbName.set(dbName); }

        public void setGpName(String gpName) { this.gpName.set(gpName); }

        public void setTable(String table) { this.table.set(table); }

        public StringProperty tableProperty() { return this.table;        }

        public StringProperty gpNameProperty() { return this.gpName;        }    

        public StringProperty dbNameProperty() { return this.dbName;        }

        public GoldplusField(String table, String dbName, String gpName) {

            this.dbName.set(dbName);
            this.gpName.set(gpName);
            this.table.set(table);
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxTableCellEdit2.class.getName());
}

