/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;

import de.swingempire.fx.scene.control.cell.DebugListCell;
import de.swingempire.fx.scene.control.cell.DebugTextFieldListCell;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
;/**
 * Trying to add item/start edit on new item in commitHandler:
 * https://stackoverflow.com/q/46047134/203657
 * 
 */
public class ListViewCommitHandler extends Application {

    private ListView<String> simpleList;
    private ChangeListener skinListener = (src, ov, nv) -> skinChanged();
    private VirtualFlow flow;
    private int expectedEditIndex = -1;
    protected boolean watchCancel;
    /**
     * responsible for cancelEdit after modification of items is ListViewSkin as
     * side-effect of fix to
     * 
     * https://bugs.openjdk.java.net/browse/JDK-8094887
     * 
     * so trying to install our own listener after skin did doesn't help: still
     * not edited
     * 
     * Trying same as in TablePersonAddRowAndEdit: which doesn't work neither if item
     * added in commitHandler (only if added with button)
     */
    private void skinChanged() {
        simpleList.skinProperty().removeListener(skinListener);
        // start in items listener - working if cell switched out of editing 
        // before notification
        simpleList.getItems().addListener((Change<? extends String> c) -> {
            while (c.next()) {
                if (c.wasAdded() && ! c.wasRemoved()) {
                    // force the re-layout before starting the edit
                    // still need
                    simpleList.layout();
                    p("before edit");
                    simpleList.edit(c.getFrom());
                    return;
                }
            }
        });

    }


    @Override
    public void start(Stage primaryStage) {
        simpleList = new ListView<>(FXCollections.observableArrayList("Item1"// {
               , "Item2", "Item3", "Item4")) {
//              same as edit in commit handler
//                @Override
//                protected void layoutChildren() {
//                    super.layoutChildren();
//                    IndexedCell cell =  getCell(this, expectedEditIndex);
//                    if (cell != null) {
//                        cell.startEdit();
//                    }
//                }
//
            
        };
        simpleList.setEditable(true);
        simpleList.skinProperty().addListener(skinListener);
        
//        simpleList.setCellFactory(TextFieldListCell.forListView());
//        simpleList.setCellFactory(DebugTextFieldListCell.forListView());
        simpleList.setCellFactory(e -> {
            DebugListCell cell = new DebugTextFieldListCell(new DefaultStringConverter());
//            cell.setPostCommit(list -> {
//                ((ListView)list).edit(expectedEditIndex);
//            });
            return cell;
        });

        simpleList.setOnEditStart(t -> {
            System.out.println(
                    "setOnEditStart " + t.getIndex() + " /" + t.getNewValue());
        });
//        simpleList.addEventHandler(ListView.editCommitEvent(), t -> {
        simpleList.setOnEditCommit(t -> {
            simpleList.getItems().set(t.getIndex(), t.getNewValue());
            if (t.getIndex() == simpleList.getItems().size() - 1) {
                int index = t.getIndex() + 1;
                 System.out.println("setOnEditCommit - last " + t.getIndex() +
                 " /" + t);
                simpleList.getItems().add("newItem");
                expectedEditIndex = index;
                simpleList.getSelectionModel().select(index);
                simpleList.getFocusModel().focus(index);
                simpleList.scrollTo(index);
                //does not start edit and leads to cancel with weird index
                // start == 4, cancel == 6
//                simpleList.edit(index);
            }

        });

        simpleList.setOnEditCancel(t -> {
            p("setOnEditCancel " + t.getIndex() + " /" + t.getNewValue());
            if (watchCancel) {
                
                 new RuntimeException("who-is-calling?").printStackTrace();
            }
        });

        BorderPane root = new BorderPane(simpleList);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void p(String text) {
        System.out.println(text);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
