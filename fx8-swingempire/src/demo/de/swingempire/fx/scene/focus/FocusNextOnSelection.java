/*
 * Created on 08.09.2015
 *
 */
package de.swingempire.fx.scene.focus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.sun.javafx.scene.traversal.Direction;

import de.swingempire.fx.util.FXUtils;

/**
 * Transfer focus programmatically.
 * 
 * http://stackoverflow.com/q/32069107/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class FocusNextOnSelection extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    /**
     * @return
     */
    private Parent getContent() {
        ObservableList<String> items = FXCollections.observableArrayList("one", "two", "thress");
        ComboBox<String> combo = new ComboBox<>(items);
        combo.getSelectionModel().selectedItemProperty().addListener((source, ov, nv) -> {
            traverse(combo, Direction.NEXT);
        });
        Button button = new Button("dummy");
        Button select = new Button("select first");
        select.setOnAction(e -> combo.getSelectionModel().select(0));
        VBox pane = new VBox(10, combo, button, select);
        return pane;
    }

    /**
     * Utility method to transfer focus from the given node into the
     * direction. Implemented to reflectively (!) invoke Scene's
     * package-private method traverse.
     * 
     * @param node
     * @param next
     */
    public static void traverse(Node node, Direction dir) {
        Scene scene = node.getScene();
        if (scene == null) return;
        try {
            Method method = Scene.class.getDeclaredMethod("traverse", 
                    Node.class, Direction.class);
            method.setAccessible(true);
            method.invoke(scene, node, dir);
        } catch (NoSuchMethodException | SecurityException 
                | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FocusNextOnSelection.class.getName());
}
