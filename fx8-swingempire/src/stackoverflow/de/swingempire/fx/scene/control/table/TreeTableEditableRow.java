/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/a/52529200/203657
 * toggle editable of row based on some item property
 * 
 * TreeTableCell doesn't respect editability of row? None of cells do ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableEditableRow extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {

        // create the treeTableView and colums
        TreeTableView<Person> ttv = new TreeTableView<Person>();
        TreeTableColumn<Person, String> colName = new TreeTableColumn<>("Name");
        TreeTableColumn<Person, Boolean> colSelected = new TreeTableColumn<>("Selected");
        colName.setPrefWidth(100);
        ttv.getColumns().add(colName);
        ttv.getColumns().add(colSelected);
        ttv.setShowRoot(false);
        ttv.setEditable(true);


        // set the columns
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        colName.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        colName.setCellFactory(c -> {
            TreeTableCell cell = new TextFieldTreeTableCell() {

                @Override
                public void startEdit() {
                    if (getTreeTableRow() != null && !getTreeTableRow().isEditable()) return;
                    super.startEdit();
                }
                
            };
            return cell;
        });
        
        colSelected.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(colSelected));
        colSelected.setCellFactory(c -> {
            TreeTableCell cell = new CheckBoxTreeTableCell() {

                @Override
                public void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (getGraphic() != null) {
                        getGraphic().disableProperty().bind(Bindings
                                .not(
                                      getTreeTableView().editableProperty()
                                     .and(getTableColumn().editableProperty())
                                     .and(editableProperty())
                                     .and(getTreeTableRow().editableProperty())
                            ));

                    }
                }
                
            };
            return cell;
        });
        
        colSelected.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
//        colSelected.setEditable(false);

        ttv.setRowFactory(table-> {
            return new TreeTableRow<Person>(){
                @Override
                public void updateItem(Person pers, boolean empty) {
                    super.updateItem(pers, empty);
                    boolean isTopLevel = table.getRoot().getChildren().contains(treeItemProperty().get());
                    if (!isEmpty() && isTopLevel) {
                        //                        if(isTopLevel){
                        setStyle("-fx-background-color:lightgrey;");
                        setEditable(false); //THIS DOES NOT SEEM TO WORK AS I WANT
                    }else{
                        setEditable(true);
                        setStyle("-fx-background-color:white;");

                    }
                }
            };
        });


        // creating treeItems to populate the treetableview
        TreeItem<Person> rootTreeItem = new TreeItem<Person>();
        TreeItem<Person> parent1 = new TreeItem<Person>(new Person("Parent 1"));
        TreeItem<Person> parent2 = new TreeItem<Person>(new Person("Parent 1"));
        parent1.getChildren().add(new TreeItem<Person>(new Person("Child 1")));
        parent2.getChildren().add(new TreeItem<Person>(new Person("Child 2")));
        rootTreeItem.getChildren().addAll(parent1,parent2);


        ttv.setRoot(rootTreeItem);

        // build and show the window
        Group root = new Group();
        root.getChildren().add(ttv);
        stage.setScene(new Scene(root, 300, 300));
        stage.show();
    }
    
    public class Person {
        private StringProperty name;
        private BooleanProperty selected;

        public Person(String name) {
            this.name = new SimpleStringProperty(name);
            selected = new SimpleBooleanProperty(false);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public void setName(String name){
            this.name.set(name);
        }

        public void setSelected(boolean selected){
            this.selected.set(selected);
        }
        }


    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableEditableRow.class.getName());

}
