/*
 * Created on 01.03.2019
 *
 */
package test.selection;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * bug report on javafxports:
 * https://github.com/javafxports/openjdk-jfx/issues/375
 * @author Jeanette Winzenburg, Berlin
 */
public class ListSelectionIssueFromjavafxports extends Application {
    
    public void testSelectedFocused375() {
        ListView<String> listView1 = new ListView<>();
        ListView<String> listView2 = new ListView<>();
        ListView<String> listView3 = new ListView<>();
//        listView1.getItems().add("TEST1");
        listView1.setItems(FXCollections.observableArrayList("TEST1"));
        listView1.getSelectionModel().select(0);
        listView1.getFocusModel().focus(0);
        printLog("1-1", listView1);
        listView1.getItems().set(0, "TEST2");
        printLog("1-2", listView1);

        listView2.getItems().addAll("TEST1", "test1");
        listView2.getSelectionModel().select(0);
        listView2.getFocusModel().focus(0);
        printLog("2-1", listView2);
        listView2.getItems().set(0, "TEST2");
        printLog("2-2", listView2);
        listView2.getItems().set(0, "TEST3");
        printLog("2-3", listView2);

        listView3.setItems(FXCollections.observableArrayList("TEST1"));
        listView3.getSelectionModel().select(0);
        listView3.getFocusModel().focus(0);
        printLog("3-1", listView3);
        listView3.getItems().set(0, "TEST2");
        printLog("3-2", listView3);
        listView3.getItems().set(0, "TEST3");
        printLog("3-3", listView3);
    }

    private void printLog(String label, ListView<String> listView) {
            System.out.println(label + " selectedIndex : " + listView.getSelectionModel().getSelectedIndex() + ", selectedItem : " + listView.getSelectionModel().getSelectedItem());
            System.out.println(label + " focusedIndex : " + listView.getFocusModel().getFocusedIndex() + ", focusedItem : " + listView.getFocusModel().getFocusedItem());
    }

    
    private Parent createContent() {
        
        return new Button("nothing");
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
        testSelectedFocused375();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListSelectionIssueFromjavafxports.class.getName());

}
