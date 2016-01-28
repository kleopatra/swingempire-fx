/*
 * Created on 28.01.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Arrays;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Cell update doesn't work properly for identical objects in the list.
 * Overriding isItemChanged doesn't help. Nor does a IdentityCheckingRow.
 * It's pathological, though: why have identical objects in a collection.
 * 
 * http://stackoverflow.com/q/35047415/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CellIdentityExample extends Application
{
    private static ObservableList<String> exampleList = FXCollections.observableArrayList();
    private static String[] testArray = new String[50];

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // basic ui setup
        AnchorPane parent = new AnchorPane();
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);

        //fill backinglist with data
        Arrays.fill(testArray, 0, testArray.length, "Test String");
        for (int i = 0; i < testArray.length; i++) {
            testArray[i] = new String("Test String");//"Item "+i ;
        }
        exampleList.setAll(Arrays.asList(testArray));

        //create a basic tableView
        TableView<String> table = new TableView<String>();
        
        // IdentifyCheckingRow doesn't help (even with updateIItemIfNeed implemented to always)
        // no wonder: item is identical, thus not firing - so listeners aren't notified
//        table.setRowFactory(p -> new IdentityCheckingTableRow<String>() {
//
//            @Override
//            protected void updateItemIfNeeded(int oldIndex, String oldItem,
//                    boolean wasEmpty) {
//                // weed out the obvious
//                if (oldIndex != getIndex()) return;
//                if (oldItem == null || getItem() == null) return;
//                if (wasEmpty != isEmpty()) return;
//                // here both old and new != null, check whether the item had changed
//                if (oldItem != getItem()) return;
//                // unchanged, check if it should have been changed
//                String listItem = getTableView().getItems().get(getIndex());
//                // update if not same
//                    // doesn't help much because itemProperty doesn't fire
//                    // so we need the help of the skin: it must listen
//                    // to invalidation and force an update if 
//                    // its super wouldn't get a changeEvent
//                    updateItem(listItem, isEmpty());
//                    if (oldItem != listItem) {
//                }
//            }
//            
//        }
//                );
        TableColumn<String, String> column = new TableColumn<String, String>();
        column.setCellFactory(E -> new TableCellTest<String, String>());
        column.setCellValueFactory(E -> new SimpleStringProperty(E.getValue()));

        // set listViews' backing list
        table.getItems().addAll(exampleList);
        // listView.setItems(exampleList); it doesnt rely on the backing list, either way it is showing this bug.


        table.getColumns().clear();
        table.getColumns().add(column);
        parent.getChildren().add(table);

        primaryStage.show();
    }

    public static class TableCellTest<S, T> extends TableCell<S, T>
    {

        @Override
        protected void updateItem(T item, boolean empty)
        {
            super.updateItem(item, empty);

            // dipslays cells' value
            this.setText((String)this.getItem());

            // checks cells index and set its color.
            if(this.getIndex() < 12)
                this.setStyle("-fx-background-color: rgba(253, 255, 150, 0.4);");
            else this.setStyle("");
        }

        @Override
        public void updateIndex(int index) {
            System.out.println("Update index: oldIndex: "+getIndex()+" newIndex: "+index);
            super.updateIndex(index);

//            this.setText((String)this.getItem()+" ("+this.getIndex()+")");
//
//
//            // checks cells index and set its color.
//            if(this.getIndex() < 12)
//                this.setStyle("-fx-background-color: rgba(253, 255, 150, 0.4);");
//            else this.setStyle("");            
        }
        @Override
        protected boolean isItemChanged(T oldItem, T newItem) {
            return true; //super.isItemChanged(oldItem, newItem);
        }
        
        
    }
}

