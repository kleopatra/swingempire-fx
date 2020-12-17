package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/65304453/203657
 * item disappears on adding 
 * 
 * worksforme (original was fxml, shouldn't make a difference)
 * might be related to https://bugs.openjdk.java.net/browse/JDK-8244826
 * viewport going blank on collapsing item
 * 
 * works for current fx15+ (Jose notes 16-ea+3), not working for fx11 
 * (Jose notes as late as 16-ea+2) - in between the eas was the change
 * to VirtualFlow (not removing sheetChildren)
 */
public class TreeItemDisappearOnAdd extends Application {

    private Parent createContent() {
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        final TreeItem<String> childNode2 = new TreeItem<>("Child Node 2");
        final TreeItem<String> childNode3 = new TreeItem<>("Child Node 3");

        final TreeItem<String> root = new TreeItem<>("Root node");
        root.setExpanded(true);
        root.getChildren().setAll(childNode1, childNode2, childNode3);

        TreeTableView<String> treeTableView = new TreeTableView<>(root);
        treeTableView.setShowRoot(true);

        TreeTableColumn<String, String> columnC1 = new TreeTableColumn<>("just some column");
        columnC1.setCellValueFactory((
                TreeTableColumn.CellDataFeatures<String, String> p) -> new ReadOnlyStringWrapper(
                        p.getValue().getValue()));
        treeTableView.getColumns().add(columnC1);
        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(actionEvent -> 
            root.getChildren().add(new TreeItem<>("Another Child")));
        Button btnDelete = new Button("Delete");
        btnDelete.setOnAction(actionEvent -> 
            root.getChildren().remove(root.getChildren().size() - 1));

        BorderPane content = new BorderPane(treeTableView);
        content.setBottom(new HBox(btnAdd, btnDelete));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
//        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeItemDisappearOnAdd.class.getName());

}
