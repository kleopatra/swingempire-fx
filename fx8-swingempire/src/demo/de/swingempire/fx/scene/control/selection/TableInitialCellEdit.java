/*
 * Created on 27.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import com.sun.javafx.runtime.VersionInfo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

/**
 * Can't start editing via keyboard initially.
 * Left-over part of issue RT-38464
 * 
 * To reproduce, run example
 * - press down (or any navigatio key) to select a row
 * - press F2
 * - expected: editing started
 * - actual: nothing happens
 * 
 * reported: https://javafx-jira.kenai.com/browse/RT-40364
 * 
 * <hr>------------------
 * 
 * Table doesn't have focus if not wrapped into a (Border)Pane
 * 
 * Bug or feature, asked at SO
 * http://stackoverflow.com/q/29300066/203657
 * 
 */
public class TableInitialCellEdit extends Application {
    
    ObservableList<MenuItem> data = FXCollections.observableArrayList(
            new MenuItem("some"),
            new MenuItem("dummy"),
            new MenuItem("data")
            );

    private Parent getContent() {
        TableView<MenuItem> table = new TableView<>(data);
        table.setEditable(true);
        TableColumn<MenuItem, String> text = new TableColumn<>("Text");
        text.setCellValueFactory(new PropertyValueFactory<>("text"));
        text.setCellFactory(TextFieldTableCell.forTableColumn());
        table.getColumns().addAll(text);
        // focus transfered to table if contained in a pane
        // return new Pane(table);
        // focus not transfered to table if contained directly in the scene 
        return table;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
//        TabOrderHelper h;
        Scene scene = new Scene(getContent());
        scene.focusOwnerProperty().addListener((s, old, value) -> System.out.println(value));
        primaryStage.setScene(scene);
        primaryStage.setTitle(VersionInfo.getRuntimeVersion());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
