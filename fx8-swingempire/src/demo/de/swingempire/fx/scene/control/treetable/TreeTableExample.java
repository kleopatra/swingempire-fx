/*
 * Created on 06.01.2016
 *
 */
package de.swingempire.fx.scene.control.treetable;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * http://stackoverflow.com/q/34548929/203657
 * TreeTableCell to edit children only 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeTableExample extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        HBox root = new HBox(createTable());
        Scene scene = new Scene(root, 200, 100);
        stage.setScene(scene);
        stage.setTitle("Using a TreeTableView");
        stage.show();
    }

    public TreeTableView createTable() {

        TreeTableView<Person> treeTable = new TreeTableView<>();
        treeTable.setEditable(true);

        Callback<TreeTableColumn<Person, String>, 
            TreeTableCell<Person, String>> cellFactory
                = (TreeTableColumn<Person, String> p) -> new EditingCell();

        TreeTableColumn<Person, String> firstName = new TreeTableColumn<>("First Name");
        firstName.setCellValueFactory(new TreeItemPropertyValueFactory<>("firstName"));
        firstName.setCellFactory(cellFactory);
//        firstName.setOnEditCommit((TreeTableColumn.CellEditEvent<Person, String> event) -> {
//            if(event.getNewValue()!=null)
//                event.getRowValue().getValue().setFirstName(event.getNewValue());
//        });
        PropertyValueFactory f;
        TreeTableColumn<Person, String> lastName = new TreeTableColumn<>("Last Name");
        lastName.setCellValueFactory(new TreeItemPropertyValueFactory<>("lastName"));
        lastName.setCellFactory(cellFactory);
//        lastName.setOnEditCommit((TreeTableColumn.CellEditEvent<Person, String> event) -> {
//            if(event.getNewValue()!=null)
//                event.getRowValue().getValue().setLastName(event.getNewValue());
//        });

        treeTable.getColumns().addAll(firstName, lastName);
        TreeItem<Person> root = new TreeItem<>();
        for (int i = 0; i < 5; i++) {
            root.getChildren().add(new TreeItem<>(new Person("first" + i, "last" + i)));
        }
        root.setExpanded(true);
        treeTable.setRoot(root);
        return treeTable;
    }

    // exposed properties
    public class Person {

        private SimpleStringProperty firstName;
        private SimpleStringProperty lastName;

        public Person(String first, String last){
            firstName = new SimpleStringProperty(this, "firstName", first);
            lastName = new SimpleStringProperty(this, "lastName", last);
        };

        public StringProperty firstNameProperty() {
            return firstName;
        }
        
        public StringProperty lastNameProperty() {
            return lastName;
        }
        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String fName) {
            firstName.set(fName);
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String fName) {
            lastName.set(fName);
        }

    }

    class EditingCell extends TreeTableCell<Person, String> {

        private TextField textField;

        public EditingCell() {
            editableProperty().addListener((s, ov, nv) -> {
                LOG.info("editable changed: " + nv + " on " + getItem()); 
                if (!nv) {
                    new RuntimeException("caller: \n").printStackTrace();
                }
            });
        }

        @Override
        public void startEdit() {
            LOG.info("startEdit: " + getItem() + isEditable());
//            if (!isEmpty() && isEditable()) {
            super.startEdit();
            if (isEditing()) {
                configureEditor();
            }
            if (!isEmpty()) {
            }
        }

        
        
        protected void configureEditor() {
//            LOG.info("super started editing: " + isEditable());
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            LOG.info("cancel: " + getItem() + isEditable());
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            TreeItem<Person> treeItem = getTreeTableRow().getTreeItem();
            setEditable(treeItem != null &&  treeItem.isLeaf());
            if (!empty && (getIndex() == 0 || getIndex() ==1)) {
                LOG.info("update: " + item + " " +isEditing() + " " +isEditable());
            }
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
//                if(!getTreeTableView().getTreeItem(getIndex()).isLeaf())
//                    setEditable(false);
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener(
                    (ObservableValue<? extends Boolean> arg0,
                            Boolean arg1, Boolean arg2) -> {
                        if (!arg2) {
                            commitEdit(textField.getText());
                        }
                    });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TreeTableExample.class
            .getName());
}