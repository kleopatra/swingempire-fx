/*
 * Created on 25.02.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

import com.sun.javafx.scene.control.inputmap.InputMap;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 * Reported
 * https://bugs.openjdk.java.net/browse/JDK-8150636
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class InputMapCleanupBug extends Application {

    private Parent getContent() {
        Shape shape = new Circle(200, 200, 100);
        
        InputMap<Shape> inputMap = new InputMap<>(shape);
        inputMap.getMappings().add(new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::initial));
        
        Button replace = new Button("Replace InputMap");
        replace.setOnAction(e -> {
            // dispose cleans up
            // that's not what a Behavior want's to do in its dispose
            // as per code comment, it wants to remove only the mappings that were
            // installed by the behavior
            // inputMap.dispose();
            // clearing the mappings doesn't cleans up
            inputMap.getMappings().clear();
            InputMap<Shape> replaced = new InputMap<>(shape);
            replaced.getMappings().add(new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::replaced));
        });
        BorderPane pane = new BorderPane(shape);
        pane.setBottom(replace);
        return pane;
    }
    
    public void initial(MouseEvent e) {
        System.out.println("initial");
    }
    
    public void replaced(MouseEvent e) {
        System.out.println("replaced");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 500, 500));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
