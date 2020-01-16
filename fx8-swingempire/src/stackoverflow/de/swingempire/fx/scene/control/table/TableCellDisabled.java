/*
 * Created on 15.01.2020
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * https://stackoverflow.com/q/59439492/203657
 * make cellA/B disabled dependent on which type of line is choosen
 * 
 * Cleanup
 * - replace EditingCell with TextFieldTableCell
 * - expose properties in model
 * 
 */
public class TableCellDisabled extends Application {

    private TableView<ItemsTableLine> itemsTable;

    ObservableList<ItemsTableLine> items;

    private Parent createContent() {
        items = FXCollections.observableArrayList(e -> new ObservableValue[] {e.typeProperty()});
        items.addAll(new ItemsTableLine("A","1","2"),
                     new ItemsTableLine("A","3","4"),
                     new ItemsTableLine("B","5", "6"),
                     new ItemsTableLine());
        // sanity check: list change is fired
        items.addListener((Change<? extends ItemsTableLine> c) -> FXUtils.prettyPrint(c));
        ItemsTable itemsTableParent = new ItemsTable();
        itemsTable = itemsTableParent.makeTable(items);

        VBox root = new VBox();        
        root.getChildren().addAll(itemsTable);
        return root;
    }

    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 200, 100));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static class ItemsTable {

//        private String lastKey = null;

        // This will be exposed through a getter to be updated from list in LoadsTable
        private TableColumn<ItemsTableLine, ItemType> typeCol;
        private TableColumn<ItemsTableLine, ItemType> typeColReadOnly;
        private TableColumn<ItemsTableLine, String> inputACol;
        private TableColumn<ItemsTableLine, String> inputBCol;

        public TableView<ItemsTableLine> makeTable(ObservableList<ItemsTableLine> items) {

            TableView<ItemsTableLine> tv = new TableView<>(items);
            tv.setEditable(true);
            tv.setRowFactory(cc -> new CustomTableRow<>());
  
//            Callback<TableColumn<ItemsTableLine, String>, TableCell<ItemsTableLine, String>> txtCellFactory
//                = TextFieldTableCell.forTableColumn();
//                    = (TableColumn<ItemsTableLine, String> p) -> {
//                        return new EditingCell();
//                    };

            ObservableList<ItemType> itemTypeList
                    = FXCollections.observableArrayList(ItemType.values());
            typeCol = new TableColumn<>("Type");
            inputACol  = new TableColumn<>("Input A");
            inputBCol   = new TableColumn<>("Input B");

            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            
            typeColReadOnly = new TableColumn<>("TypeCopy");
            typeColReadOnly.setCellValueFactory(new PropertyValueFactory<>("type"));
            
            inputACol.setCellValueFactory(new PropertyValueFactory<>("inputA"));
            inputBCol.setCellValueFactory(new PropertyValueFactory<>("inputB"));
            
            typeCol.setCellFactory(ComboBoxTableCell.forTableColumn(itemTypeList));
            
            inputACol.setCellFactory(cc -> new CustomTextFieldTableCell<>(ItemType.TYPEA));
            inputBCol.setCellFactory(cc -> new CustomTextFieldTableCell<>(ItemType.TYPEB));
//            inputACol.setCellFactory(txtCellFactory);
//            inputBCol.setCellFactory(txtCellFactory);
            tv.getColumns().setAll(typeCol, inputACol, inputBCol, typeColReadOnly);

            return tv;
        }

    }

    private static class CustomTableRow<S> extends TableRow<S> {

        
        @Override
        protected boolean isItemChanged(S oldItem, S newItem) {
            return true; //super.isItemChanged(oldItem, newItem);
        }

        @Override
        public void updateIndex(int i) {
//            System.out.println("updating rowIndex: " + i + getItem());
            super.updateIndex(i);
//            System.out.println("after updating " + i + getItem());
        }
        
        
        
    }

    // not really working: need to listen to update of other cell ..
    private static class CustomTextFieldTableCell<T> extends TextFieldTableCell<ItemsTableLine,T> {

        private ItemType target;
        public CustomTextFieldTableCell(ItemType target) {
            this.target = target;
            setConverter((StringConverter<T>) new DefaultStringConverter());
        }
        
        
        @Override
        protected boolean isItemChanged(T oldItem, T newItem) {
            return true; //super.isItemChanged(oldItem, newItem);
        }


        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            ItemType type = null;
            if (getTableRow() != null && getTableRow().getItem() != null) {
                type = getTableRow().getItem().getType();
            }
            System.out.println("type in cell" + type);
            setDisable(type != target);
        }



        @Override
        public void updateItem(T item, boolean empty) {
//            System.out.println("updating item: " + item);
            super.updateItem(item, empty);
        }
        
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellDisabled.class.getName());

    public static class ItemsTableLine {

//        private StringProperty type;
        private ObjectProperty<ItemType> type;
        private StringProperty inputA;
        private StringProperty inputB;

//        public StringProperty typeProperty() {return type;}
        public ObjectProperty<ItemType> typeProperty() {return type;}
        public StringProperty inputAProperty()  {return inputA;  }
        public StringProperty inputBProperty()   {return inputB;}

        public ItemType getType() {
            return type.get();
        }
        public ItemsTableLine() {
            super();
            type = new SimpleObjectProperty<>();
            inputA  = new SimpleStringProperty("");
            inputB   = new SimpleStringProperty("");
        }

        public ItemsTableLine(String...values) {
            this();
//            type.set(values[0]);
            type.set(ItemType.getByCode(values[0]));
            inputA.set(values[1]);
            inputB.set(values[2]);
        }

        @Override
        public String toString() {
            return "type/inputA/inputB: " + getType() + "/" + inputA.get() + "/" + inputB.get();
        }
        
        
    }


    public enum ItemType {
        TYPEA("A", "Type A"),
        TYPEB("B", "Type B");
        private String code;
        private String text;
        private ItemType(String code, String text) { this.code = code; this.text = text;}
        public String getCode() { return code; }
        public String getText() { return text; }
        public static ItemType getByCode(String genderCode) {
           for (ItemType g : ItemType.values()) if (g.code.equals(genderCode)) return g;
           return null;
        }
        @Override public String toString() { return this.text; }
    }
}
