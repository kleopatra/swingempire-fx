/*
 * Created on 15.11.2017
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;

import de.swingempire.fx.util.DebugUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Confusion about selectionModel semantics:
 * https://stackoverflow.com/q/47065696/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListRemoveSelected extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        ListView<Locale> listView = new ListView<>(createList());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE) {
                event.consume();
                if (event.isShiftDown()) {
                    ObservableList<Locale> selectedItems = listView.getSelectionModel().getSelectedItems();
                    listView.getItems().removeAll(selectedItems);
                } else {
                    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
//                    if (selectedIndex >= 0) {
                        if (listView.getSelectionModel().isSelected(selectedIndex)) {
                            listView.getItems().remove(selectedIndex);
                        }
                }
            }
        });
        Button print = new Button("print selection state");
        print.setOnAction(e -> DebugUtils.printSelectionState(listView));
        BorderPane pane = new BorderPane(listView);
        pane.setBottom(print);
        return pane;
    }
    
    private ObservableList<Locale> createList() {
        ObservableList<Locale> list = FXCollections.observableArrayList(Locale.getAvailableLocales());
        list.remove(0); // is an empty locale on my maschine
        return list;
    }
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(getContent());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
