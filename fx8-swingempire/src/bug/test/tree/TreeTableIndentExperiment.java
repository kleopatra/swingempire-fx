/*
 * Created on 18.10.2018
 *
 */
package test.tree;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.event.CheckBoxTreeCellWithGraphic;
import de.swingempire.fx.event.GraphicInTreeTableItem;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage; 

/**
 * https://bugs.openjdk.java.net/browse/JDK-8212640
 * missing indentation if only nodes without children are showing
 * 
 * run: no indentation
 * scroll down until node with children is showing 
 *    -> items above jump to correct indent
 *    
 * Another quirk (this example)
 * 
 * - run
 * - scroll down to folder
 * - expand
 * - scroll up page-wise
 * - expected: all indents correct
 * - actual: one or more of the rows have no ident 
 *    
 * Checked TreeView: is fine, so specific to layout in TreeTableRowSkin   
 *    
 * related to 
 * https://bugs.openjdk.java.net/browse/JDK-8094321
 * that one is fixed, but only for items that are already showing (?)
 * 
 * related also to:
 * https://bugs.openjdk.java.net/browse/JDK-8190331
 * that's showing a completely broken layout ..
 * 
 * @see GraphicInTreeTableItem
 * @see CheckBoxTreeCellWithGraphic
 *    
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableIndentExperiment extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeTableView<String> tableView = new TreeTableView<>();
        // happens only if root is hidden
        tableView.setShowRoot(false);

        TreeTableColumn<String, String> column = new TreeTableColumn<>("col1");
        column.setPrefWidth(100);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<String>(
                param.getValue().getValue()));
        tableView.getColumns().add(column);

        TreeItem<String> root = new TreeItem<>("root");
        insertChildren(root, "a", 30);
        TreeItem<String> itemWithChildren = new TreeItem<>("childfolder");
        itemWithChildren.setExpanded(false);
        insertChildren(itemWithChildren, "b", 5);
        root.getChildren().add(itemWithChildren);

        tableView.setRoot(root);

        Scene scene = new Scene(tableView);
        primaryStage.setScene(scene);
        primaryStage.show();

        LOG.info("indent level? " + tableView.getTreeItemLevel(root.getChildren().get(0)));
    }

    /**
     * @param root
     * @param text
     */
    protected void insertChildren(TreeItem<String> root, String text, int size) {
        root.getChildren().addAll(Stream
                .generate(() -> new TreeItem<>(text))
                .limit(size)
                .collect(Collectors.toList()));
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableIndentExperiment.class.getName());

} 