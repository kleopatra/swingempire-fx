/*
 * Created on 25.09.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52486256/203657
 * show items that are selected in one treeTable in other treeTable
 * 
 * here: try to use filteredList and bind its content to root children of second
 * weird: not updated, not even getting changes from filtered (seen in listener)
 * except during debugging - second never updated, even though the content binding
 * does update it .. doesn't matter whether we start with all-in or all-out
 * 
 * not entirely understood, but seems to be related to weakBinding of content ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableDriveTreeTableWithSelected extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    FilteredList<TreeItem<Person>>  targetItems;
    @Override
    public void start(Stage stage) throws Exception {

        // create the treeTableView and colums
        TreeTableView<Person> source = createTreeTable(true);
        // creating treeItems to populate the treetableview
        TreeItem<Person> sourceRoot = createRootItem();
        source.setRoot(sourceRoot);

        TreeTableView<Person> target = createTreeTable(false);
        TreeItem<Person> targetRoot = new TreeItem<>();
        target.setRoot(targetRoot);
        
        // backing list for filteredList, configured to fire updates on change
        // of selected
        ObservableList<TreeItem<Person>> backingTargetItems = FXCollections.observableArrayList(
                item -> new ObservableValue[] {item.getValue().selectedProperty()} 
        );
        
        // fill backing list with items of source
        // note: treeItems can't be shared across trees, so need to create with same value
        sourceRoot.getChildren()
            .forEach(tp -> backingTargetItems.add(new TreeItem<>(tp.getValue())));
        // filter the backing list by its selected property
        // this must be a strong reference, otherwise the binding is garbage-collected
        targetItems = new FilteredList<>(backingTargetItems, 
                p -> p.getValue().isSelected()
        );
        // bind content of target root to filtered list
        Bindings.bindContent(targetRoot.getChildren(), targetItems);
        
        // build and show the window
        HBox root = new HBox(10);
        root.getChildren().addAll(source, target);
        stage.setScene(new Scene(root, 300, 300));
        stage.show();
    }

    protected TreeItem<Person> createRootItem() {
        TreeItem<Person> rootTreeItem = new TreeItem<Person>();
        rootTreeItem.getChildren()
                .add(new TreeItem<Person>(new Person("Name 1")));
        rootTreeItem.getChildren()
                .add(new TreeItem<Person>(new Person("Name 2")));
        return rootTreeItem;
    }

    protected TreeTableView<Person> createTreeTable(boolean withSelected) {
        TreeTableView<Person> ttv = new TreeTableView<Person>();
        TreeTableColumn<Person, String> colName = new TreeTableColumn<>("Name");
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        ttv.getColumns().add(colName);
        ttv.setShowRoot(false);
        
        if (withSelected) {
            ttv.setEditable(true);
            // column editable is true by default
            //  colSelected.setEditable(true);
            
            TreeTableColumn<Person, Boolean> colSelected = new TreeTableColumn<>(
                    "Selected");
            ttv.getColumns().add(colSelected);
            // set the columns
            // updating the property
            colSelected.setCellFactory(
                    CheckBoxTreeTableCell.forTreeTableColumn(colSelected));
            colSelected.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
        }
        return ttv;
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

        public void setName(String name) {
            this.name.set(name);
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
        
        public boolean isSelected() {
            return selectedProperty().get();
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableDriveTreeTableWithSelected.class.getName());

}



