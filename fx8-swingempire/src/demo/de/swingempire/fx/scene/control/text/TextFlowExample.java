/*
 * Created on 05.05.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFlowExample extends Application {

    int count = 0;
    private Parent getContent() {
        ObservableList<Text> bullets = FXCollections.observableArrayList(
                new Text("dummy1"), new Text("other"));
        TextFlow flow = new TextFlow();
        Bindings.bindContent(flow.getChildren(), bullets);
        BorderPane pane = new BorderPane(flow);
        Button add = new Button("Add");
        add.setOnAction(e -> bullets.add(new Text("added " + count++)));
        pane.setBottom(add);
        return pane;
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
