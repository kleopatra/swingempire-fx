/*
 * Created on 01.04.2016
 *
 */
package de.swingempire.fx.scene.control.cell.rowissues;

import de.swingempire.fx.scene.control.selection.IndexUpdatingTableRowSkin;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableIntegerValue;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Regression guard:
 * https://bugs.openjdk.java.net/browse/JDK-8115269
 * (was:        RT-33602)
 * 
 * item updated out of order: updateItem on cell must rely
 * on row item having been updated
 * 
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TableRowBug extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage) {
        primaryStage.setTitle("getTableRow() Bug");
        StackPane root = new StackPane();
        root.getChildren().add( buildTable() );
        primaryStage.setScene(new Scene(root, 500, 250));
        primaryStage.show();
    }

    private TableView buildTable() {

       // create a table with one column that displays TableRow.getItem() * 2
       final TableView<Integer> table = new TableView<>();
       table.setRowFactory(cc -> {
           return new TableRow<Integer>() {
               @Override
               protected Skin<?> createDefaultSkin() {
                   return new IndexUpdatingTableRowSkin(this);
               }
           };
       });
       TableColumn<Integer, Integer> col = new TableColumn<>(" getItem() ");
       setupValueFactory( col );
       setupCellFactory( col );
       col.setPrefWidth( 100 );
       table.getColumns().add( col );

       // add integers from 1 to 50 as row items in the table
       for ( int i = 1; i <= 50; i++ ) {
          table.getItems().add(i);
       }

       return table;
    }

    // set up a value factory that use the row's value as each cell's value.
    private void setupValueFactory(TableColumn col) {
       col.setCellValueFactory(
          new Callback<TableColumn.CellDataFeatures<Integer, Integer>,
                ObservableIntegerValue>() {
             public ObservableIntegerValue call(TableColumn.CellDataFeatures<Integer, Integer> p) {
                return new ReadOnlyIntegerWrapper(p.getValue());
             }
          });
    }

    // display each cell value using a slightly customized cell.
    private void setupCellFactory(TableColumn col) {
       col.setCellFactory( new Callback<TableColumn<Integer,Integer>, TableCell<Integer,Integer>>() {
          public TableCell<Integer, Integer> call( TableColumn<Integer, Integer> c ) {
             return new TableCell() {
                @Override protected void updateItem( Object item, boolean empty ) {
                   super.updateItem( item, empty );
                   setText( empty ? "" : item.toString() );

                   // this section shows the bug; namely that the tablerow's
                   // "item" property has not yet been updated, even though
                   // the cell's "item" property has. shouldn't the row
                   // always be updated first?
                   if ( !isEmpty() && !getTableRow().isEmpty() ) {
                      int expectedRowItem = ((Integer)getItem());
                      int rowItem = (Integer)getTableRow().getItem();
                      if ( expectedRowItem != rowItem ) {
                         System.out.println("Wrong row item: expected " +
                            expectedRowItem + ", instead was " + rowItem );
                      }
                   }
                }
             };

          }
       });
    }
 }

