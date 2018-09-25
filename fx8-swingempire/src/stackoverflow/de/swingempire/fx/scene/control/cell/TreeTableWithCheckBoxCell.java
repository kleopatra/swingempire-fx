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
public class TreeTableWithCheckBoxCell extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    ListChangeListener cl = c -> {
//        c.reset();
        FXUtils.prettyPrint(c);
//        c.reset();
    };
    
//    ObservableList<TreeItem<Person>> personItems;

    FilteredList<TreeItem<Person>>  selectedChildren;
    @Override
    public void start(Stage stage) throws Exception {

        // create the treeTableView and colums
        TreeTableView<Person> ttv = new TreeTableView<Person>();
        TreeTableColumn<Person, String> colName = new TreeTableColumn<>("Name");
        TreeTableColumn<Person, Boolean> colSelected = new TreeTableColumn<>(
                "Selected");
        ttv.getColumns().add(colName);
        ttv.getColumns().add(colSelected);
        ttv.setShowRoot(false);
        ttv.setEditable(true);
        colSelected.setEditable(true);

        // set the columns
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        // updating the property
        colSelected.setCellFactory(
                CheckBoxTreeTableCell.forTreeTableColumn(colSelected));
        colSelected.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
        // orig: register listener in cellValueFactory -> called often
//            colSelected.setCellValueFactory(cellData -> {
//                // binding the cell property with the model
//                BooleanProperty selected = cellData.getValue().getValue().selectedProperty();
//                // listening for a change in the property
//                selected.addListener((obs, oldVal, newVal) -> {
//                    System.out.println(newVal);// WHY IS THIS GETTING CALLED MULTIPLE TIMES
//                });
//                return selected;
//            });
//
        // creating treeItems to populate the treetableview
        TreeItem<Person> rootTreeItem = new TreeItem<Person>();
        rootTreeItem.getChildren()
                .add(new TreeItem<Person>(new Person("Name 1")));
        rootTreeItem.getChildren()
                .add(new TreeItem<Person>(new Person("Name 2")));
        ttv.setRoot(rootTreeItem);

        TreeTableView<Person> selected = new TreeTableView<>();
        selected.setShowRoot(false);
        TreeItem<Person> selectedRoot = new TreeItem<>();
        selected.setRoot(selectedRoot);
        TreeTableColumn<Person, String> selectedName = new TreeTableColumn<>("Name");
        selectedName.setCellValueFactory(cc -> {
            TreeItem<Person> item = cc.getValue();
            return (item != null && item.getValue() != null) ? item.getValue().nameProperty() : null;
            
        });
        selected.getColumns().addAll(selectedName);
        
        ObservableList<TreeItem<Person>> 
        personItems = FXCollections.observableArrayList(
//                item -> new ObservableValue[] {item.getValue().selectedProperty()} 
                );
        
        rootTreeItem.getChildren()
            .forEach(tp -> personItems.add(new TreeItem<>(tp.getValue())));
//        FilteredList<TreeItem<Person>> 
        // this must be a strong reference, otherwise the binding is garbage-collected
        selectedChildren = new FilteredList<>(personItems, 
                p -> {
                    // weird: with logging (or debugging) this is working as expected
//                    LOG.info("in predicate: " + p);
                    return p.getValue().isSelected();
                    
                }
        );
        selectedChildren.addListener(cl);
        
        Bindings.bindContent(selectedRoot.getChildren(), selectedChildren);
        
        // build and show the window
        HBox root = new HBox(10);
        root.getChildren().addAll(ttv, selected);
        stage.setScene(new Scene(root, 300, 300));
        stage.show();
    }

    public class Person {
        private StringProperty name;

        private BooleanProperty selected;

        public Person(String name) {
            this.name = new SimpleStringProperty(name);
            selected = new SimpleBooleanProperty(true);
            selectedProperty().addListener((src, ov, nv) -> {
                LOG.info("selected for: " + nameProperty().get() + nv );
            });
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
            .getLogger(TreeTableWithCheckBoxCell.class.getName());

}



