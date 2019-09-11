/*
 * Created on 11.09.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static javafx.collections.FXCollections.*;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57877142/203657
 * <br>
 * scroll combo's popup content to make selected item visible
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxScroll extends Application {
    
    int count;
    private Parent createContent() {
        List<String> data = Stream.generate(() -> "item " + count++).limit(30).collect(toList());
        ComboBox<String> combo = new ComboBox<>(observableArrayList(data));
        
        // scroll just before the comboBox is showing
        combo.setOnShowing(e -> {
            ListView list = (ListView) ((ComboBoxListViewSkin) combo.getSkin()).getPopupContent();
            list.scrollTo(Math.max(0, combo.getSelectionModel().getSelectedIndex()));
        });
        
        // to see the handler working initially
        combo.getSelectionModel().select(25);
        // a button to clear see the handler working on clearing selection
        Button clear = new Button("clear selection");
        clear.setOnAction(e -> combo.getSelectionModel().clearSelection());
        BorderPane content = new BorderPane(combo);
        content.setBottom(new HBox(10, clear));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxScroll.class.getName());

}
