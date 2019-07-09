/*
 * Created on 09.07.2019
 *
 */
package de.swingempire.fx.fxml.samecontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

/**
 * Example: use same controller for included views
 * from https://www.java-forum.org/thema/gleiche-controller-instanz-fuer-inludiertes-fxml.171108/
 * 
 * not really useful: each loading creates a new instance of the controller, so only the fields
 * injected by that loader are !=null
 */
public class MainViewController {
    @FXML
    private Parent view;
    @FXML
    private AnchorPane child1;
    @FXML
    private AnchorPane child2;

    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private Button button3;
    @FXML
    private Button button4;
    
    @FXML
    protected void handleButtonEvent(ActionEvent event) {
        String parentName = ((Node) event.getSource()).getParent().getParent().getId();
        System.out.println("ChildViewController: " + ((Button)event.getSource()).getText() + " von " + parentName + " wurde gedrückt...");
        printout("handleButton " + event);
    }

    @FXML
    void initialize() {
        printout("initialize");
    }
    
    private void printout(String prefix) {
        System.out.println("prefix: " + prefix
                + "\n controller " + this
                + "\n view " + view
                + "\n child1 " + child1
                + "\n button1 " + button1
                + "\n button2 " + button2
                + "\n child2 " + child2
                + "\n button3 " + button3
                + "\n button4 " + button4
                + ""
                );
    }
}

