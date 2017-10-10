/*
 * Created on 15.09.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * fixed in fx9
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComboInHeaderFocusTraversal extends Application {

    private Parent getContent() {
        TableView table = new TableView();
        TableColumn buttonColumn = new TableColumn("TextField");
        buttonColumn.setGraphic(new TextField("first")); 
        buttonColumn.setMinWidth(200);
        TableColumn boxColumn = new TableColumn("ComboBox");
        // focus traversal stops on combo (or any button)
        boxColumn.setGraphic(new ComboBox());
        buttonColumn.setMinWidth(200);
        TableColumn fieldColumn = new TableColumn("TextField");
        fieldColumn.setGraphic(new TextField("last"));
        fieldColumn.setMinWidth(200);
        
        table.getColumns().addAll(buttonColumn, boxColumn, fieldColumn);
        
        
        FlowPane buttons = new FlowPane(10, 10);
        buttons.getChildren().addAll(new Button("Dummy"), new ComboBox(), new TextField());
        BorderPane pane = new BorderPane(table);
        pane.setBottom(buttons);
        return pane;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 800, 400));
//        URL uri = getClass().getResource("headers.css");
//        primaryStage.getScene().getStylesheets().add(uri.toExternalForm());
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
