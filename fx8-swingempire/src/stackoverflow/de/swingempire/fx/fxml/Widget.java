/*
 * Created on 30.10.2020
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

/**
 * https://stackoverflow.com/q/64600828/203657
 * indirect loading doesn't work
 * 
 */
public class Widget extends Pane{

    public Widget() {
        
        Logger logger = Logger.getLogger(Widget.class.getName());

        try {
            FXMLLoader loader= new FXMLLoader(Widget.class.getResource("Widget.fxml"));
            Pane pane = loader.load();
            this.getChildren().add(pane);
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to load Widget", e);
        }
    }
}