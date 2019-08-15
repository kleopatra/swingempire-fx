/*
 * Created on 15.08.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.property.PathAdapter;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57500726/203657
 * Nested properties in TableView.
 * 
 * answer by Zephyr: manually wrap properties around the nested - okay
 * for simple read-only for all, obviously breaks if parent is updated.
 * 
 * Simple path binding is PathAdapter (what's its state? - working but very old, pre-functions)
 * Instead, use ReactFx (InhiBeans is obsolete, Var/Val replace them?)
 * 
 * @see de.swingempire.fx.property.PathAdapter
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnNestedProperty extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    int count;
    @Override
    public void start(Stage primaryStage) {

        // Simple Interface
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        // Simple TableView
        TableView<Person> personTableView = new TableView<>();
        TableColumn<Person, String> colName = new TableColumn<>("Name");
        TableColumn<Person, String> colCar = new TableColumn<>("Car");

        // Setup the CellValueFactories
        colName.setCellValueFactory(tf -> tf.getValue().nameProperty());
//        colCar.setCellValueFactory(
//                tf -> tf.getValue().getCar().modelProperty());

        colCar.setCellValueFactory(tf -> {
            Person person = tf.getValue();
            Property<Car> car = person.carProperty();
            return new PathAdapter<Car, String>(car, c -> c.modelProperty());
        });
//        return new PathAdapter<Person, String>(tf.getValue().carProperty(), car -> car.modelProperty());
        personTableView.getColumns().addAll(colName, colCar);


        // Sample Data
        personTableView.getItems().addAll(new Person("Jack", new Car("Accord")),
                new Person("John", new Car("Mustang")),
                new Person("Sally", new Car("Yugo")));

        Button updateModel = new Button("update first");
        updateModel.setOnAction(e -> {
            personTableView.getItems().get(0).setCar(new Car("updated " + count++));
            LOG.info("" + personTableView.getItems().get(0));
            
        });
        root.getChildren().addAll(personTableView, updateModel);
        // Show the stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Sample");
        primaryStage.show();
    }

    private class Person {

        private final StringProperty name = new SimpleStringProperty();

        private final ObjectProperty<Car> car = new SimpleObjectProperty<>();

        public Person(String name, Car car) {
            this.name.set(name);
            this.car.set(car);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public Car getCar() {
            return car.get();
        }

        public void setCar(Car car) {
            this.car.set(car);
        }

        public ObjectProperty<Car> carProperty() {
            return car;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return getName() + " " + getCar();
        }
        
        
    }

    class Car {
        private final StringProperty model = new SimpleStringProperty();

        public Car(String model) {
            this.model.set(model);
        }

        public String getModel() {
            return model.get();
        }

        public void setModel(String model) {
            this.model.set(model);
        }

        public StringProperty modelProperty() {
            return model;
        }

        @Override
        public String toString() {
            return getModel();
        }
        
        
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableColumnNestedProperty.class.getName());
}