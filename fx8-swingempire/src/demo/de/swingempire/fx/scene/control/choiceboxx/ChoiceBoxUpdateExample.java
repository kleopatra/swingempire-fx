/*
 * Created on 24.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.swingempire.fx.scene.control.choiceboxx.ChoiceBoxX;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-38394
 * Choicebox misbehaving on updating elements in items
 */
public class ChoiceBoxUpdateExample extends Application {

    @Override
    public void start(Stage primaryStage) {
//        ChoiceBox<Item> choiceBox = new ChoiceBox<>();
        ChoiceBoxX<Item> choiceBox = new ChoiceBoxX<>();
        ObservableList<Item> items = FXCollections
                .observableArrayList(item -> new Observable[] { item
                        .nameProperty() }); // the extractor
        items.addAll(IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new Item("Item " + i))
                .collect(Collectors.toList()));
        choiceBox.setItems(items);
        choiceBox.getSelectionModel().select(0);
        // To help debugging...
        items.addListener((Change<? extends Item> change) -> {
            FXUtils.prettyPrint(change);
        });

        TextField changeSelectedField = new TextField();
        changeSelectedField.disableProperty().bind(
                Bindings.isNull(choiceBox.getSelectionModel()
                        .selectedItemProperty()));
        changeSelectedField.setOnAction(event -> {
            choiceBox.getSelectionModel()
                .getSelectedItem().setName(changeSelectedField.getText());
            });

        BorderPane root = new BorderPane();
        root.setTop(choiceBox);
        root.setBottom(changeSelectedField);
        Scene scene = new Scene(root, 250, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class Item {
        public final StringProperty name = new SimpleStringProperty();

        public StringProperty nameProperty() {
            return name;
        }

        public final String getName() {
            return nameProperty().get();
        }

        public final void setName(String name) {
            nameProperty().set(name);
        }

        public Item(String name) {
            setName(name);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
