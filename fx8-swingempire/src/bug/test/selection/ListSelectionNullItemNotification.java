/*
 * Created on 22.02.2018
 *
 */
package test.selection;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48922591/203657
 * incorrect change from selectedItems
 * 
 * select1, select2, deselect1 -> items = null
 * select2, select1, deselect2 -> okay
 * 
 * fx8 bug that wont be fixed:
 * https://bugs.openjdk.java.net/browse/JDK-8160973
 * 
 */
public class ListSelectionNullItemNotification extends Application {

    ListView<String> listView;
    List<String> backingItems;
    
    public void initialize(URL url, ResourceBundle rb) 
    {
        listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        fillList();
        listView.getSelectionModel().getSelectedItems().addListener(
                (Change<? extends String> change) ->
                {
                    backingItems.clear();

                    ObservableList<String> oList = listView.getSelectionModel().getSelectedItems(); 
                    System.out.println(oList);          
                });
    }

    private void fillList() 
    {
        backingItems = new  ArrayList<>();
        backingItems.add("1.item");
        backingItems.add("2.item");
        backingItems.add("3.item");

        ObservableList<String> items = FXCollections.observableArrayList(backingItems);
        listView.setItems(items);

    }


    private Parent createContent() {
        initialize(null, null);
        BorderPane pane = new BorderPane(listView);
        return pane;
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
            .getLogger(ListSelectionNullItemNotification.class.getName());

}
