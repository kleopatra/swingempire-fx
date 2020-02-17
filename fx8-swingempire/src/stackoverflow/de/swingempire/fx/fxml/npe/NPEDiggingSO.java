/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class NPEDiggingSO extends Application {

    
    private static class InnerListCell extends ListCell<String> {
        private Button button;

        public InnerListCell() {
            button = new Button();
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                button.setText(item);
                setGraphic(button);
            }
        }

    }

    private static class OuterListCell extends ListCell<String> {
        private ListView<String> cellListView;

        public OuterListCell() {
            setPrefHeight(300);
            setPrefWidth(300);

            cellListView = new ListView<>();
            cellListView.setCellFactory(c -> new InnerListCell());
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                cellListView.getItems().setAll(item);
                setGraphic(cellListView);
                // doesn't help
//                cellListView.requestFocus();
            }
        }

    }

    private Parent createContent() {
        List<String> items = Arrays.stream(Locale.getAvailableLocales())
                .map(l -> l.getDisplayCountry())
                .collect(Collectors.toList());
        ObservableList<String> model = FXCollections.observableArrayList(
                items);
        ListView<String> outer = new ListView<>(model);
        // need nested listCell
        outer.setCellFactory(c -> new OuterListCell());
        // this is fine
        //outer.setCellFactory(c -> new InnerListCell());
        BorderPane content = new BorderPane(outer);
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

}
