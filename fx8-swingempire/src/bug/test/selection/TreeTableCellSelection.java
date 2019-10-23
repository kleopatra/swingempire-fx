/*
 * Created on 23.10.2019
 *
 */
package test.selection;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Simplified example for
 * https://bugs.openjdk.java.net/browse/JDK-8232825 cell navigation not working,
 * left/right expand/collapse item, don't navigate
 * 
 * seems to be completely broken ..
 * OP tracked into changes in behavior
 */
public class TreeTableCellSelection extends Application {
    
    private Parent createContent() {
        TreeItem<Locale> root = new TreeItem<>(null);
        root.getChildren().add(createRoot());
        root.getChildren().add(createRoot());
        
        TreeTableView<Locale> treeTable = new TreeTableView<>(root);
        treeTable.getSelectionModel().setCellSelectionEnabled(true);
        TreeTableColumn<Locale, String> name = new TreeTableColumn<>("Name");
        name.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayName"));
        TreeTableColumn<Locale, String> language = new TreeTableColumn<>("Language");
        language.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayLanguage"));
        TreeTableColumn<Locale, String> country = new TreeTableColumn<>("Country");
        country.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayCountry"));
        treeTable.getColumns().addAll(name, language, country);
        BorderPane content = new BorderPane(treeTable);
        return content;
    }

    /**
     * @return
     */
    private TreeItem<Locale> createRoot() {
        TreeItem<Locale> root =  new TreeItem<>(Locale.getDefault());
        addChildren(root);
        return root;
    }

    /**
     * 
     */
    protected void addChildren(TreeItem<Locale> root) {
        Arrays.stream(Locale.getAvailableLocales(), 10, 15)
            .map(TreeItem::new)
            .forEach(root.getChildren()::add);
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
            .getLogger(TreeTableCellSelection.class.getName());

}
