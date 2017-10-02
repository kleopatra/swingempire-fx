/*
 * Created on 29.09.2017
 *
 */
package test.css;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage; 
/**
 * "label" visibility is bound to column text empty/null
 * 
 * related to:
 * https://bugs.openjdk.java.net/browse/JDK-8188164
 * 
 */
public class TableHeaderLabelVisibleBug extends Application { 

    @Override 
    public void start(Stage primaryStage) throws Exception { 
        TableView<String> tableView = new TableView<>(); 
        TableColumn<String, String> column = new TableColumn<>("initial"); //"Col " + i); 
        tableView.getColumns().add(column);
//        for (int i = 0; i < 5; i++) { 
//            tableView.getColumns().add(column); 
//        } 

        Button toggleText = new Button("toggleText");
        toggleText.setOnAction(e -> {
            boolean hasText = column.getText() != null;
            column.setText(hasText? null : "dummy");
        });
        BorderPane pane = new BorderPane(tableView);
        pane.setBottom(toggleText);
        Scene scene = new Scene(pane); 
//        scene.getStylesheets().add(TableHeaderCSSBug.class.getResource("headerbug.css").toExternalForm()); 

        primaryStage.setScene(scene); 
        primaryStage.setWidth(500); 
        primaryStage.setHeight(400); 
        primaryStage.centerOnScreen(); 
        primaryStage.show(); 
    } 

    public static void main(String[] args) { 
        launch(args); 
    } 
} 