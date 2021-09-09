/*
 * Created 08.09.2021 
 */

package control.cell;

import java.util.List;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//import support.VirtualFlowTestUtils;


/**
 * https://bugs.openjdk.java.net/browse/JDK-8181907
 * custom cell with text "empty" (empty), "null (not empty with null item) and text for not-null value
 * start with a single null (not-empty with null) item
 * dynamically add not-null values
 * 
 * fx11: can be seen when increasing height of window - null appears sooner or later
 */
public class ListNullItemArtefact extends Application {
    public static void main(String[] args) {
            launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add(null);
            ListView<String> list = new ListView<>(items);
            
            list.setCellFactory(cc -> {
                ListCell<String> cell = new ListCell<>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("empty");
                        } else if (item == null) {
                            setText("null");
                        } else {
                            setText(item);
                        }
                    }
                    
                    
                };
                return cell;
            });            
            Button minus = new Button("-");
            Button plus = new Button("+");
            minus.disableProperty().bind(Bindings.isEmpty(items));
            minus.setOnAction(e -> items.remove(items.size() - 1));
            plus.setOnAction(e -> items.add("valued"));
            
            Label label = new Label();
            label.textProperty().bind(Bindings.size(items).asString());
            
            // quick check
            Button logCells = new Button("log");
            logCells.setOnAction(e -> {
//                VirtualFlow<?> virtualFlow = VirtualFlowTestUtils.getVirtualFlow(list);
//                List<IndexedCell> cells = (List<IndexedCell>) virtualFlow.getCells();
//                for (IndexedCell cell : cells) {
//                    System.out.println("cell: " + cell.getIndex() + " " + cell.getText());
//                }
            });
            VBox root = new VBox(new HBox(minus, plus, logCells), list, label);
            
            Scene scene = new Scene(root, 400, 300);
            primaryStage.setScene(scene);
            primaryStage.show();
    }
}