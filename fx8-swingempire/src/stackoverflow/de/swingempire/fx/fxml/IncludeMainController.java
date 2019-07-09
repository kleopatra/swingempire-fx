/*
 * Created on 09.07.2019
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

/**
 * injected field is null
 * https://stackoverflow.com/q/56947722/203657
 * 
 * Problem is, that the same controller class is used in different fxml files. Each
 * creates and loads a separate instance of the controller thus that the expectation
 * (content is loaded) is met only for the main, not for the child (no way to know
 * the fields not controlled by itself)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IncludeMainController {

    @FXML
    private BorderPane myContent;
    
    @FXML
    private void initialize() {
        LOG.info("initialize" + myContent);
    }
    
    @FXML
    private void print(ActionEvent e) {
        LOG.info("and now?" + myContent);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IncludeMainController.class.getName());
}
