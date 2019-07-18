/*
 * Created on 17.07.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.io.Serializable;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.table.PropertyNaming.Item;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * yet an other version of naming constraints:
 * https://stackoverflow.com/q/57075575/203657
 * 
 * (mis-)starting with uppercase letter in PropertyValueFactory is okay, it's
 * really lenient ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PropertyNaming2 extends Application {

    public static class Client implements Serializable{

        private static final Long serialVersionUID = 1L;
        private transient SimpleStringProperty FullName = new SimpleStringProperty("");
        private transient SimpleStringProperty PhoneNumber = new SimpleStringProperty("");

    public Client(){

    }

        public Client(String fullName, String phoneNumber) {
          FullName.set(fullName);
          PhoneNumber.set(phoneNumber);
        }

        public String getFullName() {
            return FullName.get();
        }

        public SimpleStringProperty fullNameProperty() {
            return FullName;
        }

        public void setFullName(String fullName) {
            this.FullName.set(fullName);
        }

        public String getPhoneNumber() {
            return PhoneNumber.get();
        }

        public SimpleStringProperty phoneNumberProperty() {
            return PhoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.PhoneNumber.set(phoneNumber);
        }
    }
    
    private Parent createContent() {
        TableView<Client> table = new TableView<>(FXCollections.observableArrayList(
                new Client("just a name", "1945")
                ));
        TableColumn<Client, String> column = new TableColumn<>("Name");
        column.setCellValueFactory(new PropertyValueFactory<>("FullName"));
        table.getColumns().addAll(column);
        return table;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PropertyNaming2.class.getName());


}
