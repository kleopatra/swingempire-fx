/*
 * Created on 11.08.2017
 *
 */
package test.combobox;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Check cell padding in auto-size: seems to be working
 */
public class TableColumnPaddingBug extends Application {

    public static class PlainTableCell<S, T> extends TableCell<S, T> {
        
        public PlainTableCell() {
            setPadding(new Insets(3, 20, 3, 20));
        }
        
        @Override 
        protected void updateItem(T item, boolean empty) {
            if (item == getItem()) return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node)item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
        }


    }

    private Parent getContent() {
        TableView<String> table = new TableView<>(FXCollections.observableArrayList(
                "LongestText in content"));
        TableColumn<String, String> countryCode = new TableColumn<>("Code");
        countryCode.setCellValueFactory(e -> new SimpleStringProperty(e.getValue()));
        countryCode.setCellFactory(c -> new PlainTableCell<>());
        table.getColumns().addAll(countryCode);
        BorderPane pane = new BorderPane(table);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 800, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
