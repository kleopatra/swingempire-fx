/*
 * Created on 08.01.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.net.URL;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Style CheckBoxTableCell - needs a SelectedStateCallback to automatically wire
 * the checkbox with the data property (which really should be a Property)
 * 
 * https://stackoverflow.com/q/48119431/203657
 * 
 */
public class TableViewWithCheckBox extends Application {
    @Override
    public void start(Stage stage) {
        TableView<Person> table = new TableView<>();

        // Editable - really needed, 
        // though slightly astonishing because
        // the checkBoxCell by-passes the edit mechanism
        // makes sense, though, changing data on a read-only table would be
        // astonshing as well
        //https://stackoverflow.com/q/51599065/203657

        table.setEditable(true);
        TableColumn<Person, String> fullNameCol = new TableColumn<>("Name");
        TableColumn<Person, Boolean> acceptedCol = new TableColumn<>("Accepted");

        // NAME
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        fullNameCol.setCellFactory(TextFieldTableCell.<Person> forTableColumn());
        fullNameCol.setMinWidth(200);

        // ACCEPTED
        // JW: this was the manual wiring, didn't work - 
//        acceptedCol.setCellValueFactory((CellDataFeatures<Person, Boolean> param) -> {
//            Person person = param.getValue();           
//            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(person.isAccepted());
//
//            booleanProp.addListener(
//                    (ObservableValue<? extends Boolean> observable,
//                            Boolean oldValue, Boolean newValue) -> {
//                        person.setAccepted(newValue);
//                    });
//            return booleanProp;
//        });

        acceptedCol.setCellValueFactory(new PropertyValueFactory<>("accepted"));
        
        acceptedCol.setCellFactory((TableColumn<Person, Boolean> p) -> {
            CheckBoxTableCell<Person, Boolean> cell = new CheckBoxTableCell<>();
//                    i -> table.getItems().get(i).acceptedProperty());
            cell.getStyleClass().add("custom-cell");
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // working here: accepted is-a boolean property
//        acceptedCol.setCellFactory(CheckBoxTableCell.forTableColumn(acceptedCol));
        ObservableList<Person> list = getPersonList();
        table.setItems(list); 
        table.getColumns().addAll(fullNameCol, acceptedCol);

        Button p = new Button("print");
        p.setOnAction(e -> LOG.info("" +list));
        StackPane root = new StackPane();
        root.setPadding(new Insets(5));
        root.getChildren().addAll(table, p);

        Scene scene = new Scene(root, 300, 300);
        stage.setScene(scene);
        URL uri = getClass().getResource("customcheckbox.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());

        stage.show();
    }

    private ObservableList<Person> getPersonList() { 
        Person person1 = new Person("John White", true);
        Person person2 = new Person("Kevin Land", false);
        Person person3 = new Person("Rouse Hill", true); 
//        List<Person> persons = List.of(person1, person2, person3);
//        Callback<Person, Observable[]> extractor = e -> new Observable[] {e.acceptedProperty()};
//        ObservableList<Person> list = FXCollections.observableArrayList(persons); //extractor);
        ObservableList<Person> list = FXCollections.observableArrayList(person1, person2, person3); //extractor);
//        list.addAll(persons);
        return list;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    public class Person {

        private String name;
        private BooleanProperty acceptedP;
        private boolean accepted;
        
        public Person(String name, boolean single) {
            this.name = name;    
            this.acceptedP = new SimpleBooleanProperty(single);
            this.accepted = single;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public boolean isAccepted() {
//            return accepted;
            return acceptedProperty().get();
        }
        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
            acceptedProperty().set(accepted);
            LOG.info("setting accepted on " + toString());
        }
        
        public BooleanProperty acceptedProperty() {
            return acceptedP;
        }
        @Override
        public String toString() {
            return name + " " + isAccepted();
        }
        
        
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewWithCheckBox.class.getName());
}

