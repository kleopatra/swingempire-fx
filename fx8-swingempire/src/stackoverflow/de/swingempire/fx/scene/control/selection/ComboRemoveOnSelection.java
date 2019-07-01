/*
 * Created on 01.07.2019
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 * Remove selected item from list on selection / action
 * https://stackoverflow.com/q/56834141/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboRemoveOnSelection extends Application {

    // Launch the application 
    public void start(Stage stage)
    {
        // Set title for the stage 
        stage.setTitle("creating combo box ");

        // Create a tile pane 
        TilePane r = new TilePane();

        // Create a label 
        Label description_label =
                new Label("This is a combo box example ");

        // Weekdays 
        String week_days[] =
                { "Monday", "Tuesday", "Wednesday",
                        "Thrusday", "Friday" };

        // Create a combo box 
        ComboBox comboBox =
                new ComboBox(FXCollections
                        .observableArrayList(week_days));

        // Label to display the selected menuitem 
        Label selected = new Label("default item selected");

        // Create action event 
        EventHandler<ActionEvent> event =
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e)
                    {
                        
                        Object value = comboBox.getValue();
                        LOG.info("value: " + value);
                        if (value == null) return;
                        // runlater to let combo update itself ..   
                        // without getting weird errors (from selectionModel data)
                        // like "UnsupportedOperation add ..."
                        // working answer by Fabian (though not fully specified?)
                        Platform.runLater(() -> {
                            selected.setText(value + " selected");
                            //  need to null, otherwise will auto-select the next and remove all iteratively
                            comboBox.setValue(null);
                            comboBox.getItems().remove(value);
                        });
                    }
                };

        // Set on action 
        comboBox.setOnAction(event);

        // Create a tile pane 
        TilePane tile_pane = new TilePane(comboBox, selected);

        // Create a scene 
        Scene scene = new Scene(tile_pane, 200, 200);

        // Set the scene 
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String args[])
    {
        // Launch the application 
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboRemoveOnSelection.class.getName());
}

