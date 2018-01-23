/*
 * Created on 23.01.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/q/48395337/203657
 * Problem with interaction of list and table with combo as cell
 * 
 * Unclear what she's trying to achieve
 */
public class TableAndComboTest extends Application {

    @Override
    public void start(Stage primaryStage) {
      BorderPane root = new BorderPane();
      Scene scene = new Scene(root, 500, 500);

      GridPane gridpane = new GridPane();
      gridpane.setPadding(new Insets(5));
      gridpane.setHgap(10);
      gridpane.setVgap(10);
      root.setCenter(gridpane);

      ObservableList<Person> leaders = FXCollections. observableArrayList();
      Person name1 = new Person("Ram");
      Person name2 = new Person("Vicky");
      Person name3 = new Person("Saga");
      leaders.addAll(name1, name2, name3);


     // ObservableList<VDPair> leaders_cbox = FXCollections.observableArrayList();
      final ListView<Person> leaderListView = new ListView<Person>(leaders);
      leaderListView.setPrefWidth(150);
      leaderListView.setPrefHeight(150);
      leaderListView.setCellFactory(new Callback<ListView<Person>, ListCell<Person>>() {
        @Override
        public ListCell<Person> call(ListView<Person> param) {
          ListCell<Person> cell = new ListCell<Person>() {
            @Override
            public void updateItem(Person item, boolean empty) {
              super.updateItem(item, empty);
              if (item != null) {
                setText(item.getFirstName());
              }
            }
          };
          return cell;
        }
      });

      ObservableList<ListView<Person>> listObl = FXCollections. observableArrayList();
      listObl.add(leaderListView);
      final TableView<Person> employeeTableView = new TableView<>();
      employeeTableView.setPrefWidth(300);
      employeeTableView.setEditable(true);
//      final ComboBox combo = new ComboBox(listObl);

      TableColumn<Person, String> firstNameCol = new TableColumn<>("Names");
      firstNameCol.setPrefWidth(employeeTableView.getPrefWidth());
      firstNameCol.setCellFactory(ComboBoxTableCell.forTableColumn(leaders));
      //firstNameCol.setCellFactory(new TableComboBoxObjCellFactory(leaders,false, false, leaders));
      firstNameCol.setEditable(true);
     firstNameCol.setCellValueFactory(new PropertyValueFactory<Person,String>("firstName"));

      employeeTableView.getColumns().addAll(firstNameCol);
      employeeTableView.setItems(leaders);
      //employeeTableView.visibler
      TextField addFld = new TextField();
      Button btn = new Button();
      btn.setText("Add");
      btn.setMinWidth(100);
      btn.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent pactionevent) {
              Person newPerson = new Person(addFld.getText());
              //leaderListView.getItems().add(newPerson);
              employeeTableView.getItems().add(newPerson);
              }
      });

      Label lstHdr = new Label("ListView");
      GridPane.setHalignment(lstHdr, HPos.CENTER);
      gridpane.add(lstHdr, 0, 0);
      gridpane.add(leaderListView, 0, 1);

      Label tblHdr = new Label("TableView");
      GridPane.setHalignment(tblHdr, HPos.CENTER);
      gridpane.add(tblHdr, 1, 0);
      gridpane.add(employeeTableView, 1, 1);

      gridpane.add(addFld, 0, 2);
      gridpane.add(btn, 1, 2);
      primaryStage.setScene(scene);
      primaryStage.show();
    }
    public static void main(String[] args) {
      launch(args);
    }

    public static class Person {

      private StringProperty firstName;
      private ObservableList<Person> employees = FXCollections.observableArrayList();

      public Person(String firstName) {
        this.setFirstName(firstName);
      }

      public final void setFirstName(String value) {
        firstNameProperty().set(value);
      }

      public final String getFirstName() {
        return firstNameProperty().get();
      }

      public StringProperty firstNameProperty() {
        if (firstName == null) {
          firstName = new SimpleStringProperty();
        }
        return firstName;
      }

    @Override
    public String toString() {
        return "Person: " + getFirstName();
    }
      
      
    }
  } 

