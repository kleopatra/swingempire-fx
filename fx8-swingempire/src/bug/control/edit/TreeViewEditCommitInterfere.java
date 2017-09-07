/*
 * Created on 05.09.2017
 *
 */
package control.edit;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TreeViewCell: must not interfere with custom commit handler
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8187309
 */
public class TreeViewEditCommitInterfere extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        TreeItem<String> rootItem = new TreeItem<>("root");
        TreeItem<String> child = new TreeItem<>("child");
        rootItem.getChildren().add(child);
        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(TextFieldTreeCell.forTreeView());
        // custom commit handler: replace value on some condition only
        treeView.setOnEditCommit(t -> {
            String ov = t.getOldValue();
            String nv = t.getNewValue();
            if (nv.length() > ov.length()) {
                t.getTreeItem().setValue(nv);
            }
        });

        Button check = new Button("Print child value");
        check.setOnAction(e -> System.out.println(child.getValue()));
        BorderPane pane = new BorderPane(treeView);
        pane.setBottom(check);
        Scene scene = new Scene(pane, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}