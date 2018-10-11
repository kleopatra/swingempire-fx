/*
 * Created on 11.10.2018
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

/**
 * https://stackoverflow.com/q/52754803/203657
 * bind pref width to a property
 * 
 * answer by fabian:
 * direct binding not supported, way around is to expose the 
 * property on the controller and bind to that.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SizeController {

    @FXML
    DoubleProperty size;
    @FXML
    GridPane root;
    
    @FXML
    private void initialize() {
        LOG.info("size: " + size.get());
        root.widthProperty().addListener((scr, ov, nv) -> {
            size.set(nv.doubleValue() / 5);
            LOG.info("width? " + nv);
        });
    }

    public DoubleProperty sizeProperty() {
        return size;
    }
    
    public void setSize(double size) {
        this.size.set(size);
    }
    
    public double getSize() {
        return size.get();
    }
    
   
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SizeController.class.getName());
}
