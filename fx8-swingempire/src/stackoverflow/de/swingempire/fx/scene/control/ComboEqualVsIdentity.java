/*
 * Created on 06.01.2019
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ComboEqualVsIdentity extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Group group = new Group();
            Scene scene = new Scene(group,100,40);

            ObservableList<EqualContent> content = FXCollections.observableArrayList();
            content.add(new EqualContent("A"));
            content.add(new EqualContent("B"));

            Label selection = new Label();

            ComboBox<EqualContent> demoBox = new ComboBox<EqualContent>(content);
            demoBox.setOnAction(event -> selection.setText(" selected: "+demoBox.getValue()));

            demoBox.valueProperty().addListener((src, ov, nv) -> System.out.println("value changed: " + demoBox.getValue()));
            demoBox.valueProperty().addListener(c -> System.out.println("value invalidated: " + demoBox.getValue()));
            group.getChildren().add(new HBox(demoBox, selection));

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    class EqualContent {
        private String name;
        EqualContent(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
        @Override
        public boolean equals(Object other) {
            return other != null;
        }
        @Override
        public int hashCode() {
            return 0;
        }
    }
}

