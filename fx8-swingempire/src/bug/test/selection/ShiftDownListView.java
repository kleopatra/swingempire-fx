/*
 * Created on 16.02.2018
 *
 */
package test.selection;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.swingempire.fx.scene.control.selection.SelectionAndModification;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8197985
 * Shift-down throws IOOB if middle element was selected (of 3)
 * okay for others
 * 
 * for more elements: throws whenever extending from any element 1 or
 * higher. Though: can't reproduce with SelectionAndModification? was error
 * (forgot the listener in the latter)
 * 
 * The error is in MultipleSelectionModelBase.SelectedIndices.set(int, int...): for 
 * size 1 it fires the _value_ instead of the index of the value!
 * 
 * Seems okay for table, why? selectIndices is re-implemented from scratch (not using
 * the SelectedIndicesList from the base impl) because it needs to support cell selection.
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 * @see SelectionAndModification
 */
public class ShiftDownListView extends Application {

    ObservableList<Locale> locales = FXCollections.observableArrayList(Locale.getAvailableLocales());
    List<String> localeNames = locales.stream().map(l -> l.getDisplayName()).collect(Collectors.toList());
    ObservableList<String> listitems = FXCollections.observableArrayList("zero", "one", "two", "three", "4", "5");
    @Override
    public void start(Stage primaryStage) {
//        listitems = FXCollections.observableArrayList(localeNames);
        final ListView<String> lv = new ListView<>();
        lv.setItems(listitems);

//        ListViewSkin s;
        lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lv.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) ch -> {
//            FXUtils.prettyPrint(ch);
            while (ch.next()) {
                if (ch.wasAdded()) {
                    System.out.println("+" + ch.getAddedSubList());
                }
            }
        });

        final HBox hbox = new HBox();
        hbox.getChildren().add(lv);
        primaryStage.setScene(new Scene(hbox));
        
        primaryStage.show();
        Platform.runLater(() -> {
//            lv.getSelectionModel().select(1);
//            lv.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, true, false, false, false));
        }
        );
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

