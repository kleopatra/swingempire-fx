/*
 * Created on 18.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

    /**
     * Reported: https://javafx-jira.kenai.com/browse/RT-39030
     * 
     * - sanity: open popup and see it's empty (expected)
     * - press button
     * - open popup
     * - expected: see added item
     * - actual: empty
     */
public class Combo15793Regression extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ComboBox box = new ComboBox();
        Button button = new Button("set empty list and add item");
        button.setOnAction(e -> {
            box.setItems(FXCollections.observableArrayList());
            box.getItems().add("Toto");
        });
        Parent root = new VBox(box, button);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
