/*
 * Created on 13.12.2017
 *
 */
package test.selection;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Issue
 * https://bugs.openjdk.java.net/browse/JDK-8193442
 * 
 * Incorrect selection state after remove
 * 
 * sounds similar to 
 * https://bugs.openjdk.java.net/browse/JDK-8187596
 * 
 * see also SO
 * https://stackoverflow.com/q/47781235/203657
 * EXPECTED VERSUS ACTUAL BEHAVIOR :
EXPECTED -
Selected index = 4
Selected item = Node 1
Selected index = 4
Selected item = Node 1
ACTUAL -
Selected index = 4
Selected item = Node 1
Selected index = 3
Selected item = Sub Node 0-1

 * That is, the selected Index is decreased by 1 even though the removal is 
 * down the tree and should not have any effect.
 */
public class TreeTableSelectionAndRemoveItem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        TreeItem<String> rootNode = new TreeItem<String>("Root");
        rootNode.setExpanded(true);
        for (int i = 0; i < 3; i++) {
            rootNode.getChildren().add(new TreeItem<>("Node " + i));
        }
        for (int i = 0; i < 2; i++) {
            TreeItem<String> node = rootNode.getChildren().get(i);
            node.setExpanded(true);
            for (int j = 0; j < 2; j++) {
                node.getChildren().add(new TreeItem<>("Sub Node " + i + "-" + j));
            }
        }

        TreeTableColumn<String, String> column = new TreeTableColumn<>("Nodes");
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<String, String> p) -> {
            return new ReadOnlyStringWrapper(p.getValue().getValue());
        });
        column.setPrefWidth(200);

        TreeTableView<String> table = new TreeTableView<>(rootNode);
//        table.setShowRoot(false);
        table.getColumns().add(column);

        int selectIndex = 4; // select "Node 1"
        int removeIndex = 2; // remove "Node 2"
        table.getSelectionModel().select(selectIndex);
        System.out.println("Selected index = " + table.getSelectionModel().getSelectedIndex());
        System.out.println("Selected item  = " + table.getSelectionModel().getSelectedItem().getValue());
        Button remove =  new Button("remove root child at index 2");
        remove.setOnAction(e -> {
            table.getRoot().getChildren().remove(removeIndex);
            System.out.println("Selected index = " + table.getSelectionModel().getSelectedIndex());
            System.out.println("Selected item  = " + table.getSelectionModel().getSelectedItem().getValue());
            
            
        });
        HBox buttons = new HBox(10, remove);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        primaryStage.setTitle("Tree Table View Selection");
        primaryStage.setScene(new Scene(content, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
