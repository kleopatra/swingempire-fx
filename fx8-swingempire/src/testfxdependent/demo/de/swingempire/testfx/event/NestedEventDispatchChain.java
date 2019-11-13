/*
 * Created on 17.10.2019
 *
 */
package de.swingempire.testfx.event;

import java.util.logging.Logger;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * Trying an example of how/if/when building a nested dispatch chain and 
 * delivering the received event while it is delivered is evil.
 * 
 * sent to openjfx mailing list
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class NestedEventDispatchChain extends Application {
    
    private KeyCode key = DIGIT1;
    private Parent createContent() {
        Button button = new Button("the evil button!");
        // re-firing handler
        button.addEventHandler(KEY_PRESSED, e -> {
            if (key == e.getCode()) {
                System.out.println("before refire " + e);
                button.getParent().fireEvent(e);
                System.out.println("after refire " + e);
            }
        });
        
        // consuming singleton handler
        button.setOnKeyPressed(e -> {
            if (key == e.getCode()) {
                e.consume();
                System.out.println("consumed in singleton " + e.getCode());
            }
         });
        BorderPane content = new BorderPane(button);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        // accelerator that shouldn't be triggered because singleton handler consumed
        scene.getAccelerators().put(KeyCombination.keyCombination(key.getName()), () -> {
            System.out.println("accelerator triggered for " + key);
        });
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(NestedEventDispatchChain.class.getName());

}
