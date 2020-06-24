/*
 * Created on 19.06.2020
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ListCellScrollPerformance extends Application {
    
    ListView<Locale> list;
    private Parent createContent() {
        ObservableList<Locale> data = FXCollections.observableArrayList(Locale.getAvailableLocales());
        list = new ListView<>(data);
        list.getSelectionModel().selectFirst();
        Button start = new Button("start selecting");
        start.setOnAction(e -> {
            // do start automatic selecting
        });
        
        BorderPane content = new BorderPane(list);
        content.setBottom(new HBox(10, start));
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
            .getLogger(ListCellScrollPerformance.class.getName());

}
