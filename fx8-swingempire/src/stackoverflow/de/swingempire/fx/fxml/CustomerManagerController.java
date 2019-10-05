/*
 * Created on 04.10.2019
 *
 */
package de.swingempire.fx.fxml;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.swingempire.fx.fxml.CustomerApp.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58203401/203657
 * passing parameters
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CustomerManagerController implements Initializable {

    @FXML
    public TableView<Customer> customerTable;

    @FXML
    public TableColumn<Customer, String> custname;

    @FXML
    public TableColumn<Customer, String> city;

    @FXML
    private TextField cityTxtFld;

    @FXML
    private TextField nameTxtFld;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        custname.setCellValueFactory(new PropertyValueFactory<>("name"));
        city.setCellValueFactory(new PropertyValueFactory<>("city"));
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        new CustomerController(this);
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("customer.fxml"));
//        
//        Parent parent = loader.load();
//        ((CustomerController) loader.getController()).setManagerController(this);
//        Stage stage = new Stage();
//        Scene scene = new Scene(parent);
//        stage.setScene(scene);
//        stage.show();
    }

    
    public void inflateUI(Customer customer) {
        customerTable.getItems().add(customer);
    }
}
