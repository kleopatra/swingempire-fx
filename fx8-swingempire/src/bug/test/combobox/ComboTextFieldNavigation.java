/*
 * Created on 21.08.2018
 *
 */
package test.combobox;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/51943654/203657
 * ctrl-A not working if dropDown showing
 * 
 * 
 * reasons/fixes:
 * combo - listViewBehavior has mapping that consumes the A.shortCut 
 *     grab mapping from inputMap and set its auto-consume to false
 */
public class ComboTextFieldNavigation extends Application {
    
    @Override
    public void start(Stage stage) {
        HBox root = new HBox();

        ObservableList<String> items = FXCollections.observableArrayList(
                "One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten");
        ComboBox<String> cb = new ComboBox<String>(items);
        cb.setEditable(true);
        root.getChildren().addAll(cb);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboTextFieldNavigation.class.getName());
}

