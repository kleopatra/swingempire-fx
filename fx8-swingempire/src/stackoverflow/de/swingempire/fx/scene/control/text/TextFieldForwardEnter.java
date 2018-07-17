/*
 * Created on 17.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldForwardEnter extends Application {

    /** 
     * fishy code snippet from TextFieldBehaviour: 
     * 
     * during fire, the actionEvent without target is copied - such that
     * the check for being consumed of the original has no effect
     */
//    @Override protected void fire(KeyEvent event) {
//        TextField textField = getNode();
//        EventHandler<ActionEvent> onAction = textField.getOnAction();
//        ActionEvent actionEvent = new ActionEvent(textField, null);
//
//        textField.commitValue();
//        textField.fireEvent(actionEvent);
//
//        if (onAction == null && !actionEvent.isConsumed()) {
//            forwardToParent(event);
//        }
//    }

    private Parent createContent() {
        TextField addedActionHandler = new TextField("added handler: ");
        addedActionHandler.addEventHandler(ActionEvent.ACTION, e -> {
            System.out.println("in added: " + e);
            e.consume();
        });
        
        TextField setActionHandler = new TextField("set handler: ");
        setActionHandler.setOnAction(e -> {
            System.out.println("in set: " + e);
            e.consume();
        });
        
        TextField both = new TextField("set/add handler: ");
        // siblings are not effected by consume, only successors in the 
        // dispatchChain
        both.addEventHandler(ActionEvent.ACTION, e -> {
            System.out.println("in added: " + e);
            e.consume();
        });
        both.setOnAction(e -> {
            System.out.println("in set: " + e);
//            e.consume();
        });
        
        VBox pane = new VBox(10, addedActionHandler, setActionHandler, both);
        
        pane.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            System.out.println("in parent: " + e);
        });
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldForwardEnter.class.getName());

}
