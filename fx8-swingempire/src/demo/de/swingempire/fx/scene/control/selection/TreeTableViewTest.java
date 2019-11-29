/*
 * Created on 27.11.2019
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8234884
 * selectPrevious in last row doesn't wrap to second-last row
 *   after first in last is reached 
 *   (analogue selectNext in first row does wrap to second row)
 *   
 * This is the example in the test case: throws in 
 * propertyValueFactory due to mixed types (don't .. replaced with
 * property accessors for not-matching names)   
 */
public class TreeTableViewTest extends Application {
    
    public static class Person {
        
        private final StringProperty nameProperty;
        private final StringProperty surnameProperty;
        
        public Person() {
            this.nameProperty = new SimpleStringProperty();
            this.surnameProperty = new SimpleStringProperty();
        }
        
        public StringProperty nameProperty() {
            return this.nameProperty;
        }
        
        public void setName(String value) {
            this.nameProperty.set(value);
        }
        
        public String getName() {
            return this.nameProperty.get();
        }
        
        public StringProperty surnameProperty() {
            return this.surnameProperty;
        }
        
        public void setSurname(String value) {
            this.surnameProperty.set(value);
        }
        
        public String getSurname() {
            return this.surnameProperty.get();
        }
    }
    
    public static class Dog {
        
        private final StringProperty nameProperty;
        private final IntegerProperty ageProperty;
        private final StringProperty breedProperty;
        
        public Dog() {
            this.nameProperty = new SimpleStringProperty();
            this.ageProperty = new SimpleIntegerProperty();
            this.breedProperty = new SimpleStringProperty();
        }
        
        public StringProperty nameProperty() {
            return this.nameProperty;
        }
        
        public void setName(String value) {
            this.nameProperty.set(value);
        }
        
        public String getName() {
            return this.nameProperty.get();
        }
        
        public IntegerProperty ageProperty() {
            return this.ageProperty;
        }
        
        public void setAge(int value) {
            this.ageProperty.setValue(value);
        }
        
        public int getAge() {
            return this.ageProperty.get();
        }
        
        public StringProperty breedProperty() {
            return this.breedProperty;
        }
        
        public void setBreed(String breed) {
            this.breedProperty.set(breed);
        }
        
        public String getBreed() {
            return this.breedProperty.get();
        }
    }
    
    private TreeItem<Object> createTreeItems() {
        TreeItem<Object> rootItem = new TreeItem<>();
        
        List<TreeItem<Object>> rootChildren = rootItem.getChildren();
        
        Person john = new Person();
        john.setName("John");
        john.setSurname("Denver");
        TreeItem<Object> johnTreeItem = new TreeItem<>(john);
        
        rootChildren.add(johnTreeItem);
        
        List<TreeItem<Object>> johnChildren = johnTreeItem.getChildren();
        
        Dog charlie = new Dog();
        charlie.setName("Charlie");
        charlie.setAge(4);
        charlie.setBreed("Labrador");
        TreeItem<Object> charlieTreeItem = new TreeItem<>(charlie);
        johnChildren.add(charlieTreeItem);
        
        Dog daisy = new Dog();
        daisy.setName("Daisy");
        daisy.setAge(7);
        daisy.setBreed("Bulldog");
        TreeItem<Object> daisyTreeItem = new TreeItem<>(daisy);
        johnChildren.add(daisyTreeItem);
        
        Person alice = new Person();
        alice.setName("Alice");
        alice.setSurname("Goodhead");
        TreeItem<Object> aliceTreeItem = new TreeItem<>(alice);
        
        rootChildren.add(aliceTreeItem);
        
        List<TreeItem<Object>> aliceChildren = aliceTreeItem.getChildren();
        
        Dog buck = new Dog();
        buck.setName("Buck");
        buck.setAge(1);
        buck.setBreed("Border Collie");
        TreeItem<Object> buckTreeItem = new TreeItem<>(buck);
        aliceChildren.add(buckTreeItem);
        
        return rootItem;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        TreeTableView<Object> treeTableView = new TreeTableView<>();
        
        TreeTableViewSelectionModel<Object> selectionModel = treeTableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.setCellSelectionEnabled(true);
        
        treeTableView.setEditable(true);
        
        List<TreeTableColumn<Object, ?>> columns = treeTableView.getColumns();
        
        TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        columns.add(nameColumn);
        
        TreeTableColumn<Object, String> surnameColumn = new TreeTableColumn<>("Surname");
        surnameColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        surnameColumn.setCellValueFactory(cc -> {
            if (cc.getValue().getValue() instanceof Person) {
                return ((Person) cc.getValue().getValue()).surnameProperty();
            }
            return null;
        });
                
//                new TreeItemPropertyValueFactory<>("surname"));
        columns.add(surnameColumn);
        
        TreeTableColumn<Object, Integer> ageColumn = new TreeTableColumn<>("Age");
        ageColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerStringConverter()));
        ageColumn.setCellValueFactory(cc -> {
            if (cc.getValue().getValue() instanceof Dog) {
                return ((Dog) cc.getValue().getValue()).ageProperty().asObject();
            }
            return null;
        });
