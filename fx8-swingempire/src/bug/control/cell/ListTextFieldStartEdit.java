/*
 * Created 11.09.2021 
 */

package control.cell;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Activate edit by keyboard (same for exp-cellediting, fx17, fx11):
 * - with enter: ok
 * - with space: second start on same cell is deleting content (and adding that space) 
 * - with F2: ok
 * 
 * Activate focused cell (but not selected) - same for exp-cellediting, fx17
 * - text in field is _not_ selected initially
 */
public class ListTextFieldStartEdit extends Application {
    
    int count;
    private Parent createContent() {
        ObservableList<String> items = FXCollections.observableArrayList(
                Stream.generate(() -> "item" + count++)
                .limit(50)
                .collect(Collectors.toList()));
        ListView<String> list = new ListView<>(items);
        list.setEditable(true);
        list.setCellFactory(TextFieldListCell.forListView());
        
        VBox content = new VBox(10, list);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(de.swingempire.fx.util.FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListTextFieldStartEdit.class.getName());

}
