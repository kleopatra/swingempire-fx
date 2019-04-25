/*
 * Created on 24.04.2019
 *
 */
package de.swingempire.fx.binding;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SetContainsBinding extends Application {

    
    private Parent createContent() {
        ObservableSet<Integer> numberSet = FXCollections.observableSet(1, 2, 3, 4);
        BooleanProperty contained = new SimpleBooleanProperty(this, "contained", true);
        int number = 2;
        numberSet.addListener((SetChangeListener<Integer>) c -> {
            contained.set(c.getSet().contains(number));
        });
        
        Circle circle = new Circle(50);
        circle.setFill(Color.RED);
        circle.visibleProperty().bind(contained);
        
        SetProperty<Integer> setProperty = new SimpleSetProperty<>(numberSet);
        BooleanBinding setBinding = 
                Bindings.createBooleanBinding(() -> setProperty.contains(number), setProperty);
        
        Circle bindingC = new Circle(50);
        bindingC.setFill(Color.BLUEVIOLET);
        bindingC.visibleProperty().bind(setBinding);
        
        HBox circles = new HBox(10, circle, bindingC);
        
        Button remove = new Button("remove");
        remove.setOnAction(e -> {
            numberSet.remove(number);
        });
        Button add = new Button("add");
        add.setOnAction(e -> {
            numberSet.add(number);
        });
        BorderPane content = new BorderPane(circles);
        content.setBottom(new HBox(10, remove, add));
        return content;
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
            .getLogger(SetContainsBinding.class.getName());

}
