/*
 * Created on 23.12.2019
 *
 */
package de.swingempire.fx.fxml;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;

/**
 * Quick check: possible to access the controller 
 * - without loading? No - this throws NPE.
 * - without application? No - throws IllegalStateException (toolkitNotInitialized)
 * 
 * https://stackoverflow.com/q/59435261/203657
 */
public class ChoiceBoxMain {
    
    ChoiceBoxController controller;
    
    private void getController() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("choicebox.fxml"));
        loader.load();
        controller = loader.getController();
        System.out.println(controller.getClass());
        
    }
    public static void main(String[] args) throws IOException {
        new ChoiceBoxMain().getController();
    }
}
