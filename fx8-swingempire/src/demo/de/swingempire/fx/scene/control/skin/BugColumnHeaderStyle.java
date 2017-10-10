/*
 * Created on 14.09.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

import java.net.URL;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableColumnHeader: Custom styles are lost. 
 * 
 * reported: 
 * https://bugs.openjdk.java.net/browse/JDK-8166025
 * 
 * fixed in fx9
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BugColumnHeaderStyle extends Application {

    private Parent getContent() {
        TableView table = new TableView();
        TableColumn single = new TableColumn("Single");
        TableColumn nested = new TableColumn("Nested");
        TableColumn childOne = new TableColumn("Child One");
        TableColumn childTwo = new TableColumn("Child Two");
        nested.getColumns().addAll(childOne, childTwo);
        table.getColumns().addAll(single, nested);
        
        Button addStyle = new Button("add style to TableColumn");
        addStyle.setOnAction(e -> nested.getStyleClass().add("dummy"));
        BorderPane pane = new BorderPane(table);
        pane.setBottom(addStyle);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400));
        URL uri = getClass().getResource("headers.css");
        primaryStage.getScene().getStylesheets().add(uri.toExternalForm());
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
