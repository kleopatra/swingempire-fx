/*
 * Created on 15.01.2020
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/q/59439492/203657
 * make cellA/B disabled dependent on which type of line is choosen
 */
public class TableCellDisabledOriginal extends Application {

    private TableView<ItemsTableLine> itemsTable;

    ObservableList<ItemsTableLine> items;

    private Parent createContent() {
        items = FXCollections.observableArrayList();
        items.addAll(new ItemsTableLine("A","1","2"),
                     new ItemsTableLine("A","3","4"),
                     new ItemsTableLine("B","5", "6"),
                     new ItemsTableLine());

        ItemsTable itemsTableParent = new ItemsTable();
        itemsTable = itemsTableParent.makeTable(items);

        VBox root = new VBox();        
        root.getChildren().addAll(itemsTable);
        return root;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static class ItemsTable {

        private String lastKey = null;

        // This will be exposed through a getter to be updated from list in LoadsTable
        private TableColumn<ItemsTableLine, ItemType> typeCol;
        private TableColumn<ItemsTableLine, String> inputACol;
        private TableColumn<ItemsTableLine, String> inputBCol;

        public TableView<ItemsTableLine> makeTable(ObservableList<ItemsTableLine> items) {

            TableView<ItemsTableLine> tv = new TableView<>(items);
            tv.setEditable(true);

            Callback<TableColumn<ItemsTableLine, String>, TableCell<ItemsTableLine, String>> txtCellFactory
                    = (TableColumn<ItemsTableLine, String> p) -> {
                        return new EditingCell();
                    };

            ObservableList<ItemType> itemTypeList
                    = FXCollections.observableArrayList(ItemType.values());
            typeCol = new TableColumn<>("Type");
            inputACol  = new TableColumn<>("Input A");
            inputBCol   = new TableColumn<>("Input B");

            typeCol.setCellValueFactory(new Callback<CellDataFeatures<ItemsTableLine, ItemType>, ObservableValue<ItemType>>() {

                @Override
                public ObservableValue<ItemType> call(CellDataFeatures<ItemsTableLine, ItemType> param) {
                    ItemsTableLine lineItem = param.getValue();
                    String itemTypeCode = lineItem.typeProperty().get();
                    ItemType itemType = ItemType.getByCode(itemTypeCode);
                    return new SimpleObjectProperty<>(itemType);
                }
            });

            inputACol.setCellValueFactory(new PropertyValueFactory<>("inputA"));
            inputBCol.setCellValueFactory(new PropertyValueFactory<>("inputB"));
            typeCol.setCellFactory(ComboBoxTableCell.forTableColumn(itemTypeList));
            inputACol.setCellFactory(txtCellFactory);
            inputBCol.setCellFactory(txtCellFactory);
            typeCol.setOnEditCommit((CellEditEvent<ItemsTableLine, ItemType> event) -> {
                TablePosition<ItemsTableLine, ItemType> pos = event.getTablePosition();
                ItemType newItemType = event.getNewValue();
                int row = pos.getRow();
                ItemsTableLine lineItem = event.getTableView().getItems().get(row);
                lineItem.setType(newItemType.getCode());
            });
            inputACol.setOnEditCommit((TableColumn.CellEditEvent<ItemsTableLine, String> evt) -> {
                evt.getTableView().getItems().get(evt.getTablePosition().getRow())
                        .inputAProperty().setValue(evt.getNewValue().replace(",", "."));
            });
            inputBCol.setOnEditCommit((TableColumn.CellEditEvent<ItemsTableLine, String> evt) -> {
                evt.getTableView().getItems().get(evt.getTablePosition().getRow())
                        .inputBProperty().setValue(evt.getNewValue().replace(",", "."));
            });


            tv.getColumns().setAll(typeCol, inputACol, inputBCol);

            return tv;
        }

        private class EditingCell extends TableCell {
            private TextField textField;
            @Override public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(textField);
                    //setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
                    Platform.runLater(() -> {//without this space erases text, f2 doesn't
                        textField.requestFocus();//also selects
                    });
                    if (lastKey != null) {
                        textField.setText(lastKey);
                        Platform.runLater(() -> {textField.deselect(); textField.end();});}
                }
            }
            public void commit() { commitEdit(textField.getText()); }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                try {
                    setText(getItem().toString());
                } catch (Exception e) {
                }
                setGraphic(null);
            }

            @Override
            public void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                    if (!getTableColumn().getText().equals("Name")) {
                        setAlignment(Pos.CENTER);
                    }
                }
            }
            private void createTextField() {
                textField = new TextField(getString());
                textField.focusedProperty().addListener(
                        (ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
                            if (!arg2) { commitEdit(textField.getText()); }});
                textField.setOnKeyReleased((KeyEvent t) -> {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                        EditingCell.this.getTableView().getSelectionModel().selectBelowCell(); }
                    if (t.getCode() == KeyCode.ESCAPE) { cancelEdit(); }});
                textField.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent t) -> { 
                    if (t.getCode() == KeyCode.DELETE) { t.consume();}});
            }
            private String getString() {return getItem() == null ? "" : getItem().toString();}
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellDisabledOriginal.class.getName());

    public static class ItemsTableLine {

        private StringProperty type;
        private StringProperty inputA;
        private StringProperty inputB;

        public StringProperty typeProperty() {return type;}
        public StringProperty inputAProperty()  {return inputA;  }
        public StringProperty inputBProperty()   {return inputB;}

        public ItemsTableLine() {
            super();
            type = new SimpleStringProperty("");
            inputA  = new SimpleStringProperty("");
            inputB   = new SimpleStringProperty("");
        }

        public ItemsTableLine(String...values) {
            this();
            type.set(values[0]);
            inputA.set(values[1]);
            inputB.set(values[2]);
        }

        // Setters required due to combobox 
        public void setType(String value) {
            type.set(value);
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
