/*
 * Created on 11.10.2017
 *
 */
package de.swingempire.fx.property;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class BindingShortLivedDemo extends Application {

    int counter;
    Property<String> text;
    /**
     * @return
     */
    private Parent getContent() {
        text = new SimpleObjectProperty<>();
        // not gc'ed
        Property<String> intermediate =  new SimpleObjectProperty<>("initial");
//        intermediate.bind(text);
        TextField field = new TextField();
        field.textProperty().bind(intermediate);
        Button changeText = new Button("change");
        changeText.setOnAction(e -> {
            System.gc();
//            text.setValue(text.getValue() + "X");
            field.textProperty().set("changed");
        });
        BorderPane pane = new BorderPane(field);
        pane.setBottom(changeText);
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(getContent());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
