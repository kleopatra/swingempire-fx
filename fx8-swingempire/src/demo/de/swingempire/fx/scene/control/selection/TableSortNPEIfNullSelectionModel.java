/*
 * Created on 09.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Issue TableView throws NPE on sorting column if selectionModel property value is null.
 * reported https://javafx-jira.kenai.com/browse/RT-39624
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableSortNPEIfNullSelectionModel extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales()
                    );

    private Parent getContent() {
        // instantiate the table with null items
        TableView<Locale> view = new TableView<Locale>(data);
        TableColumn<Locale, String> column = new TableColumn<>(
                "Language");
        column.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        // either click on header for sorting
        view.getColumns().addAll(column);
        view.setSelectionModel(null);
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
