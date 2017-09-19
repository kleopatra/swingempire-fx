/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
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
 * 
 * reported as
 * https://bugs.openjdk.java.net/browse/JDK-8187665
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewUpdateBug extends Application {

    
    /**
     * TableCell that updates state based on another value in the row.
     */
    public static class DisableTextFieldTableCel extends TextFieldTableCell {
        
        public DisableTextFieldTableCel() {
            super(new DefaultStringConverter());
        }

        /**
         * Just to see whether or not this is called on update notification
         * from the items (it's not)
         */
        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
//            LOG.info("called? " + index);
        }
        
        /**
         * Implemented to change background based on 
         * visible property of row item.
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
            } else {
                setStyle("-fx-background-color: null");
            }
        }
        
    }
    
    @Override
    public void start(Stage primaryStage) {
        // data: list of tableColumns with extractor on visible property
        ObservableList<TableColumn> data = FXCollections.observableArrayList(
                c ->  new Observable[] {c.visibleProperty()});
                
        data.addAll(new TableColumn("first"), new TableColumn("second"));
        
        TableView<TableColumn> table = new TableView<>(data);
        table.setEditable(true);
        
        // for bug report: verify that we get an update
//        data.addListener((ListChangeListener) c -> {
//            
//            while(c.next()) {
//                if (c.wasUpdated()) {
//                    LOG.info("was update on " + (((TableColumn) c.getList().get(c.getFrom())).getText()));
//                } 
//                FXUtils.prettyPrint(c);
//            }
//        });
        // hack-around: call refresh
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
                table.refresh();
            }
            FXUtils.prettyPrint(c);
        });
        TableColumn<TableColumn, String> text = new TableColumn<>("Text");
        text.setCellFactory(c -> new DisableTextFieldTableCel()); //TextFieldTableCell.forTableColumn());
        text.setCellValueFactory(new PropertyValueFactory<>("text"));

        TableColumn<TableColumn, Boolean> visible = new TableColumn<>("Visible");
        visible.setCellValueFactory(new PropertyValueFactory<>("visible"));
        visible.setCellFactory(CheckBoxTableCell.forTableColumn(visible));
        
        table.getColumns().addAll(text, visible);
        
        BorderPane root = new BorderPane(table);
        Scene scene = new Scene(root, 300, 150);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewUpdateBug.class.getName());
}
