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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58203401/203657
 * passing parameters
 * 
 * error was to create a new CustomerManagerController (via load)
 * options: 
 * - keep loading of this in caller (as in original), pass caller after creation
 * - move loading of this into this constructor (with original as param)
 *   see answer of Zephyr to pass-parameter-question
 *   https://stackoverflow.com/a/51050736/203657
 *    
 * @author Jeanette Winzenburg, Berlin
 */
public class CustomerController implements Initializable {

    
    @FXML
    private TextField cNameTextField;
    @FXML
    private TextField custCityTextField;
    private CustomerManagerController managerController;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public CustomerController() {
        
    }
    /**
     * Option B:
     * self-load in constructor
     * @throws IOException 
     */
    public CustomerController(CustomerManagerController managerController) throws IOException {
        setManagerController(managerController);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("customer.fxml"));
        // BEWARE: settin controller here requires _not_ setting it in fxml!
        loader.setController(this);
        Parent parent = loader.load();
//        ((CustomerController) loader.getController()).setManagerController(this);
        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
   }
    /**
     * Option A:
     * public api to set controller, invoked by caller after having loaded
     * this
     * 
     * @param managerController
     */
    public void setManagerController(CustomerManagerController managerController) {
        this.managerController = managerController;
    }
    
    public void newCustomer(ActionEvent e) throws IOException {
        String name = cNameTextField.getText();
        String stringCity = custCityTextField.getText();

        Customer customer = new Customer(10, name, stringCity);

//        FXMLLoader fXMLLoader = new FXMLLoader();
//        fXMLLoader.setLocation(
//                getClass().getResource("customermanager.fxml"));
//        Parent parent = fXMLLoader.load();
//
//        CustomerManagerController fXMLDocumentController = fXMLLoader
//                .getController();
        managerController.inflateUI(customer);
    }

}