/*
 * Created on 09.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Issue TableView throws NPE on sorting column if items property value is null.
 * Reported as https://javafx-jira.kenai.com/browse/RT-37674
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableSortNPEIfNullItems extends Application {

    private Parent getContent() {
        // instantiate the table with null items
        TableView<String> view = new TableView<String>(null);
        TableColumn<String, String> column = new TableColumn<>("Items");
        // either click on header for sorting
        view.getColumns().addAll(column);
        // or add column to sort order immediately
        //view.getSortOrder().add(column);
        Pane parent = new HBox(100);
        parent.getChildren().addAll(view);
        parent.setPadding(new Insets(20));
        return parent;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
}
