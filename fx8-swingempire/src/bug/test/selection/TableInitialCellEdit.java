package test.selection;

import de.swingempire.fx.util.FXUtils;
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
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
