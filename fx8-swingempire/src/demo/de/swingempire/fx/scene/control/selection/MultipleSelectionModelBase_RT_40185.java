/*
 * Created on 04.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.Collections;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;


/**
 * ClearSelection in MultipleSelectionModelBase is incorrect.
 * https://javafx-jira.kenai.com/browse/RT-40185
 */
public class MultipleSelectionModelBase_RT_40185 extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        final ListView<String> lv = new ListView<>();
//        lv.setSelectionModel(new SimpleListSelectionModel<>(lv));
        final ArrayList<Integer> expected = new ArrayList<>();
        Collections.addAll(expected, 1, 2);

        lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lv.getSelectionModel()
                .getSelectedIndices()
                .addListener(
                        (ListChangeListener<Integer>) change -> {
                            while (change.next()) {
                                if (change.wasRemoved()) {
                                    System.out.println(change.getRemoved()
                                            .toString()
                                            + " <-> "
                                            + expected.toString());
                                    if (!change.getRemoved().equals(expected)) 
                                        throw new IllegalStateException("expected/actual: " + expected + "/" + change.getRemoved());
                                }
                            }
                        });

        lv.getItems().addAll("-0-", "-1-", "-2-");
        lv.getSelectionModel().selectIndices(1, 2);
        lv.getSelectionModel().clearSelection();
        System.exit(-1);
    }
}
