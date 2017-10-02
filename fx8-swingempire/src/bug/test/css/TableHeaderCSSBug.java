/*
 * Created on 29.09.2017
 *
 */
package test.css;

import javafx.application.Application; 
import javafx.scene.Scene; 
import javafx.scene.control.TableColumn; 
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.stage.Stage; 
/**
https://bugs.openjdk.java.net/browse/JDK-8188164
here we get two "additional" filler headers, on top of the normal and the filterHeader
specifics don't work, see fx css reference
"Note that JavaFX does not currently support structural pseudo-classes. "
*/
public class TableHeaderCSSBug extends Application { 

    @Override 
    public void start(Stage primaryStage) throws Exception { 
        TableView<String> tableView = new TableView<>(); 
        for (int i = 0; i < 5; i++) { 
            TableColumn<String, String> column = new TableColumn<>(); //"Col " + i); 
            tableView.getColumns().add(column); 
        } 

        Scene scene = new Scene(tableView); 
        scene.getStylesheets().add(TableHeaderCSSBug.class.getResource("headerbug.css").toExternalForm()); 

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