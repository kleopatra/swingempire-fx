/*
 * Created on 27.11.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8234885
 * size glitches of parent/child columns by dragging
 * 
 * - resize any of the children to min, go further
 * - move back and forth: at some time - not clearly reproducible - the min is fixed
 * - also note, that the text of all headers "move through" on initial min, even though its
 *   not visible due to being too small 
 * 
 */
public class TreeTableColumnWidthBug {

    public static void main(String[] args) {
        Application.launch(MainFx.class, args);
    }

    public static class MainFx extends Application {

        @Override
        public void start(Stage aPrimaryStage) throws Exception {

            TreeTableView treeTable = new TreeTableView<>();
            treeTable.setPrefWidth(400);

            treeTable.getColumns().add(generateTreeTableColumnColumn("Col1"));
            treeTable.getColumns().add(generateTreeTableColumnColumn("Col2"));
            treeTable.getColumns().add(generateTreeTableColumnColumn("Col3"));
            treeTable.getColumns().add(generateTreeTableColumnColumn("Col4"));
            treeTable.getColumns().add(generateTreeTableColumnColumn("Col5"));

            TableView table = new TableView();
            table.setPrefWidth(400);
            
            table.getColumns().add(generateColumn("Col1"));
            table.getColumns().add(generateColumn("Col2"));
            table.getColumns().add(generateColumn("Col3"));
            table.getColumns().add(generateColumn("Col4"));
            table.getColumns().add(generateColumn("Col5"));
            
            // Create the VBox
            HBox root = new HBox(10, treeTable, table);

            // Create the Scene
            Scene scene = new Scene(root);
            // Add the Scene to the Stage
            aPrimaryStage.setScene(scene);
            // Set the Title
            aPrimaryStage.setTitle("A simple TreeTableView");
            // Display the Stage
            aPrimaryStage.show();
        }
    }

    private static TableColumn generateColumn(String aName) {
        TableColumn col1 = new TableColumn(aName);
        TableColumn col11 = new TableColumn(aName + "1");
        TableColumn col12 = new TableColumn(aName + "2");
        TableColumn col13 = new TableColumn(aName + "3");
        col1.getColumns().add(col11);
        col11.getColumns().add(col12);
        col12.getColumns().add(col13);
        return col1;
    }
    
    private static TreeTableColumn generateTreeTableColumnColumn(String aName) {
        TreeTableColumn col1 = new TreeTableColumn(aName);
        TreeTableColumn col11 = new TreeTableColumn(aName + "1");
        TreeTableColumn col12 = new TreeTableColumn(aName + "2");
        TreeTableColumn col13 = new TreeTableColumn(aName + "3");
        col1.getColumns().add(col11);
        col11.getColumns().add(col12);
        col12.getColumns().add(col13);
        return col1;
    }
    
}
