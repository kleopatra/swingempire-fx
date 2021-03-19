/*
 * Created 19.03.2021
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/66674211/203657
 * action fired on setAll
 * 
 * initial value blue: 
 *   - press button to setAll different list
 *   - action fired twice - first null, then blue (because it's still contained)
 *   - press button to setAll (equal list to second)
 *   - actions fired null/blue/null and selection cleared
 *   
 * initial value black
 *   - no action fired on setAll - though value changes from black to pink (same index)
 *   - select another value - fires action
 *   - press button again: fires null/othervalue/null (selection cleared)  
 *   
 * same for versions fx11 -> current (fx15)
 * for fx8:
 * 
 * initial value blue
 *   - setItems
 *   - no action, but value changed to mauve (same index)
 *   - setItems again
 *   - no action, value changed back to blue (nothing selected though, button text only?)
 *   
 * initial value black  
 *   - setItems
 *   - no action, value changed to red (old uncontained, select first?)
 *   - setItems again
 *   - no action, value changed to black (old index, nothing selected in dropdown)
 */
public class ComboActionModifyItems extends Application {

    ObservableList<String> theList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Sample");
        FlowPane root = new FlowPane(Orientation.VERTICAL);
        root.setVgap(20);

        List<String> initialColors = Arrays.asList("red", "green", "blue", "black");
        theList.addAll(initialColors);

        ComboBox<String> theComboBox = new ComboBox<>();
        theComboBox.setItems(theList);
        theComboBox.setOnAction( event -> {
            System.out.println(String.format("theComboBox action listener triggered, current value is %s", theComboBox.getValue()));
        });
        
        Button bttn1 = new Button("Press me");
        bttn1.setOnAction(event -> {
            List<String> someColors = Arrays.asList("red", "orange", "mauve", "pink", "blue", "salmon", "chiffon");
            System.out.println("About to issue setAll against observable list");
            theList.setAll(someColors);
        });

        root.getChildren().add(theComboBox);
        root.getChildren().add(bttn1);

        primaryStage.setScene(new Scene(root, 100, 150));
        primaryStage.show();

        String initial = "blue";
        System.out.println("Setting initial selection to " + initial);
        theComboBox.setValue(initial);

    }


    public static void main(String[] args) {
        launch(args);
    }
}

