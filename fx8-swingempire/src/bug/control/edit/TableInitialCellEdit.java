/*
 * Created on 15.09.2017
 *
 */
package control.edit;

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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * This is the original for 
 * https://bugs.openjdk.java.net/browse/JDK-8089652
 * 
 * in fx9, instead of doing nothing it throws an NPE from behavior
 * 
 * @author Jeanette Winzenburg, Berlin
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
        return new BorderPane(table);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(VersionInfo.getRuntimeVersion());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
