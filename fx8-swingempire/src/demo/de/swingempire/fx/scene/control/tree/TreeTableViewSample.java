/*
 * Created on 27.03.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * CheckBoxTreeItem/CheckBoxTreeCell not for TreeTableView?
 * 
 * http://stackoverflow.com/q/29300551/203657
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableViewSample extends Application implements Runnable {

    List<Employee> employees = Arrays.<Employee> asList(new Employee(
            "Ethan Williams", 30.0), new Employee("Emma Jones", 10.0),
            new Employee("Michael Brown", 70.0), new Employee("Anna Black",
                    50.0), new Employee("Rodger York", 20.0), new Employee(
                    "Susan Collins", 70.0));

    /*
     * private final ImageView depIcon = new ImageView ( new
     * Image(getClass().getResourceAsStream("department.png")) );
     */
    final CheckBoxTreeItem<Employee> root = new CheckBoxTreeItem<>(
            new Employee("Sales Department", 0.0));
    
    final CheckBoxTreeItem<Employee> root2 = new CheckBoxTreeItem<>(
            new Employee("Departments", 0.0));
    
//    TreeItem<Employee> root = new TreeItem<>(
//            new Employee("Sales Department", 0.0));
//
//    TreeItem<Employee> root2 = new TreeItem<>(
//            new Employee("Departments", 0.0));

    public static void main(String[] args) {
        Application.launch(TreeTableViewSample.class, args);
    }

    @Override
    public void start(Stage stage) {
//        CheckBoxTreeCell c;
//        TreeTableRow d;

//        CheckBox rootBox2 = new CheckBox();
//        rootBox2.setAlignment(Pos.TOP_CENTER);
//        root2.setGraphic(rootBox2);
//        CheckBox rootBox = new CheckBox();
//        LOG.info(rootBox.getAlignment() + "");
//        rootBox.setAlignment(Pos.TOP_CENTER);
//        root.setGraphic(rootBox);
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
        TreeTableColumn<Employee, Double> salaryColumn = new TreeTableColumn<>(
                "Salary");
        salaryColumn.setPrefWidth(190);
        /*
         * salaryColumn.setCellValueFactory(
         * (TreeTableColumn.CellDataFeatures<Employee, String> param) -> new
         * ReadOnlyDoubleWrapper(param.getValue().getValue().getEmail()) );
         */
        salaryColumn.setCellFactory(ProgressBarTreeTableCell
                .<Employee> forTreeTableColumn());
        root2.getChildren().add(root);

        TreeTableView<Employee> treeTableView = new TreeTableView<>(root2);
        treeTableView.getColumns().setAll(empColumn, salaryColumn);
//        treeTableView.setRowFactory(item -> new CheckBoxTreeTableRowHack<>());
        treeTableView.setRowFactory(item -> new CheckBoxTreeTableRow<>());
        
        sceneRoot.getChildren().addAll(treeTableView); //, button);
        stage.setScene(scene);
        stage.show();
//        ScheduledExecutorService executorService = Executors
//                .newScheduledThreadPool(1);
//        executorService.scheduleAtFixedRate(this, 3, 10, TimeUnit.SECONDS);

    }

    @Override
    public void run() {
        root2.getValue().setSalary(calcSalary(root));
    }

    public double calcSalary(TreeItem<Employee> t) {
        Double salary = 0.0;
        if (!t.isLeaf()) {

            ObservableList<TreeItem<Employee>> al = t.getChildren();
            for (int i = 0; i < al.size(); i++) {
                TreeItem<Employee> get = al.get(i);
                salary += calcSalary(get);
            }
            t.getValue().setSalary(salary);
        }
        return salary += t.getValue().getSalary();
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
