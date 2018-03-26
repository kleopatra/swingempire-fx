/*
 * Created on 27.03.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.cell.CheckBoxTreeTableRow;
import de.swingempire.fx.scene.control.cell.DefaultTreeTableCell;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * CheckBoxTreeItem/CheckBoxTreeCell not for TreeTableView?
 * http://stackoverflow.com/q/29300551/203657
 * <p>
 * Example from question, changed to 
 * 
 * <li> have tree item graphics on folders
 * <li> use TreeTableCells with DefaultTreeTableSkin to cope with row graphics
 * <li> use CheckBoxTreeTableRow to install/manage a checkbox with its selected/
 *      indeterminate properties bound to the CheckBoxTreeItem.
 *    
 * <p>
 * removed salary updating code - the progressBarTreeTableCell only serves for
 * demonstrating how to set a skin that copes with row graphic.
 *      
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeTableViewSample extends Application {

    List<Employee> employees = Arrays.<Employee> asList(new Employee(
            "Ethan Williams", 30.0), new Employee("Emma Jones", 10.0),
            new Employee("Michael Brown", 70.0), new Employee("Anna Black",
                    50.0), new Employee("Rodger York", 20.0), new Employee(
                    "Susan Collins", 70.0));

    final CheckBoxTreeItem<Employee> root = new CheckBoxTreeItem<>(
            new Employee("Sales Department", 0.0), new Circle(10, Color.RED));
    
    final CheckBoxTreeItem<Employee> root2 = new CheckBoxTreeItem<>(
            new Employee("Departments", 0.0), new Circle(10, Color.BLUE));

    public static void main(String[] args) {
        Application.launch(TreeTableViewSample.class, args);
    }

    @Override
    public void start(Stage stage) {
        root.setExpanded(true);
        employees.stream().forEach((employee) -> {
            root.getChildren().add(new CheckBoxTreeItem<>(employee));
        });
        stage.setTitle("Tree Table View Sample");
        VBox sceneRoot  = new VBox();
        final Scene scene = new Scene(sceneRoot, 400, 100);
        scene.setFill(Color.LIGHTGRAY);

        TreeTableColumn<Employee, String> empColumn = new TreeTableColumn<>(
                "Employee");
        empColumn.setPrefWidth(150);
        empColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        empColumn.setCellFactory(p -> new DefaultTreeTableCell<>());
        TreeTableColumn<Employee, Double> salaryColumn = new TreeTableColumn<>(
                "Salary");
        salaryColumn.setPrefWidth(190);
        // all cell types must have a skin that copes with row graphics
        salaryColumn.setCellFactory(e -> {
            TreeTableCell cell = new ProgressBarTreeTableCell() {

                @Override
                protected Skin<?> createDefaultSkin() {
                    return new DefaultTreeTableCell.DefaultTreeTableCellSkin<>(this);
                }
                
            };
            return cell;
        });
        root2.getChildren().add(root);

        TreeTableView<Employee> treeTableView = new TreeTableView<>(root2);
        treeTableView.getColumns().setAll(empColumn, salaryColumn);
//        treeTableView.setRowFactory(item -> new CheckBoxTreeTableRowHack<>());
        treeTableView.setRowFactory(item -> new CheckBoxTreeTableRow<>());
        
        sceneRoot.getChildren().addAll(treeTableView); //, button);
        stage.setScene(scene);
        stage.show();
    }

    public class Employee {

        private SimpleStringProperty name;

        private SimpleDoubleProperty salary;

        public SimpleStringProperty nameProperty() {
            if (name == null) {
                name = new SimpleStringProperty(this, "name");
            }
            return name;
        }

        public SimpleDoubleProperty salaryProperty() {
            if (salary == null) {
                salary = new SimpleDoubleProperty(this, "salary");
            }
            return salary;
        }

        private Employee(String name, Double salary) {
            this.name = new SimpleStringProperty(name);
            this.salary = new SimpleDoubleProperty(salary);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String fName) {
            name.set(fName);
        }

        public Double getSalary() {
            return salary.get();
        }

        public void setSalary(Double fName) {
            salary.set(fName);
        }

        @Override
        public String toString() {
            return getName();
        }
        
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableViewSample.class.getName());
}
