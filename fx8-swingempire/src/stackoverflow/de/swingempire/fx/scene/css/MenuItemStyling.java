/*
 * Created on 11.03.2020
 *
 */
package de.swingempire.fx.scene.css;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60637551/203657
 * dynamic styling of menuItem has no effect
 * 
 * looks like a bug (still in fx11)
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuItemStyling extends Application {
    private static final String STYLED = "styled";
    private ListView<String> listView;
    private Button change;

    private Parent removeNode;
    private boolean installed;
    private Label label;

    private Parent createContent() {
        listView = new ListView<>();

        ContextMenu cm = new ContextMenu();

        MenuItem miAdd = new MenuItem("Add");
        miAdd.setOnAction(event -> listView.getItems().add("Apple"));

        MenuItem miRemove = new MenuItem("Remove");
        miRemove.disableProperty().bind(
                listView.getSelectionModel().selectedItemProperty().isNull());
        miRemove.setOnAction(event -> listView.getItems()
                .remove(listView.getSelectionModel().getSelectedItem()));
        cm.getItems().addAll(miAdd, miRemove);

        listView.setContextMenu(cm);
        // from James: working if not set before adding to scenegraph
        miRemove.getStyleClass().add(STYLED);

        miRemove.getStyleClass().addListener((ListChangeListener) c -> {
            FXUtils.prettyPrint(c);
        });
        change = new Button("change style");
        change.setOnAction(event -> {
            if (removeNode == null) {
                removeNode = (Parent) miRemove.getStyleableNode();
                if (removeNode != null && !installed) {
                    System.out.println("remove node?" + removeNode);
                    removeNode.getStyleClass()
                    .addListener((ListChangeListener) c -> {
                        System.out.println("from removeNode: ");
                        FXUtils.prettyPrint(c);
                    });
                    
                    installed = true;
                    label = (Label) removeNode.lookup(".label");
                    System.out.println("label: " + label);
                    label.getStyleClass()
                            .addListener((ListChangeListener) c -> {
                                System.out.println("from label: ");
                                FXUtils.prettyPrint(c);
                            });
                    
                }
            }
            System.out.println("styles: " + miRemove.getStyleClass());
            if (!miRemove.getStyleClass().contains(STYLED)) {
                miRemove.getStyleClass().add(STYLED);
            } else {
                miRemove.getStyleClass().remove(STYLED);
            }
            ((Node) miRemove.getStyleableNode()).applyCss();
            if (label != null) {
                label.applyCss();
            }
        });
        BorderPane content = new BorderPane(listView);
        content.setBottom(new HBox(10, change));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("menustyle.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuItemStyling.class.getName());

}
