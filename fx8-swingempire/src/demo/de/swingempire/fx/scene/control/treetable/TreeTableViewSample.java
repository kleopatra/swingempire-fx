/*
 * Created on 12.12.2017
 *
 */
package de.swingempire.fx.scene.control.treetable;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Original from oracle tutorial.
 * 
 * added fx-indent css: working in fx9
 */
public class TreeTableViewSample extends Application {
    
    List<Employee> employees = Arrays.<Employee>asList(
        new Employee("Ethan Williams", "ethan.williams@example.com"),
        new Employee("Emma Jones", "emma.jones@example.com"),
        new Employee("Michael Brown", "michael.brown@example.com"),
        new Employee("Anna Black", "anna.black@example.com"),
        new Employee("Rodger York", "roger.york@example.com"),
        new Employee("Susan Collins", "susan.collins@example.com"));

//    private final ImageView depIcon = new ImageView (
//            new Image(getClass().getResourceAsStream("department.png"))
//    );

    final TreeItem<Employee> root = 
        new TreeItem<>(new Employee("Sales Department", ""), new RadioButton());
    public static void main(String[] args) {
        Application.launch(TreeTableViewSample.class, args);
    }

    @Override
    public void start(Stage stage) {
        root.setExpanded(true);
        employees.stream().forEach((employee) -> {
            root.getChildren().add(new TreeItem<>(employee));
        });
        stage.setTitle("Tree Table View Sample");
        final Scene scene = new Scene(new Group(), 400, 400);
        scene.setFill(Color.LIGHTGRAY);
        Group sceneRoot = (Group) scene.getRoot();

        TreeTableColumn<Employee, String> empColumn = 
            new TreeTableColumn<>("Employee");
        empColumn.setPrefWidth(150);
        empColumn.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<Employee, String> param) -> 
            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );

        TreeTableColumn<Employee, String> emailColumn = 
            new TreeTableColumn<>("Email");
        emailColumn.setPrefWidth(190);
        emailColumn.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<Employee, String> param) -> 
            new ReadOnlyStringWrapper(param.getValue().getValue().getEmail())
        );

        TreeTableView<Employee> treeTableView = new TreeTableView<>(root);
        treeTableView.getColumns().setAll(empColumn, emailColumn);
        sceneRoot.getChildren().add(treeTableView);
        stage.setScene(scene);
        URL uri = getClass().getResource("indent.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());

        stage.show();
    }
 
    public class Employee {
 
        private SimpleStringProperty name;
        private SimpleStringProperty email;
        public SimpleStringProperty nameProperty() {
            if (name == null) {
                name = new SimpleStringProperty(this, "name");
            }
            return name;
        }
        public SimpleStringProperty emailProperty() {
            if (email == null) {
                email = new SimpleStringProperty(this, "email");
            }
            return email;
        }
        private Employee(String name, String email) {
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
        }
        public String getName() {
            return name.get();
        }
        public void setName(String fName) {
            name.set(fName);
        }
        public String getEmail() {
            return email.get();
        }
        public void setEmail(String fName) {
            email.set(fName);
        }
    }
}