/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;
import static de.swingempire.fx.scene.control.edit.ListAutoCell.*;

import javafx.application.Application;


import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
;/**
 * Trying to add item/start edit on new item in commitHandler:
 * https://stackoverflow.com/q/46047134/203657
 * 
 * Here: try a custom skin that starts edit if requested in layout children.
 * doesn't work - can't find the exact path that leads to cancelEdit via
 * updateFocus ...
 */
public class ListViewAutoEditInSkin extends Application {

    private ListView<String> simpleList;
    private int expectedEditIndex = -1;
    
    public static class MyListViewSkin<T> extends ListViewSkin<T> {

        VirtualFlow flow;
        /**
         * @param listView
         */
        public MyListViewSkin(ListView<T> listView) {
            super(listView);
        }
        
        @Override
        protected void layoutChildren(double arg0, double arg1, double arg2,
                double arg3) {
            super.layoutChildren(arg0, arg1, arg2, arg3);
            ListView<T> skinnable = getSkinnable();
            ObservableMap<Object, Object> properties = skinnable.getProperties();
            Integer editPending = (Integer) properties.get("PENDING_EDIT_KEY");
            if (editPending != null && editPending >= 0) {
                p("detecting edit pending? " + editPending);
                properties.put("PENDING_EDIT_KEY", null);
                skinnable.edit(editPending);
            }
        }
    }
    /**
     * Callback for editTimer. Implemented to scroll to and force
     * edit of cell at expectedEditIndex.
     */
    private void checkEdit() {
        if (expectedEditIndex < 0) return;
        if (expectedEditIndex == simpleList.getEditingIndex()) {
            expectedEditIndex = -1;
            return;
        }
        int index = expectedEditIndex;
        expectedEditIndex = -1;
        simpleList.scrollTo(index);
        simpleList.edit(index);
    }

    @Override
    public void start(Stage primaryStage) {
        simpleList = new ListView<>(FXCollections.observableArrayList("Item1", "item2", "item2", "item3")) {

//            @Override
//            protected Skin<?> createDefaultSkin() {
//                return new MyListViewSkin<>(this);
//            }
//            
        };
        simpleList.setEditable(true);
        
        simpleList.setCellFactory(TextFieldListAutoCell.forListView());

        simpleList.setOnEditStart(t -> p("edit start: " + t.getIndex()));
        simpleList.setOnEditCancel(t -> p("edit cancel: " + t.getIndex()));
        simpleList.setOnEditCommit(t -> {
            p("edit commit: " + t.getIndex());
            // any modification of the items will trigger a cancel
            simpleList.getItems().set(t.getIndex(), t.getNewValue());
            p("item set");
            if (t.getIndex() == simpleList.getItems().size() - 1) {
                expectedEditIndex = t.getIndex() + 1;
                  p("expected edit? " + expectedEditIndex);
                simpleList.getItems().add("newItem");
                p("item added");
                simpleList.getSelectionModel().select(expectedEditIndex);
                // this does work
                simpleList.scrollTo(expectedEditIndex);
                simpleList.getProperties().put("PENDING_EDIT_KEY", expectedEditIndex);
                // this does not work
                simpleList.edit(expectedEditIndex);
                
                // ... so we start a timer to force
                // uncomment for a brittle solution ;)
            } else {
                // reset .. a bit paranoid here ;)
//                expectedEditIndex = -1;
            }

        });


        BorderPane root = new BorderPane(simpleList);
        Scene scene = new Scene(root, 300, 150);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void p(String t) {
        System.out.println(t);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
