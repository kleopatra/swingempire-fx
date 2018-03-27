/*
 * Created on 23.01.2018
 *
 */
package de.swingempire.fx.fxml;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * problem with menushortcut not working
 * https://stackoverflow.com/q/48394959/203657
 * 
 * fine for me
 */
public class SampleController 
{
        @FXML
        // The reference of inputText will be injected by the FXML loader
        private TextField inputText;
        
        // The reference of outputText will be injected by the FXML loader
        @FXML
        private TextArea outputText;
        
        // location and resources will be automatically injected by the FXML loader     
        @FXML
        private URL location;
        
        @FXML
        private ResourceBundle resources;
        
        // Add a public no-args constructor
        public SampleController() 
        {
        }
        
        @FXML
        private void initialize() 
        {
        }
        
        @FXML
        private void handleClearAll() {
            System.out.println("got from menu");
        }
        @FXML
        private void printOutput() 
        {
                outputText.setText(inputText.getText());
        }
}
