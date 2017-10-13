/*
 * Created on 01.10.2015
 *
 */
package control.skin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * TableRowSkin: NPE on instantiation.
 * 
 * Not reported: it's borderline quirky to have the tableView property
 * not null but not being in its flow.
 * 
 * TableRowSkinBar.getVirtualFlow() can return null, installing the 
 * listener on it may fail. Not respecting that is an error. 
 * 
 */
public class TableRowSkinBug extends Application {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage stage) throws Exception {
//        TableRowSkin s;
        TableRow tableRow = new TableRow();
        tableRow.setSkin(new TableRowSkin(tableRow));
        TableView tableView = new TableView();
        tableRow.updateTableView(tableView);
        FlowPane flowPane = new FlowPane();
        flowPane.getChildren().addAll(tableView, tableRow);
        Scene scene = new Scene(flowPane, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
    
}