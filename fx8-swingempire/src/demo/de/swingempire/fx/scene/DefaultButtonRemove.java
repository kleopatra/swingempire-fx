/*
 * Created on 09.07.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.MapChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * bug: https://bugs.openjdk.java.net/browse/JDK-8087722
 * adding second default button makes it default (active)
 * removing second default button doesn't remove the keybinding, that is
 *      first not active
 *      
 * problem is that the original binding in scene.getAccelerators is replaced
 * by the second (without changing the state of the first) and on removal simply
 * removed. seems like the app has the task of keeping track of the accelerators, 
 * core does nothing.
 *       
 * @author Jeanette Winzenburg, Berlin
 */
public class DefaultButtonRemove extends Application
{
        public static void main(String[] args) {
                launch(args);
        }
        
        @Override
        public void start(Stage primaryStage) {
                BorderPane root = new BorderPane();
                
                Button addButton = new Button("Add");
                addButton.setDefaultButton(true);
                addButton.defaultButtonProperty().addListener((pObservable, pOldValue, pNewValue) -> {
                        System.out.println("addButton: " + pOldValue + " -> " + pNewValue);
                });
                
                addButton.setOnAction(pActionEvent -> {
                        if (root.getCenter() == null)
                        {
                                Button removeButton = new Button("Remove again.");
                                removeButton.setOnAction(pActionEvent2 -> {
                                        root.setCenter(null);
                                });
                                
                                root.setCenter(removeButton);
                                removeButton.setDefaultButton(true);
                        }
                });
                
                root.setTop(addButton);
                // changed from Button to textField
                root.setBottom(new TextField("Focus can go here"));
                
                Scene scene = new Scene(root, 640, 480);
                
                scene.getAccelerators().addListener((MapChangeListener) c -> {
                    LOG.info("" + c.getMap());
                });
                primaryStage.setScene(scene);
                primaryStage.show();
        }
        
        @SuppressWarnings("unused")
        private static final Logger LOG = Logger
                .getLogger(DefaultButtonRemove.class.getName());
}
