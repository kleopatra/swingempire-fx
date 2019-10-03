/*
 * Created on 18.09.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import static javafx.scene.input.KeyEvent.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Oct. 2019: closed as cannot-reproduce - could be non-mac only?
 * Mac has no binding for enter in buttonBehaviour.
 * 
 * Controls' behavior must not depend on sequence of handler registration
 * <p>
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8231245
 * 
 * <hr>
 * Quick check: sibling handlers are notified
 * <p>
 * but: again difference between register before/after showing
 * before: action not triggered if one of the handlers consumes the event
 * after: action triggered always, independent of consumed or not
 * <p>
 * behavior different from fx8: action never triggered if keyEvent
 *    is consumed, doesn't matter if registered before/after
 *    same in Swing
 *<p>    
 * --> this seems to be the expected behavior   
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AddEventHandler extends Application {

    private Button before;
    private Button after;
    
    
    protected void registerHandlers(Button button) {
        button.addEventHandler(KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
            }
            System.out.println(e.getCode() + " received in first");
        });
        button.addEventHandler(KEY_PRESSED, e -> {
            System.out.println(e.getCode() + " received in second");
        });
        button.setOnKeyPressed(e -> {
            System.out.println("singleton");
        });
        button.setOnAction(a -> {
            System.out.println("action");
        });
    }
    
    private Parent createContent() {
        // some simple control that's focusable
        before = new Button("handlers registered BEFORE showing");
        registerHandlers(before);  
        after = new Button("handlers registered AFTER showing");
        VBox content = new VBox(10, before, after); 
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
        registerHandlers(after);
        String version = "java: " + System.getProperty("java.version")+ "-" + System.getProperty("java.vm.version")
             + " (" + System.getProperty("os.arch") + ")"
             + "\n  fx: " + System.getProperty("javafx.runtime.version") ;
        System.out.println(version);

    }

    public static void main(String[] args) {
        launch(args);
    }

//    @SuppressWarnings("unused")
//    private static final Logger LOG = Logger
//            .getLogger(AddEventHandler.class.getName());

}
