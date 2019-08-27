/*
 * Created on 27.08.2019
 *
 */
package test.tree;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * from openjfx-dev list
 * - no indentation at all
 * - expanded icon in text
 * 
 *  weird setup: cellValue is a Text?
 * ------
 * might be related to 
 * https://bugs.openjdk.java.net/browse/JDK-8212640 and others
 * 
 * @author Jeanette Winzenburg, Berlin
 * see bug.test.TreeTableIndentExperiment
 */
public class TreeTableGraphicBug extends Application {

    public static void main(String[] args) {
        TreeTableGraphicBug.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        
        StackPane root = new StackPane();
        TreeTableView<String> treeTableView = new TreeTableView<>();
        treeTableView.setShowRoot(false);
        root.getChildren().add(treeTableView);

        // original: uses Node as data .. don't
//        TreeTableColumn<String, Text> column = new TreeTableColumn<>("Column");
//        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(
//                new Text(param.getValue().getValue())
//                ));
        // working: use string
        TreeTableColumn<String, String> column = new TreeTableColumn<>("Column");
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<String>(
                param.getValue().getValue()));
        column.setPrefWidth(200);
        
        treeTableView.getColumns().add(column);



        TreeItem<String> rootItem = new TreeItem<>("");
        treeTableView.setRoot(rootItem);

        TreeItem<String> item1 = new TreeItem<>("LEVEL1");
        item1.setExpanded(true);
        rootItem.getChildren().add(item1);

        TreeItem<String> item2 = new TreeItem<>("LEVEL2");
        item2.setExpanded(true);
        item1.getChildren().add(item2);

        TreeItem<String> item3 = new TreeItem<>("LEVEL2_1");
        item3.setExpanded(true);
        item2.getChildren().add(item3);

        Scene scene = new Scene(root, 250, 150);

        stage.setTitle("JavaFX11");
        stage.setScene(scene);
        stage.show();
    }
}