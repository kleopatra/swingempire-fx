/*
 * Created on 05.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;


/**
 * http://stackoverflow.com/q/35095220/203657
 * 
 * Add row and edit cell at once.
 */
public class TableAddRowAndEdit extends Application
{
    private static ObservableList<SimpleStringProperty> exampleList = FXCollections.observableArrayList();
    //Placeholder for the button
    private static SimpleStringProperty PlaceHolder = new SimpleStringProperty();

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
        for(int i = 0 ; i < 20; i++)
            exampleList.add(new SimpleStringProperty("Hello Test " +i));
        exampleList.add(PlaceHolder);

        //create a basic tableView
        TableView<SimpleStringProperty> listView = new TableView<SimpleStringProperty>();
        listView.setEditable(true);

        TableColumn<SimpleStringProperty, String> column = new TableColumn<SimpleStringProperty, String>();
        column.setCellFactory(E -> new TableCellTest<SimpleStringProperty, String>());
//        Callback<TableColumn<SimpleStringProperty, String>, TableCell<SimpleStringProperty, String>> 
//            coreTextFieldCellFactory = TextFieldTableCell.forTableColumn();
//        column.setCellFactory(coreTextFieldCellFactory);
        column.setCellValueFactory(E -> E.getValue());
        column.setEditable(true);
        
//        column.setOnEditCommit(e -> {
//            
//        });

        // set listViews' backing list
        listView.setItems(exampleList);


        listView.getColumns().clear();
        listView.getColumns().add(column);
        parent.getChildren().add(listView);

//        parent.setOnKeyReleased(E -> System.out.println("KeyRelease Captuered: Parent"));


        primaryStage.show();
    }

    // basic editable cell example
    public static class TableCellTest<S, T> extends TableCell<S, T>
    {
        // The editing textField.
        protected static TextField textField;
        protected Button addButton;
        protected ContextMenu menu;


        public TableCellTest()
        {
            this.setOnContextMenuRequested(E -> {
                if(this.getTableView().editingCellProperty().get() == null)
                    this.menu.show(this, E.getScreenX(), E.getScreenY());
            });
            this.menu = new ContextMenu();

            MenuItem createNew = new MenuItem("create New");
            createNew.setOnAction(E -> {
                if(this.getIndex() == exampleList.size() - 2)
                    this.onNewItem(this.getIndex() + 1);
            });
            this.menu.getItems().add(createNew);

            addButton = new Button("Add");
            addButton.setOnAction(E -> this.onNewItem(exampleList.size() - 1));
            addButton.prefWidthProperty().bind(this.widthProperty());
        }

        public void onNewItem(int index)
        {
            TableAddRowAndEdit.exampleList.add(index, new SimpleStringProperty("New Item " + index));
            this.getTableView().edit(index, this.getTableColumn());
            Platform.runLater(() -> {
                
            });
//            textField.requestFocus();
        }

        @Override
        public void startEdit()
        {
            if (!isEditable()
                    || (this.getTableView() != null && !this.getTableView().isEditable())
                    || (this.getTableColumn() != null && !this.getTableColumn().isEditable()))
                return;

            super.startEdit();

            if(textField == null)
                this.createTextField();


            textField.setText((String)this.getItem());
            this.setGraphic(textField);
            textField.selectAll();
            this.setText(null);
        }

        @Override
        public void cancelEdit()
        {
            if (!this.isEditing())
                return;

            super.cancelEdit();

            this.setText((String)this.getItem());
            this.setGraphic(null);
        }

        @Override
        protected void updateItem(T item, boolean empty)
        {
            super.updateItem(item, empty);

            // Checks if visuals need an update.
            if(this.getIndex() == TableAddRowAndEdit.exampleList.size() - 1)
            {
                this.setText("");
                this.setGraphic(addButton);
            }
            else if(empty || item == null)
            {
                this.setText(null);
                this.setGraphic(null);
            }
            else
            {
                // These checks are needed to make sure this cell is the specific cell that is in editing mode.
                // Technically this#isEditing() can be left out, as it is not accurate enough at this point.
//                if(this.isEditing() && this.getTableView().getEditingCell() != null 
//                        && this.getTableView().getEditingCell().getRow() == this.getIndex())
                if (isEditing())
                {
                    //change to TextField
                    this.setText(null);
                    this.setGraphic(textField);
                }
                else
                {
                    //change to actual value
                    this.setText((String)this.getItem());
                    this.setGraphic(null);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void createTextField()
        {
                if(textField == null)
                  textField = new TextField();
            // A keyEvent for the textField. which is called when there is no keyEvent set to this cellObject.
            textField.addEventHandler(KeyEvent.KEY_PRESSED, E -> {

//                if(this.getTableView().getEditingCell().getRow() == this.getIndex())
                    if(E.getCode() == KeyCode.ENTER)  {
//                        this.setItem((T) textField.getText());
//                        this.commitEdit(this.getItem());
                        commitEdit((T) textField.getText());
                    }
                    else if(E.getCode() == KeyCode.ESCAPE) {
                        this.cancelEdit();
                    }
            });
        }
    }
}

