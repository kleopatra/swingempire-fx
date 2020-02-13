/*
 * Created on 10.02.2020
 *
 */
package de.swingempire.fx.binding;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60139395/203657
 * remove listener implemented as method handle
 * 
 * doesn't work, it's registered multiple times - but then, the OP's app
 * design seems to be broken.
 * 
 * Requirement: in collection of nodes (f.i. textFields), one is the-current - a
 * listener should be un/registered to a given (f.i. text-) property of the-current
 * only.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AddRemoveListener extends Application {

    int count;
    private Parent createContent() {
        TextField field = new TextField("sometext");
        Button button = new Button("remove-add ad-hoc");
        // method handles pointing to the same method are not identical!
        button.setOnAction(e -> {
            System.out.println("add-hoc " + count++);
            field.textProperty().removeListener(this::handleChange);
            field.textProperty().addListener(this::handleChange);
        });
        InvalidationListener il = this::handleChange;
        Button useFixed = new Button("remove-add-fixed");
        useFixed.setOnAction(e -> {
            System.out.println("fixed" + count++);
            field.textProperty().removeListener(il);
            field.textProperty().addListener(il);
            
        });
        VBox content = new VBox(10, field, button, useFixed);
        return content;
    }

    private void handleChange(Observable ov) {
        System.out.println("notified! ");
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
            .getLogger(AddRemoveListener.class.getName());

}
