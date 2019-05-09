/*
 * Created on 09.05.2019
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;

/**
 * Trying to hook a eventHandler to the combo's editor via fxml.
 * https://stackoverflow.com/q/56046420/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboChildEventController {

    @FXML 
    private ComboBox<String> combo;
    
    @FXML
    private void initialize() {
        combo.setItems(FXCollections.observableArrayList("one", "two", "and a longish"));
        combo.getSelectionModel().select(0);
    }
    
    @FXML 
    private void handleTyped(KeyEvent ev) {
        LOG.info("ev: " + ev);
    }
    
    @FXML 
    private void handleAction(ActionEvent ev) {
        LOG.info("ev: " + ev);
    }
 
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboChildEventController.class.getName());
}
