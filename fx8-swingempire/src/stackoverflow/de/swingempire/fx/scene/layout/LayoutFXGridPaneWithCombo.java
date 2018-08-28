/*
 * Created on 28.08.2018
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application; 
import javafx.collections.FXCollections; 
import javafx.collections.ObservableList; 
import javafx.scene.Scene; 
import javafx.scene.control.ComboBox; 
import javafx.scene.control.Label; 
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.Priority; 
import javafx.stage.Stage; 

/**
 * https://bugs.openjdk.java.net/browse/JDK-8210037
 * resizing probs if combo popup open
 * 
 * run, resize: both are same width
 * open popup: both are resized to something that's not the same
 * 
 * fine if setting maxSize of either root or combo to maxValue -1 (instead of maxValue)
 * might be an overflow error
 * somewhere along the size calculation path (commented the issue)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class LayoutFXGridPaneWithCombo extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final ObservableList<String> fruits = FXCollections.observableArrayList(
                "Apple", "Banana", "Pineapple",
                "Some kind of fruit that has a very very long name");
        final GridPane root = new GridPane();
//        root.setMaxSize(Double.MAX_VALUE - 1, Double.MAX_VALUE -1);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
//        root.setMaxSize(2000, 2000);
        root.setPrefSize(GridPane.USE_COMPUTED_SIZE,
                GridPane.USE_COMPUTED_SIZE);

        final Label label1 = new Label("Test");
        final ComboBox<String> combo1 = new ComboBox<>(fruits);
        combo1.setMaxWidth(Double.MAX_VALUE-1);
        GridPane.setHgrow(label1, Priority.SOMETIMES);
        GridPane.setHgrow(combo1, Priority.ALWAYS);

        final Label label2 = new Label("Test");
        final ComboBox<String> combo2 = new ComboBox<>(fruits);
        combo2.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(label2, Priority.SOMETIMES);
        GridPane.setHgrow(combo2, Priority.ALWAYS);

        root.addRow(0, label1, combo1, label2, combo2);

        final Scene sc = new Scene(root);
        primaryStage.setScene(sc);
        primaryStage.show();
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }
} 