//                new TreeItemPropertyValueFactory<>("age"));
        columns.add(ageColumn);
        
        TreeTableColumn<Object, String> breedColumn = new TreeTableColumn<>("Breed");
        breedColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        breedColumn.setCellValueFactory(cc -> {
            if (cc.getValue().getValue() instanceof Dog) {
                return ((Dog) cc.getValue().getValue()).breedProperty();
            }
            return null;
        });
                
//                new TreeItemPropertyValueFactory<>("breed"));
        columns.add(breedColumn);
        
        TreeItem<Object> rootTreeItem = createTreeItems();
        treeTableView.setRoot(rootTreeItem);
        treeTableView.setShowRoot(false);
        
        
        // quick check for table
        TableView<Object> table = new TableView<>(createTableData(rootTreeItem));
        TableColumn<Object, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Object, String> surname = new TableColumn<>("Surname");
        surname.setCellValueFactory(cc -> {
           if (cc.getValue() instanceof Person ) {
               return ((Person) cc.getValue()).surnameProperty();
           }
           return null;
        });
        TableColumn<Object, Integer> age = new TableColumn<>("Age");
        age.setCellValueFactory(cc -> {
            if (cc.getValue() instanceof Dog) {
                return ((Dog) cc.getValue()).ageProperty().asObject();
            }
            return null;
        });    
        TableColumn<Object, Integer> breed = new TableColumn<>("Breed");
        breed.setCellValueFactory(cc -> {
            if (cc.getValue() instanceof Dog) {
                return ((Dog) cc.getValue()).ageProperty().asObject();
            }
            return null;
        });
        
        table.getColumns().addAll(name, surname, age, breed);
        table.getSelectionModel().setCellSelectionEnabled(true);
        
        TableSelectionModel<?> currentSelectionModel = table.getSelectionModel();
                treeTableView.getSelectionModel();
        currentSelectionModel.clearSelection();
        
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(treeTableView);

        FlowPane buttonPane = new FlowPane();
        List<Node> buttons = buttonPane.getChildren();
        
        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> currentSelectionModel.selectNext());
        buttons.add(nextButton);
        
        Button previousButton = new Button("Previous");
        previousButton.setOnAction(e -> currentSelectionModel.selectPrevious());
        buttons.add(previousButton);
        
        mainPane.setBottom(buttonPane);
        mainPane.setRight(table);
        Scene scene = new Scene(mainPane, 800, 600);
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }
    
    /**
     * @param rootTreeItem
     * @return
     */
    private ObservableList<Object> createTableData(TreeItem<Object> rootTreeItem) {
        ObservableList<Object> data = FXCollections.observableArrayList();
        for (TreeItem<Object> child : rootTreeItem.getChildren()) {
            data.add(child.getValue());
            
            for (TreeItem<Object> grandChild : child.getChildren()) {
                data.add(grandChild.getValue());
            }
        }
        return data;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}