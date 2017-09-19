/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

/**
 * CheckBoxTableCell: update editable state of one column based of  
 * the boolean in another column
 * https://stackoverflow.com/q/46290417/203657
 * 
 * Bug in skins: cell not updated on listChange.wasUpdated
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewCheckBoxCell extends Application {

    private int counter;
    
    public static class DisableTextFieldTableCel extends TextFieldTableCell {
        
        public DisableTextFieldTableCel() {
            super(new DefaultStringConverter());
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            LOG.info("called? " + index);
        }


        @Override
        protected boolean isItemChanged(Object oldItem, Object newItem) {
            super.isItemChanged(oldItem, newItem);
            return true;
        }

        /**
         *
         */
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            TableRow<TableColumn> currentRow = getTableRow();
            boolean editable = false;
            if (!empty && currentRow != null) {
                TableColumn column = currentRow.getItem();
                if (column != null) {
                    editable = column.isVisible();
                }
            }
            if (!empty) {
                setDisable(!editable);
                setEditable(editable);
                if (editable) {
                    this.setStyle("-fx-background-color: red");

                } else {
                    this.setStyle("-fx-background-color: green");
                }
                LOG.info(item + " in row: " + currentRow);
            } else {
                setStyle("-fx-background-color: null");
            }
        }
        
    }
    
    public static class DTableRow extends TableRow {
        
        
        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            LOG.info("row " + index);
        }

        @Override
        protected boolean isItemChanged(Object oldItem, Object newItem) {
            super.isItemChanged(oldItem, newItem);
            return true;
        }
        
    }
    @Override
    public void start(Stage primaryStage) {
        ObservableList<TableColumn> data = FXCollections.observableArrayList(
                c -> {
            return new Observable[] {c.visibleProperty()};
        }
                );
                
                
        data.addAll(new TableColumn("first"), new TableColumn("second"));
        
        TableView<TableColumn> table = new TableView<>(data);
        table.setEditable(true);
        table.getItems().get(1).setVisible(false);
        
        data.addListener((ListChangeListener) c -> {
            boolean wasUpdated = false;
            boolean otherChange = false;
            while(c.next()) {
                if (c.wasUpdated()) {
                    wasUpdated = true;
                } else {
                    otherChange = true;
                }
                
            }
            if (wasUpdated && !otherChange) {
//                table.refresh();
            }
            FXUtils.prettyPrint(c);
        });
        table.setRowFactory(r -> new DTableRow());
        TableColumn<TableColumn, String> text = new TableColumn<>("Text");
        text.setCellFactory(c -> new DisableTextFieldTableCel()); //TextFieldTableCell.forTableColumn());
        text.setCellValueFactory(new PropertyValueFactory<>("text"));

        TableColumn<TableColumn, Boolean> visible = new TableColumn<>("Visible");
        visible.setCellValueFactory(new PropertyValueFactory<>("visible"));
        visible.setCellFactory(CheckBoxTableCell.forTableColumn(visible));
        
        TableColumn<TableColumn, String> id = new TableColumn<>("Id");
        id.setCellFactory(TextFieldTableCell.forTableColumn());
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        table.getColumns().addAll(text, visible, id);

        Button add = new Button("addItem");
        add.setOnAction(e -> table.getItems().add(new TableColumn<>("added " + counter++)));
        BorderPane root = new BorderPane(table);
        root.setBottom(add);
        Scene scene = new Scene(root, 300, 150);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewCheckBoxCell.class.getName());
}
