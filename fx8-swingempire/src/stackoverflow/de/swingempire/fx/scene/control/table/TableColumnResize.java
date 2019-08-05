/*
 * Created on 02.08.2019
 *
 */
package de.swingempire.fx.scene.control.table;


import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * hbar flickers on/off in constrained resize mode, example from
 * https://bugs.openjdk.java.net/browse/JDK-8089280
 * 
 * reported 2011, still virulent in 2019 (fx11)
 * 
 * @author Jonathan
 */
public class TableColumnResize extends Application {

    public static void main(String... arg) {
        launch(arg);
    }

    public static class Person {

        private StringProperty firstName;
        private StringProperty lastName;
        private StringProperty email;

        private final String country = "New Zealand";

        private Person(String fName, String lName) {
            this(fName, lName, null);
        }

        private Person(String fName, String lName, String email) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
            this.email = new SimpleStringProperty(email);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public StringProperty firstNameProperty() {
            return firstName;
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public StringProperty lastNameProperty() {
            return lastName;
        }

        public String getEmail() {
            return email.get();
        }

        public void setEmail(String email) {
            this.email.set(email);
        }

        public StringProperty emailProperty() {
            return email;
        }

        public String getCountry() {
            return country;
        }

        public String toString() {
            return "Person [ " + getFirstName() + " " + getLastName()/* + ", " + getEmail()*/ + " ]";
        }
    }


    public void start(Stage primaryStage) throws Exception {
        Group rootGroup = new Group();
        Scene scene = new Scene(rootGroup, 800, 600);

        primaryStage.setScene(scene);

        ObservableList<Person> testItems = FXCollections.observableArrayList();
        testItems.addAll(
//            // sources for names:
//            // http://www.ssa.gov/OACT/babynames/
//            // http://names.mongabay.com/most_common_surnames.htm
//
            new Person("Jacob",     "Smith",    "jacob.smith<at>example.com" ),
            new Person("Isabella",  "Johnson",  "isabella.johnson<at>example.com" ),
            new Person("Ethan",     "Williams", "ethan.williams<at>example.com" ),
            new Person("Emma",      "Jones",    "emma.jones<at>example.com" ),
            new Person("Michael",   "Brown",    "michael.brown<at>example.com" ),
            new Person("Olivia",    "Davis",    "olivia.davis<at>example.com" ),
            new Person("Alexander", "Miller",   "alexander.miller<at>example.com" ),
            new Person("Sophia",    "Wilson",   "sophia.wilson<at>example.com" ),
            new Person("William",   "Moore",    "william.moore<at>example.com" ),
            new Person("Ava",       "Taylor",   "ava.taylor<at>example.com" ),

            new Person("Joshua",    "Anderson", "joshua.anderson<at>example.com" ),
            new Person("Emily",     "Thomas",   "emily.thomas<at>example.com" ),
            new Person("Daniel",    "Jackson",  "daniel.jackson<at>example.com" ),
            new Person("Madison",   "White",    "madison.white<at>example.com" ),
            new Person("Jayden",    "Harris",   "jayden.harris<at>example.com" ),
            new Person("Abigail",   "Martin",   "abigail.martin<at>example.com" ),
            new Person("Noah",      "Thompson", "noah.thompson<at>example.com" ),
            new Person("Chloe",     "Garcia",   "chloe.garcia<at>example.com" ),
            new Person("Anthony",   "Martinez", "anthony.martinez<at>example.com" ),
            new Person("Mia",       "Robinson", "mia.robinson<at>example.com" ),

            new Person("Jacob",     "Smith" ),
            new Person("Isabella",  "Johnson" ),
            new Person("Ethan",     "Williams" ),
            new Person("Emma",      "Jones" ),
            new Person("Michael",   "Brown" ),
            new Person("Olivia",    "Davis" ),
            new Person("Alexander", "Miller" ),
            new Person("Sophia",    "Wilson" ),
            new Person("William",   "Moore" ),
            new Person("Ava",       "Taylor" ),
            new Person("Joshua",    "Anderson" ),
            new Person("Emily",     "Thomas" ),
            new Person("Daniel",    "Jackson" ),
            new Person("Madison",   "White" ),
            new Person("Jayden",    "Harris" ),
            new Person("Abigail",   "Martin" ),
            new Person("Noah",      "Thompson" ),
            new Person("Chloe",     "Garcia" ),
            new Person("Anthony",   "Martinez" ),
            new Person("Mia",       "Robinson" )
        );

        TableView<Person> table = new TableView<Person>();
        table.setItems(testItems);

        TableColumn<Person, String> testColumn = new TableColumn<>("Width");
//        testColumn.setResizable(true);
        testColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
//        testColumn.setMaxWidth(100);

        TableColumn<Person, String> testColumn2 = new TableColumn<>("Height");
//        testColumn2.setResizable(true);
        testColumn2.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        testColumn2.setMinWidth(100);

        table.getColumns().addAll(testColumn, testColumn2);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(table);

        scene.setRoot(mainPane);

        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}