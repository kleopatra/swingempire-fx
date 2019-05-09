/*
 * Created on 05.05.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class CopyTableView extends Application {

    public static class CTableCell<T, S> extends TableCell<T, S> implements CopyCellDecorator {

        public CTableCell() {
            setOnMouseClicked(e -> {
               LOG.info(getCopyText()); 
            });
        }
        
        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.toString());
            }
        }
        
        
    }
    private Parent createContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales()));
        TableColumn<Locale, String> column = new TableColumn<>("Display");
        column.setCellFactory(c -> new CTableCell());
        column.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        table.getColumns().addAll(column);
        return new BorderPane(table);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CopyTableView.class.getName());

}
